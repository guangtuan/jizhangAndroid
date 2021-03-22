package tech.igrant.jizhang.main.account

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.http.GET
import tech.igrant.jizhang.framework.LocalStorage
import tech.igrant.jizhang.framework.RetrofitFacade
import tech.igrant.jizhang.state.EnvManager
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

        private const val DB = "accounts"

        private var memoryCache: MutableList<AccountVo> = mutableListOf()

        fun findAccountFromMemory(id: Long): AccountVo? {
            if (memoryCache.isNotEmpty()) {
                memoryCache.find { accountVo -> id == accountVo.id }
            }
            val localData = LocalStorage.instance().batchGet(DB, AccountVo::class.java)
            memoryCache.clear()
            memoryCache.addAll(localData)
            Log.i("TAG", memoryCache.joinToString(",") { it.toString() })
            Log.i("TAG", "" + id)
            return memoryCache.find { accountVo -> id == accountVo.id }
        }

        fun loadAccount(): Observable<List<AccountVo>> {
            if (EnvManager.offline()) {
                return Observable
                    .just(LocalStorage.instance().batchGet(DB, AccountVo::class.java))
                    .doOnNext {
                        memoryCache.clear()
                        memoryCache.addAll(it)
                    }
            }
            if (memoryCache.isNotEmpty()) {
                Observable.just(memoryCache.toList()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
            return RetrofitFacade.get().create(AccountService::class.java)
                .list()
                .doOnNext {
                    memoryCache.clear()
                    memoryCache.addAll(it)
                    LocalStorage.instance().batchClear(DB)
                    LocalStorage.instance().batchSave(DB, { account -> "" + account.id }, it)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

}