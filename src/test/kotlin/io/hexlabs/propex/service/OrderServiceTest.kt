package io.hexlabs.propex.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.amazonaws.services.dynamodbv2.model.ScanResult
import io.hexlabs.propex.model.Order
import io.hexlabs.propex.model.Product
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.expect

class OrderServiceTest {
    @Test
    fun `should map data from listOrders into orders correctly`() {
        val client = mockk<AmazonDynamoDB>()
        val result = ScanResult().withItems(
            mapOf( "order" to AttributeValue("1"), "serial" to AttributeValue("abc"), "dateTime" to AttributeValue().withN("1"), "model" to AttributeValue("5098") ),
            mapOf( "order" to AttributeValue("1"), "serial" to AttributeValue("def"), "dateTime" to AttributeValue().withN("1"), "model" to AttributeValue("5099") ),
            mapOf( "order" to AttributeValue("2"), "serial" to AttributeValue("ghi"), "dateTime" to AttributeValue().withN("2"), "model" to AttributeValue("5100") )
            )
        every { client.scan(ScanRequest("Test").withScanFilter(emptyMap())) } returns result
        val expectedOrders = listOf(
            Order(order = "1", dateTime = 1L, products = listOf(
                    Product(serial = "abc", model = "5098"),
                    Product(serial = "def", model = "5099")
                )
            ),
            Order(order = "2", dateTime = 2L, products = listOf(
                    Product(serial = "ghi", model = "5100")
                )
            )
        )
        expect(expectedOrders) { ConnectedOrderService("Test",client).listOrders() }
    }
}