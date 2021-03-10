package tech.igrant.jizhang.startup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tech.igrant.jizhang.R
import tech.igrant.jizhang.framework.RetrofitFacade
import tech.igrant.jizhang.login.LoginActivity
import tech.igrant.jizhang.login.TokenManager
import tech.igrant.jizhang.main.MainActivity

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
        TokenManager.get()
            ?.let {
                RetrofitFacade.init(it)
                MainActivity.start(this)
            } ?: LoginActivity.start(this)
        finish()
    }

}