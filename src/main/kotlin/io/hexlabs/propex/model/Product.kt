package io.hexlabs.propex.model

import org.jetbrains.exposed.sql.Table

data class CreateProduct(val serial: String, val model: String)
data class Product(val identifier: String, val serial: String, val model: String)

data class Products(val identifier: String, val orderIdentifier: String, val serial: String, val model: String) {
    companion object : Table("product") {
        val IDENTIFIER = varchar("identifier", 36).primaryKey()
        val ORDER_IDENTIFIER = varchar("order", 36).primaryKey() references Orders.IDENTIFIER
        val SERIAL = varchar("serial", length = 50).index().nullable()
        val MODEL = varchar("model", length = 50).nullable()
    }
}