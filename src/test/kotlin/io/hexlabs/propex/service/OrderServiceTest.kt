package io.hexlabs.propex.service

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.amazonaws.services.dynamodbv2.model.ScanResult
import io.hexlabs.propex.model.Order
import io.hexlabs.propex.model.Product
import io.hexlabs.propex.model.toAttributeValues
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.expect

class OrderServiceTest {
    @Test
    fun `should map data from listOrders into orders correctly`() {
        val client = mockk<AmazonDynamoDB>()
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
        val result = ScanResult().withItems(expectedOrders.flatMap { it.toAttributeValues() })
        every { client.scan(ScanRequest("Test").withScanFilter(emptyMap())) } returns result

        expect(expectedOrders) { ConnectedOrderService("Test", client).listOrders() }
    }
}