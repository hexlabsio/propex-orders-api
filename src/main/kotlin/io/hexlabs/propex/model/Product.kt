package io.hexlabs.propex.model

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder

data class CreateProduct(val serial: String, val model: String)
data class Product(val identifier: String, val serial: String?, val model: String?)

fun Product.asRow(orderIdentifier: String) = ProductTable.ProductRow(identifier, orderIdentifier, model, serial)

object ProductTable : Table("product") {
    val IDENTIFIER = varchar("identifier", 36).primaryKey()
    val ORDER_IDENTIFIER = varchar("order", 36).primaryKey() references OrderTable.IDENTIFIER
    val SERIAL = varchar("serial", length = 50).index().nullable()
    val MODEL = varchar("model", length = 50).nullable()

    data class ProductRow(val identifier: String, val orderIdentifier: String, val serial: String?, val model: String?) {
        companion object {
            fun from(resultRow: ResultRow) = ProductRow(
                identifier = resultRow[IDENTIFIER],
                orderIdentifier = resultRow[ORDER_IDENTIFIER],
                serial = resultRow[SERIAL],
                model = resultRow[MODEL]
            )
        }
    }
}

fun UpdateBuilder<*>.insert(row: ProductTable.ProductRow) {
    this[ProductTable.IDENTIFIER] = row.identifier
    this[ProductTable.ORDER_IDENTIFIER] = row.orderIdentifier
    this[ProductTable.MODEL] = row.model
    this[ProductTable.SERIAL] = row.serial
}