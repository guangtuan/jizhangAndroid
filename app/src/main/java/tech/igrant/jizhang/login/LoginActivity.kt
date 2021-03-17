package tech.igrant.jizhang.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import tech.igrant.jizhang.R
import tech.igrant.jizhang.framework.RetrofitFacade
import tech.igrant.jizhang.main.MainActivity
import tech.igrant.jizhang.state.EnvManager

class LoginActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val endpoint = findViewById<EditText>(R.id.endpoint_address)
        val loginName = findViewById<EditText>(R.id.login_name)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        login.setOnClickListener {
            val loginNameStr = loginName.text?.toString()
            val passwordStr = password.text?.toString()
            val endpointStr = endpoint.text?.toString()
            if (loginNameStr == null) {
                Toast.makeText(this, "请输入登录名", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (passwordStr == null) {
                Toast.makeText(this, "请输入密码", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (endpointStr == null) {
                Toast.makeText(this, "请输入请求地址", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val form = LoginService.LoginFrom(loginNameStr, passwordStr)
            RetrofitFacade.tmp(endpointStr).create(LoginService::class.java).login(form)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { runOnUiThread { loading.visibility = VISIBLE } }
                .subscribe(
                    {
                        loading.visibility = GONE
                        TokenManager.set(
                            TokenManager.AppDataSource(
                                token = it.token,
                                email = it.email,
                                nickname = it.nickname,
                                endpoint = endpointStr,
                                userId = it.userId
                            )
                        )
                        EnvManager.init(EnvManager.State.ONLINE)
                        MainActivity.start(this)
                        finish()
                    },
                    {
                        it.printStackTrace()
                        loading.visibility = GONE
                        EnvManager.init(EnvManager.State.OFFLINE)
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    }
                )
        }
    }

}