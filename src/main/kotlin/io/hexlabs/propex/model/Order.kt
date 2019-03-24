package io.hexlabs.propex.model

import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.model.AttributeValue

const val ORDER = "order"
const val MODEL = "model"
const val SERIAL = "serial"
const val DATE_TIME = "dateTime"

data class Order(val order: String, val products: List<Product>, val dateTime: Long)

fun Order.toDynamoItems() = products.map { product ->
    Item()
        .withString(ORDER, order)
        .withNumber(DATE_TIME, dateTime)
        .withString(MODEL, product.model)
        .withString(SERIAL, product.serial)!!
}
fun Order.toAttributeValues() = products.map { product ->
    mapOf(
        ORDER to AttributeValue(order),
        DATE_TIME to AttributeValue().withN(dateTime.toString()),
        MODEL to AttributeValue(product.model),
        SERIAL to AttributeValue(product.serial)
    )
}