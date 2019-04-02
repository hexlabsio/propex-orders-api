package io.hexlabs.propex.service

import io.hexlabs.propex.model.Order
import io.hexlabs.propex.model.Orders
import io.hexlabs.propex.model.Product
import io.hexlabs.propex.model.Products
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.selectAll
import org.joda.time.DateTime

interface OrderService {
    fun listOrders(): List<Order>
    fun create(orders: List<Order>)
}

class ConnectedOrderService : OrderService {
    override fun create(orders: List<Order>) {
        val inserts = orders.map { order -> Orders(order.identifier, order.order, order.dateTime) to order.products.map { product ->
            Products(product.identifier, order.identifier, product.serial, product.model)
        } }
        org.jetbrains.exposed.sql.transactions.transaction {
            val orderRows = inserts.map { it.first }
            val productRows = inserts.flatMap { it.second }
            Orders.batchInsert(orderRows) {
                this[Orders.IDENTIFIER] = it.identifier
                this[Orders.ORDER] = it.order
                this[Orders.DATE_TIME] = DateTime(it.dateTime)
            }
            Products.batchInsert(productRows) {
                this[Products.IDENTIFIER] = it.identifier
                this[Products.ORDER_IDENTIFIER] = it.orderIdentifier
                this[Products.MODEL] = it.model
                this[Products.SERIAL] = it.serial
            }
        }
    }
    override fun listOrders(): List<Order> {
        return org.jetbrains.exposed.sql.transactions.transaction {
            Products.innerJoin(Orders).selectAll().orderBy(Orders.DATE_TIME).map {
                val orderIdentifier = it[Orders.IDENTIFIER]
                val orderNumber = it[Orders.ORDER]
                val orderDateTime = it[Orders.DATE_TIME]
                val productIdentifier = it[Products.IDENTIFIER]
                val productModel = it[Products.MODEL]
                val productSerial = it[Products.SERIAL]
                Pair(
                    Orders(orderIdentifier, orderNumber, orderDateTime.millis / 1000L),
                    Products(productIdentifier, orderIdentifier, productSerial, productModel)
                )
            }.groupBy { it.first.identifier }.map { (_, rows) ->
                val order = rows.first().first
                val products = rows.map { it.second }
                Order(
                    identifier = order.identifier,
                    order = order.order,
                    dateTime = order.dateTime,
                    products = products.map {
                        Product(identifier = it.identifier, serial = it.serial, model = it.model)
                    })
            }
        }
    }
}