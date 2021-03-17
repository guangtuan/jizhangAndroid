package tech.igrant.jizhang.state

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import tech.igrant.jizhang.framework.RetrofitFacade

class EnvManager private constructor(val state: State) {

    interface PingService {
        @GET("ping")
        fun ping(): Observable<Any>
    }

    enum class State {
        OFFLINE,
        ONLINE
    }

    companion object {
        lateinit var envManager: EnvManager

        fun init(state: State) {
            envManager = EnvManager(state)
        }

        fun offline(): Boolean {
            return envManager.state == State.OFFLINE
        }

        fun online(): Boolean {
            return envManager.state == State.ONLINE
        }

    }

}