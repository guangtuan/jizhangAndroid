package tech.igrant.jizhang.startup

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import tech.igrant.jizhang.databinding.ActivityStartupBinding
import tech.igrant.jizhang.framework.RetrofitFacade
import tech.igrant.jizhang.login.LoginActivity
import tech.igrant.jizhang.login.TokenManager
import tech.igrant.jizhang.main.MainActivity
import tech.igrant.jizhang.state.EnvManager

class StartupActivity : AppCompatActivity() {

    lateinit var activityStartupBinding: ActivityStartupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityStartupBinding = ActivityStartupBinding.inflate(layoutInflater)

        TokenManager.get()
            ?.let {
                RetrofitFacade.init(it)
                activityStartupBinding.startUpLoading.visibility = View.VISIBLE
                RetrofitFacade.get().create(EnvManager.PingService::class.java).ping()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { error ->
                        run {
                            error.printStackTrace()
                            onOffline()
                        }
                    }
                    .subscribe(
                        {
                            EnvManager.init(EnvManager.State.ONLINE)
                            activityStartupBinding.startUpLoading.visibility = View.GONE
                            MainActivity.start(this)
                        },
                        {
                            it.printStackTrace()
                            onOffline()
                        }
                    )
            } ?: LoginActivity.start(this)
        finish()
    }

    private fun onOffline() {
        runOnUiThread {
            Toast.makeText(this@StartupActivity, "当前是离线模式", Toast.LENGTH_SHORT)
                .show()
            activityStartupBinding.startUpLoading.visibility = View.GONE
        }
        EnvManager.init(EnvManager.State.OFFLINE)
        MainActivity.start(this)
    }

}