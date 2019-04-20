package io.hexlabs.propex.service

import io.hexlabs.propex.model.Order
import io.hexlabs.propex.model.OrderTable
import io.hexlabs.propex.model.Product
import io.hexlabs.propex.model.ProductTable
import io.hexlabs.propex.model.insert
import io.hexlabs.propex.model.orderFrom
import io.hexlabs.propex.model.ordersFrom
import io.hexlabs.propex.model.productFrom
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

interface OrderService: CrudService<Order> {
    fun create(orders: List<Order>)
}

class ConnectedOrderService : OrderService {

    override fun create(item: Order): String {
        TODO()
    }

    override fun create(orders: List<Order>) {
        transaction {
            val inserts = orders.map { order ->
                OrderTable.OrderRow(
                    order.identifier,
                    order.order,
                    DateTime(order.dateTime)
                ) to order.products.map { product ->
                    ProductTable.ProductRow(product.identifier, order.identifier, product.serial, product.model)
                }
            }
            val orderRows = inserts.map { it.first }
            val productRows = inserts.flatMap { it.second }
            OrderTable.batchInsert(orderRows) { insert(it) }
            ProductTable.batchInsert(productRows) { insert(it) }
        }
    }

    override fun read(identifier: String): Order? = transaction {
        ProductTable.innerJoin(OrderTable)
            .select { OrderTable.IDENTIFIER eq identifier }
            .map { OrderTable.OrderRow.from(it) to ProductTable.ProductRow.from(it) }
            .let {
                if (it.isEmpty()) null else (it.first().first to it.map { (_, product) -> product })
            }?.let { (orderRow, productRows) -> orderFrom(orderRow, productRows.map { productFrom(it) }) }
    }

    override fun readAll(): List<Order> = transaction {
        ordersFrom(ProductTable.innerJoin(OrderTable).selectAll().orderBy(OrderTable.DATE_TIME).map {
            OrderTable.OrderRow.from(it) to ProductTable.ProductRow.from(it)
        })
    }

    override fun update(identifier: String, item: Order) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(identifier: String): Boolean {
        return read(identifier)?.also { order -> transaction {
            ProductTable.deleteWhere { ProductTable.IDENTIFIER inList order.products.map { it.identifier } }
            OrderTable.deleteWhere { OrderTable.IDENTIFIER eq order.identifier }
        } } != null
    }
}