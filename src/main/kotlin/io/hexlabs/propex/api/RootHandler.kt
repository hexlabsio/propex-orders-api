package io.hexlabs.propex.api

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType
import io.hexlabs.propex.model.Order
import io.hexlabs.propex.model.Product

fun main(args: Array<String>) {
    val client = AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "eu-west-1"))
        .build()
}

fun randomString(length: Int) = (0..length).map { '0' + (Math.random() * 9).toInt() }.joinToString(separator = "")

fun randomOrders() = (0..10000).map { index -> Order(order = randomString(5), products = (0..10).map { Product(randomString(10), randomString(8)) }, dateTime = (Math.random()*1000).toLong()) }

fun deleteTable(name: String, client: AmazonDynamoDB) {
    client.deleteTable(name)
}

fun createTable(name: String, client: AmazonDynamoDB) {
    try {
        println("Attempting to create table; please wait...")
        val table = DynamoDB(client).createTable(name,
            listOf(
                KeySchemaElement("order", KeyType.HASH),
                KeySchemaElement("serial", KeyType.RANGE)
            ),
            listOf(
                AttributeDefinition("order", ScalarAttributeType.S),
                AttributeDefinition("serial", ScalarAttributeType.S)
            ),
            ProvisionedThroughput(5L, 5L)
        )
        table.waitForActive()
        System.out.println("Success.  Table status: " + table.description.tableStatus)
    } catch (e: Exception) {
        System.err.println(e.message)
    }
}