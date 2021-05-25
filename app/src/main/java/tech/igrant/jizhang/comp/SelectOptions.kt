package tech.igrant.jizhang.comp

class SelectOptions(val keys: List<String>, val map: Map<String, List<String>>) {

    fun get(key: String): List<String> = map.getOrDefault(key, emptyList())

    fun first(): List<String> = get(keys[0])

}