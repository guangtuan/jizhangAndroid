package tech.igrant.jizhang.main.detail

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.http.Body
import retrofit2.http.POST
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
    fun list(@Body pageQuery: PageQuery<DetailQuery>): Observable<PageResult<DetailVo>>

    @POST("/api/details")
    fun create(@Body detailTo: DetailTo): Observable<DetailVo>

    @POST("/api/details/batch")
    fun createBatch(@Body detailTos: List<DetailTo>): Observable<List<DetailVo>>

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

    data class DetailVo(
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
        var amount: Int,
        var splited: Int?,
        var parentId: Int?
    ) {
        fun extern(): Boolean = this.sourceAccountName != null

        companion object {
            fun fromTo(detailTo: DetailTo): DetailVo {
                val sourceAccountName = detailTo.sourceAccountId?.let {
                    AccountService.findAccountFromMemory(it)?.name
                }
                val subjectName = SubjectService.findSubjectFromMemory(detailTo.subjectId)?.name
                Log.i("TAG", "get subjectName $subjectName")
                return DetailVo(
                    id = -1,
                    userId = detailTo.userId,
                    username = TokenManager.get()?.nickname,
                    sourceAccountId = detailTo.sourceAccountId,
                    destAccountId = detailTo.destAccountId,
                    sourceAccountName = sourceAccountName,
                    destAccountName = detailTo.destAccountId?.let {
                        AccountService.findAccountFromMemory(it)?.name
                    },
                    subjectId = detailTo.subjectId,
                    subjectName = subjectName,
                    createdAt = detailTo.createdAt.toLocalDateTime(),
                    updatedAt = detailTo.updatedAt?.toLocalDateTime(),
                    amount = detailTo.amount,
                    splited = detailTo.splited,
                    parentId = detailTo.parentId,
                    remark = detailTo.remark
                )
            }
        }
    }

    data class DetailTo(
        var userId: Long = -1,
        var sourceAccountId: Long? = null,
        var destAccountId: Long? = null,
        var subjectId: Long = -1,
        var remark: String? = null,
        var createdAt: Date = Date(),
        var updatedAt: Date? = null,
        var amount: Int = 0,
        var splited: Int = NOT_SPLITED,
        var parentId: Int? = null
    )

    companion object {
        private const val DB = "offline_details"

        fun create(detailTo: DetailTo): Observable<DetailVo> {
            if (EnvManager.online()) {
                return RetrofitFacade.get().create(DetailService::class.java).create(detailTo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
            val uuid = UUID.randomUUID().toString().replace("-", "")
            LocalStorage.instance().put(DB, uuid, detailTo)
            return Observable.just(DetailVo.fromTo(detailTo))
        }

        fun loadFromLocal(): Observable<List<DetailTo>> {
            return Observable.just(LocalStorage.instance().batchGet(DB, DetailTo::class.java))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        fun createBatch(list: List<DetailTo>): Observable<List<DetailVo>> {
            return RetrofitFacade.get().create(DetailService::class.java)
                .createBatch(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }

        const val NOT_SPLITED = 0
        const val SPLITED = 1
        const val SPLIT_PARENT = 2
    }

}