package tech.igrant.jizhang.main.subject

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.http.GET
import tech.igrant.jizhang.framework.RetrofitFacade
import java.time.LocalDateTime

interface SubjectService {

    @GET("/api/subjects")
    fun list(): Observable<List<SubjectVo>>

    data class SubjectVo(
        val name: String,
        val description: String,
        val id: Long,
        val children: MutableList<SubjectVo>,
        val parentId: Long?,
        val parent: String?,
        val level: Int,
        val createdAt: LocalDateTime
    )

    companion object {
        const val LEVEL_BIG = 1
        const val LEVEL_SMALL = 2

        private var memoryCache: MutableList<SubjectVo> = mutableListOf()

        fun loadSubject(): Observable<List<SubjectVo>> {
            return if (memoryCache.isNotEmpty()) {
                Observable.just(memoryCache.toList()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            } else RetrofitFacade.get().create(SubjectService::class.java)
                .list()
                .doOnNext { memoryCache.clear(); memoryCache.addAll(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

}