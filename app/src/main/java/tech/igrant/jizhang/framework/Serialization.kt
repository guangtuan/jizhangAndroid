package tech.igrant.jizhang.framework

class Serialization private constructor() {

    companion object {

        private val gson = GSONExt.gson()

        fun <T> toJson(t: T): String {
            return gson.toJson(t)
        }

        fun <T> fromJson(str: String, clazz: Class<T>): T {
            return gson.fromJson(str, clazz)
        }
    }

}