package tech.igrant.jizhang.login

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {

    data class LoginFrom(val email: String, val password: String)

    data class TokenWrapper(var email: String, var nickname: String, var token: String, var userId: Long) {
        constructor() : this("", "", "", -1)
    }

    @POST("login")
    fun login(@Body loginFrom: LoginFrom): Observable<TokenWrapper>

}