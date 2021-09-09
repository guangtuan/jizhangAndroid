package tech.igrant.jizhang.framework

import android.content.Context
import android.util.Log
import java.io.File

class LocalStorage private constructor() {

    private val cacheDirPath = "localDb"

    lateinit var cacheDir: File

    fun init(context: Context) {
        val f = File(context.cacheDir, cacheDirPath)
        Log.i("LocalStorage", f.absolutePath)
        if (!f.exists()) {
            f.mkdir()
        }
        cacheDir = f
    }

    fun <T> put(db: String, key: String, t: T) {
        File(cacheDir, db).also {
            if (!it.exists()) {
                it.mkdir()
            }
            File(it, key).writeText(Serialization.toJson(t))
        }
    }

    fun <T> get(db: String, key: String, tClass: Class<T>): T? {
        return File(cacheDir, db).let {
            if (!it.exists()) {
                return null
            }
            Serialization.fromJson(File(it, key).readText(), tClass)
        }
    }

    fun <T> batchSave(db: String, getKey: (t: T) -> String, data: List<T>) {
        for (datum in data) {
            getKey(datum).also {
                Log.i("LocalStorage", "put key $it")
                put(db, it, datum)
            }
        }
    }

    fun batchClear(db: String) {
        File(cacheDir, db).let { dbDir ->
            dbDir.list()?.mapNotNull { singleFile -> File(dbDir, singleFile) }
                    ?.forEach { it.delete() }
        }
    }

    fun <T> batchGet(db: String, tClass: Class<T>): List<T> {
        return File(cacheDir, db).let { dir ->
            dir.list()
                    ?.mapNotNull { f -> File(dir, f).readText() }
                    ?.map { content -> Serialization.fromJson(content, tClass) }
                    .orEmpty()
        }
    }

    fun delete(db: String, localId: String) {
        File(cacheDir, db).let { dir ->
            File(dir, localId).delete()
        }
    }

    companion object {
        private val localStorage = LocalStorage()

        fun instance(): LocalStorage {
            return localStorage
        }
    }

}