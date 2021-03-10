package tech.igrant.jizhang.framework

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.util.function.Function

class LocalStorage private constructor() {

    private val cacheDirPath = "localDb"

    lateinit var cacheDir: File

    val gson = Gson()

    fun init(context: Context) {
        val f = File(context.externalCacheDir, cacheDirPath)
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
            File(it, key).writeText(gson.toJson(t))
        }
    }

    fun <T> get(db: String, key: String, tClass: Class<T>): T? {
        return File(cacheDir, db).let {
            if (!it.exists()) {
                return null;
            }
            gson.fromJson(File(it, key).readText(), tClass)
        }
    }

    fun <T> batchSave(db: String, keyGetter: Function<T, String>, data: List<T>) {
        for (datum in data) {
            keyGetter.apply(datum).also {
                put(db, it, datum)
            }
        }
    }

    fun <T> batchGet(db: String, tClass: Class<T>): List<T> {
        return File(cacheDir, db).list()
            ?.mapNotNull { File(cacheDir, it).readText() }
            ?.map { gson.fromJson(it, tClass) }
            .orEmpty()
    }

    companion object {
        private val localStorage = LocalStorage()

        fun instance(): LocalStorage {
            return localStorage
        }
    }

}