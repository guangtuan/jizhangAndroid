package tech.igrant.jizhang.login

import tech.igrant.jizhang.framework.LocalStorage
import tech.igrant.jizhang.framework.RetrofitFacade

class TokenManager {

    data class AppDataSource(
        var token: String,
        var email: String,
        var nickname: String,
        val userId: Long
    )

    companion object {

        fun get(): AppDataSource? {
            return LocalStorage.instance().get("token", "token", AppDataSource::class.java)
        }

        fun set(
            appDataSource: AppDataSource
        ) {
            RetrofitFacade.init(appDataSource)
            LocalStorage.instance().put("token", "token", appDataSource)
        }
    }

}