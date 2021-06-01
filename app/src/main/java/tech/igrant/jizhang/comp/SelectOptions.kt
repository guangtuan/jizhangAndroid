package tech.igrant.jizhang.comp

import tech.igrant.jizhang.framework.IdName

class SelectOptions(val keys: List<IdName>, val map: Map<IdName, List<IdName>>) {

    fun get(key: IdName): List<IdName> = map.getOrDefault(key, emptyList())

    fun first(): List<IdName> = get(keys[0])

}