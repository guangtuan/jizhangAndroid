package tech.igrant.jizhang.main.account

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.http.GET
import tech.igrant.jizhang.framework.RetrofitFacade
import java.util.*

interface AccountService {

    @GET("/api/accounts")
    fun list(): Observable<List<AccountVo>>

    data class AccountVo(
        var id: Long,
        var type: String,
        var name: String,
        var userId: Long,
        var createdAt: Date,
        var updatedAt: Date,
        var description: String,
        var nickname: String,
        var initAmount: Int
    )

    companion object {
        private var memoryCache: MutableList<AccountVo> = mutableListOf()

        fun loadAccount(): Observable<List<AccountVo>> {
            return if (memoryCache.isNotEmpty()) {
                Observable.just(memoryCache.toList()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            } else RetrofitFacade.get().create(AccountService::class.java)
                .list()
                .doOnNext { memoryCache.clear(); memoryCache.addAll(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

}