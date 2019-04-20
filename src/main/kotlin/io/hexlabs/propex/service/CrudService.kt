package io.hexlabs.propex.service

interface CrudService<T : Any> {
    fun read(identifier: String): T?
    fun readAll(): List<T>
    fun create(item: T): String
    fun update(identifier: String, item: T)
    fun delete(identifier: String): Boolean
}
