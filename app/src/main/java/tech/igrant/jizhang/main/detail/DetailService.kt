package tech.igrant.jizhang.main.detail

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.http.*
import tech.igrant.jizhang.framework.LocalStorage
import tech.igrant.jizhang.framework.PageQuery
import tech.igrant.jizhang.framework.PageResult
import tech.igrant.jizhang.framework.RetrofitFacade
import tech.igrant.jizhang.framework.ext.toDate
import tech.igrant.jizhang.framework.ext.toLocalDateTime
import tech.igrant.jizhang.login.TokenManager
import tech.igrant.jizhang.main.account.AccountService
import tech.igrant.jizhang.main.subject.SubjectService
import tech.igrant.jizhang.state.EnvManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface DetailService {

    @POST("/api/details/query")
    fun list(@Body pageQuery: PageQuery<DetailQuery>): Observable<PageResult<DetailViewObject.Remote>>

    @POST("/api/details")
    fun create(@Body detailTo: DetailTransferObject.Remote): Observable<DetailViewObject.Remote>

    @PUT("/api/details/{id}")
    fun update(
        @Path("id") id: Long,
        @Body detailTo: DetailTransferObject.Remote
    ): Observable<DetailViewObject.Remote>

    @POST("/api/details/batch")
    fun createBatch(@Body detailTos: List<DetailTransferObject.Remote>): Observable<List<DetailViewObject.Remote>>

    @DELETE("/api/details/{id}")
    fun delete(@Path("id") id: Long): Completable

    data class DetailQuery(
        val subjectIds: List<Long>?,
        val start: Date,
        val end: Date,
        val sourceAccountId: Long?,
        val destAccountId: Long?
    ) {
        companion object {
            fun first(): DetailQuery {
                return DetailQuery(
                    subjectIds = arrayListOf(),
                    start = LocalDate.now().minusMonths(1).atTime(0, 0, 0).toDate(),
                    end = LocalDate.now().atTime(23, 59, 59).toDate(),
                    sourceAccountId = null,
                    destAccountId = null
                )
            }
        }
    }

    class DetailViewObject {
        data class Local(
            var remoteId: Long,
            val userId: Long,
            var username: String?,
            val sourceAccountId: Long? = null,
            var sourceAccountName: String?,
            val destAccountId: Long? = null,
            var destAccountName: String?,
            val subjectId: Long,
            var subjectName: String?,
            var remark: String?,
            val createdAt: LocalDateTime,
            var updatedAt: LocalDateTime?,
            var amount: Int,
            var localId: String?
        ) {
            fun extern(): Boolean = this.sourceAccountName != null

            fun toTransferObject(): DetailTransferObject.Local {
                return DetailTransferObject.Local(
                    remoteId,
                    userId,
                    sourceAccountId,
                    destAccountId,
                    subjectId,
                    remark,
                    createdAt = createdAt.toDate(),
                    updatedAt = updatedAt?.toDate(),
                    amount = amount,
                    localId = localId
                )
            }
        }

        data class Remote(
            var id: Long,
            val userId: Long,
            var username: String?,
            val sourceAccountId: Long? = null,
            var sourceAccountName: String?,
            val destAccountId: Long? = null,
            var destAccountName: String?,
            val subjectId: Long,
            var subjectName: String?,
            var remark: String?,
            val createdAt: LocalDateTime,
            var updatedAt: LocalDateTime?,
            var amount: Int
        ) {
            fun local(): Local {
                return Local(
                    id,
                    userId,
                    username,
                    sourceAccountId,
                    sourceAccountName,
                    destAccountId,
                    destAccountName,
                    subjectId,
                    subjectName,
                    remark,
                    createdAt,
                    updatedAt,
                    amount,
                    localId = null
                )
            }
        }
    }

    class DetailTransferObject {
        data class Local(
            var remoteId: Long,
            var userId: Long = -1,
            var sourceAccountId: Long? = null,
            var destAccountId: Long? = null,
            var subjectId: Long = -1,
            var remark: String? = null,
            var createdAt: Date = Date(),
            var updatedAt: Date? = null,
            var amount: Int = 0,
            var localId: String?
        ) {
            fun toViewObject(): DetailViewObject.Local {
                val sourceAccountName = this.sourceAccountId?.let {
                    AccountService.findAccountFromMemory(it)?.name
                }
                val subjectName = SubjectService.findSubjectFromMemory(this.subjectId)?.name
                Log.i("TAG", "get subjectName $subjectName")
                return DetailViewObject.Local(
                    remoteId = this.remoteId,
                    userId = this.userId,
                    username = TokenManager.get()?.nickname,
                    sourceAccountId = this.sourceAccountId,
                    destAccountId = this.destAccountId,
                    sourceAccountName = sourceAccountName,
                    destAccountName = this.destAccountId?.let {
                        AccountService.findAccountFromMemory(it)?.name
                    },
                    subjectId = this.subjectId,
                    subjectName = subjectName,
                    createdAt = this.createdAt.toLocalDateTime(),
                    updatedAt = this.updatedAt?.toLocalDateTime(),
                    amount = this.amount,
                    remark = this.remark,
                    localId = this.localId
                )
            }

            fun remote(): Remote {
                return Remote(
                    userId,
                    sourceAccountId,
                    destAccountId,
                    subjectId,
                    remark,
                    createdAt,
                    updatedAt,
                    amount
                )
            }
        }

        data class Remote(
            var userId: Long = -1,
            var sourceAccountId: Long? = null,
            var destAccountId: Long? = null,
            var subjectId: Long = -1,
            var remark: String? = null,
            var createdAt: Date = Date(),
            var updatedAt: Date? = null,
            var amount: Int = 0
        )
    }

    companion object {
        private const val DB = "local_details"

        fun create(detail: DetailTransferObject.Local): Observable<DetailViewObject.Local> {
            if (EnvManager.online()) {
                return RetrofitFacade.get().create(DetailService::class.java)
                    .create(detail.remote())
                    .map { it.local() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            } else {
                detail.localId?.let {
                    LocalStorage.instance().put(DB, it, detail)
                }
                return Observable.just(detail.toViewObject())
            }
        }

        fun update(detail: DetailTransferObject.Local): Observable<DetailViewObject.Local> {
            if (EnvManager.online()) {
                return RetrofitFacade.get().create(DetailService::class.java)
                    .update(detail.remoteId, detail.remote())
                    .map { it.local() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            } else {
                detail.localId?.let {
                    LocalStorage.instance().put(DB, it, detail)
                    return Observable.just(detail.toViewObject())
                } ?: throw Error()
            }
        }

        fun load(): Observable<List<DetailViewObject.Local>> {
            if (EnvManager.offline()) {
                return loadFromLocal()
            }
            return RetrofitFacade.get().create(DetailService::class.java).list(
                PageQuery(
                    queryParam = DetailQuery.first(),
                    page = 0,
                    size = 10
                )
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.content.map { d -> d.local() } }
        }

        fun loadFromLocal(): Observable<List<DetailViewObject.Local>> {
            return Observable.just(
                LocalStorage.instance().batchGet(DB, DetailTransferObject.Local::class.java)
                    .map { it.toViewObject() })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        fun createBatch(list: List<DetailTransferObject.Local>): Observable<List<DetailViewObject.Local>> {
            return RetrofitFacade.get()
                .create(DetailService::class.java)
                .createBatch(list.map { it.remote() })
                .map { it.map { d -> d.local() } }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        fun clearLocal() {
            LocalStorage.instance().batchClear(DB)
        }

        fun delete(detailVo: DetailViewObject.Local): Completable {
            return if (EnvManager.online()) {
                RetrofitFacade.get()
                    .create(DetailService::class.java)
                    .delete(detailVo.remoteId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            } else {
                LocalStorage.instance().delete(DB, detailVo.localId!!)
                Completable.complete()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
        }

    }

}