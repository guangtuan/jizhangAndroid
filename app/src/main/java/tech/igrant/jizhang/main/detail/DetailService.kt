package tech.igrant.jizhang.main.detail

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST
import tech.igrant.jizhang.framework.PageQuery
import tech.igrant.jizhang.framework.PageResult
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface DetailService {

    @POST("/api/details/query")
    fun list(@Body pageQuery: PageQuery<DetailQuery>): Observable<PageResult<DetailVo>>

    @POST("/api/details")
    fun create(@Body detailTo: DetailTo): Observable<DetailVo>

    data class DetailQuery(
        val subjectIds: List<Long>?,
        val start: LocalDate?,
        val end: LocalDate?,
        val sourceAccountId: Long?,
        val destAccountId: Long?
    ) {
        companion object {
            fun first(): DetailQuery {
                return DetailQuery(
                    subjectIds = arrayListOf(),
                    start = LocalDate.now().minusMonths(1),
                    end = LocalDate.now(),
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
        const val NOT_SPLITED = 0
        const val SPLITED = 1
        const val SPLIT_PARENT = 2
    }

}