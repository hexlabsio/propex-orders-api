package io.hexlabs.propex.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import io.hexlabs.propex.model.Order
import io.hexlabs.propex.model.Product

interface OrderService {
    fun listOrders(): List<Order>
}

class ConnectedOrderService(val table: String, val client: AmazonDynamoDB) : OrderService{
    override fun listOrders(): List<Order> {
        return DynamoDB(client).getTable(table).scan().groupBy { it.getString("order") }.map { (order, items) ->
                val dateTime = items.firstOrNull()?.getLong("dateTime") ?: 0
                val products = items.map { Product(serial = it.getString("serial"), model = it.getString("model")) }
                Order(order = order, dateTime = dateTime, products = products)
        }
    }
}