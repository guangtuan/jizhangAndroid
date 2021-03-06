package tech.igrant.jizhang.main.subject

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.http.GET
import tech.igrant.jizhang.framework.IdName
import tech.igrant.jizhang.framework.LocalStorage
import tech.igrant.jizhang.framework.RetrofitFacade
import tech.igrant.jizhang.state.EnvManager
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

        private const val DB = "subjects";

        private var memoryCache: MutableList<SubjectVo> = mutableListOf()

        fun findSubjectFromMemory(id: Long): SubjectVo? {
            if (memoryCache.isNotEmpty()) {
                for (subjectVo in memoryCache) {
                    if (subjectVo.children.any { child -> child.id == id }) {
                        return subjectVo.children.find { child -> child.id == id }
                    }
                }
                return null
            }
            val localData = LocalStorage.instance().batchGet(DB, SubjectVo::class.java)
            memoryCache.clear()
            memoryCache.addAll(localData)
            for (subjectVo in memoryCache) {
                if (subjectVo.children.any { child -> child.id == id }) {
                    return subjectVo.children.find { child -> child.id == id }
                }
            }
            return null
        }

        fun loadSubjectSync(): List<SubjectVo> {
            return if (memoryCache.isNotEmpty()) {
                memoryCache.flatMap {
                    mutableListOf(it).apply {
                        this.addAll(it.children)
                    }
                }
            } else emptyList()
        }

        data class ToGroup(val pid: Long, val p: String, val id: Long, val name: String)

        fun subjectMap(): Map<IdName, List<IdName>> {
            return loadSubjectSync()
                .mapNotNull { subjectVo ->
                    subjectVo.parent?.let { p ->
                        subjectVo.parentId?.let { pid ->
                            ToGroup(
                                pid = pid,
                                p = p,
                                id = subjectVo.id,
                                name = subjectVo.name
                            )
                        }
                    }
                }
                .groupBy(
                    { toGroup -> IdName(id = toGroup.pid, name = toGroup.p) },
                    { toGroup -> IdName(id = toGroup.id, name = toGroup.name) }
                )
        }

        fun loadSubject(): Observable<List<SubjectVo>> {
            if (EnvManager.offline()) {
                return Observable.just(LocalStorage.instance().batchGet(DB, SubjectVo::class.java))
                    .doOnNext {
                        memoryCache.clear()
                        memoryCache.addAll(it)
                    }
            }
            if (memoryCache.isNotEmpty()) {
                Observable.just(memoryCache.toList()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
            return RetrofitFacade.get().create(SubjectService::class.java)
                .list()
                .doOnNext {
                    memoryCache.clear()
                    memoryCache.addAll(it)
                    LocalStorage.instance().batchClear(DB)
                    LocalStorage.instance().batchSave(DB, { subject -> "" + subject.id }, it)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

}