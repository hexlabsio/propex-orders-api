package io.hexlabs.propex.model

data class Order(val order: String, val products: List<Product>, val dateTime: Long)