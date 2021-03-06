package tech.igrant.jizhang.startup

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tech.igrant.jizhang.databinding.ActivityStartupBinding
import tech.igrant.jizhang.framework.RetrofitFacade
import tech.igrant.jizhang.login.LoginActivity
import tech.igrant.jizhang.login.TokenManager
import tech.igrant.jizhang.main.InitService
import tech.igrant.jizhang.main.MainActivity
import tech.igrant.jizhang.state.EnvManager

class StartupActivity : AppCompatActivity() {

    lateinit var binding: ActivityStartupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        TokenManager.get()
            ?.let {
                RetrofitFacade.init(it)
                binding.setToOnline.visibility = View.VISIBLE
                binding.setToOnline.setOnClickListener {
                    EnvManager.init(EnvManager.State.ONLINE)
                    binding.loading.visibility = View.VISIBLE
                    binding.loading.show()
                    InitService.init().subscribe(
                        {
                            runOnUiThread {
                                binding.loading.hide()
                                MainActivity.start(this)
                                finish()
                            }
                        },
                        {
                            run {
                                runOnUiThread {
                                    Toast.makeText(this, "login expired", Toast.LENGTH_SHORT).show()
                                    TokenManager.clear()
                                    binding.loading.hide()
                                    LoginActivity.start(this); finish();
                                }
                            }
                        }
                    )
                }
                binding.setToOffline.visibility = View.VISIBLE
                binding.setToOffline.setOnClickListener {
                    EnvManager.init(EnvManager.State.OFFLINE)
                    binding.loading.visibility = View.VISIBLE
                    binding.loading.show()
                    InitService.init().subscribe {
                        runOnUiThread {
                            binding.loading.hide()
                            MainActivity.start(this)
                            finish()
                        }
                    }
                }
            }
            ?: run { LoginActivity.start(this); finish(); }
    }

}