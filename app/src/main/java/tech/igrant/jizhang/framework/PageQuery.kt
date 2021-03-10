package tech.igrant.jizhang.framework

class PageQuery<T>(
    val queryParam: T,
    val page: Int,
    val size: Int
) {
    fun next(): PageQuery<T> {
        return PageQuery(
            queryParam = queryParam,
            page = page + 1,
            size = size
        )
    }
}

class PageResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val total: Long
) {
    fun hasNext(): Boolean {
        return (this.page + 1) * this.size < this.total
    }
}