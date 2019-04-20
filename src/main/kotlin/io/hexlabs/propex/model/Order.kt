package io.hexlabs.propex.model

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.joda.time.DateTime

data class CreateOrders(val orders: List<CreateOrder>)

data class CreateOrder(val order: String, val dateTime: Long, val products: List<CreateProduct>)

data class Order(val identifier: String, val order: String, val dateTime: Long, val products: List<Product>)

fun orderFrom(orderRow: OrderTable.OrderRow, products: List<Product>) = Order(
    identifier = orderRow.identifier,
    order = orderRow.order,
    dateTime = orderRow.dateTime.millis,
    products = products
)

fun productFrom(productRow: ProductTable.ProductRow) = Product(
    identifier = productRow.identifier,
    model = productRow.model,
    serial = productRow.serial
)

fun ordersFrom(orderProductRows: List<Pair<OrderTable.OrderRow, ProductTable.ProductRow>>): List<Order> {
    return orderProductRows.groupBy { it.first.identifier }
        .map { (_, rows) -> orderFrom(rows.first().first, rows.map { productFrom(it.second) }) }
}

object OrderTable: Table("order") {
    val IDENTIFIER = varchar("identifier", 36).primaryKey()
    val ORDER = varchar("order", length = 50).index()
    val DATE_TIME = datetime("dateTime")

    data class OrderRow(val identifier: String, val order: String, val dateTime: DateTime){
        companion object {
            fun from(resultRow: ResultRow) = OrderRow(
                identifier = resultRow[IDENTIFIER],
                order = resultRow[ORDER],
                dateTime = resultRow[DATE_TIME]
            )
        }
    }
}
fun UpdateBuilder<*>.insert(row: OrderTable.OrderRow) {
    this[OrderTable.IDENTIFIER] = row.identifier
    this[OrderTable.ORDER] = row.order
    this[OrderTable.DATE_TIME] = row.dateTime
}