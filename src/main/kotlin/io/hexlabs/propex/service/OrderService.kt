package io.hexlabs.propex.service

import io.hexlabs.propex.asJoda
import io.hexlabs.propex.jodaDateTimeFrom
import io.hexlabs.propex.model.Order
import io.hexlabs.propex.model.OrderTable
import io.hexlabs.propex.model.ProductTable
import io.hexlabs.propex.model.insert
import io.hexlabs.propex.model.orderFrom
import io.hexlabs.propex.model.ordersFrom
import io.hexlabs.propex.model.productFrom
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface OrderService : CrudService<Order> {
    fun create(orders: List<Order>)
    fun readAll(startDate: Instant?, endDate: Instant?): List<Order>
}

class ConnectedOrderService : OrderService {

    override fun create(item: Order): String {
        TODO()
    }

    override fun readAll(startDate: Instant?, endDate: Instant?): List<Order> {
        val op = if (startDate != null && endDate != null)(OrderTable.DATE_TIME greaterEq startDate.asJoda()) and (OrderTable.DATE_TIME lessEq endDate.asJoda())
        else if (startDate != null) OrderTable.DATE_TIME greaterEq startDate.asJoda()
        else if (endDate != null) OrderTable.DATE_TIME lessEq endDate.asJoda()
        else null
        val query = if (op != null) ProductTable.innerJoin(OrderTable).select(op) else ProductTable.innerJoin(OrderTable).selectAll()
        return transaction {
            ordersFrom(query.map { OrderTable.OrderRow.from(it) to ProductTable.ProductRow.from(it) })
        }
    }

    override fun create(orders: List<Order>) {
        transaction {
            val inserts = orders.map { order ->
                OrderTable.OrderRow(
                    order.identifier,
                    order.order,
                    jodaDateTimeFrom(utcString = order.dateTime)
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
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(identifier: String): Boolean {
        return read(identifier)?.also { order -> transaction {
            ProductTable.deleteWhere { ProductTable.IDENTIFIER inList order.products.map { it.identifier } }
            OrderTable.deleteWhere { OrderTable.IDENTIFIER eq order.identifier }
        } } != null
    }
}