package io.hexlabs.propex.model

import org.jetbrains.exposed.sql.Table

data class CreateOrders(val orders: List<CreateOrder>)

data class CreateOrder(val order: String, val dateTime: Long, val products: List<CreateProduct>)

data class Order(val identifier: String, val order: String, val dateTime: Long, val products: List<Product>)

data class Orders(val identifier: String, val order: String, val dateTime: Long) {
    companion object : Table("order") {
        val IDENTIFIER = varchar("identifier", 36).primaryKey()
        val ORDER = varchar("order", length = 50).index()
        val DATE_TIME = datetime("dateTime")
    }
}
