package tech.igrant.jizhang.framework

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File

class LocalStorage private constructor() {

    private val cacheDirPath = "localDb"

    lateinit var cacheDir: File

    val gson = Gson()

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

    fun <T> batchSave(db: String, getKey: (t: T) -> String, data: List<T>) {
        for (datum in data) {
            getKey(datum).also {
                Log.i("LocalStorage", "put key $it")
                put(db, it, datum)
            }
        }
    }

    fun batchClear(db: String) {
        File(cacheDir, db).list()
            ?.mapNotNull { File(cacheDir, it) }
            ?.forEach { it.delete() }
    }

    fun <T> batchGet(db: String, tClass: Class<T>): List<T> {
        return File(cacheDir, db).let { dir ->
            dir.list()
                ?.mapNotNull { f -> File(dir, f).readText() }
                ?.map { content -> gson.fromJson(content, tClass) }
                .orEmpty()
        }
    }

    companion object {
        private val localStorage = LocalStorage()

        fun instance(): LocalStorage {
            return localStorage
        }
    }

}