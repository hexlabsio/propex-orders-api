package io.hexlabs.propex.api

import io.hexlabs.propex.model.ApiOperation
import io.hexlabs.propex.model.CreateOrders
import io.hexlabs.propex.model.Order
import io.hexlabs.propex.model.Product
import io.hexlabs.propex.model.Resource
import io.hexlabs.propex.model.ResourceCollection
import io.hexlabs.propex.service.OrderService
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.format.Jackson.auto
import java.util.UUID

class OrderApi(val orderService: OrderService) : Api {
    private val extractCreateOrdersFrom = Body.auto<CreateOrders>().toLens()
    private val extractOrdersFrom = Body.auto<ResourceCollection<OrderResource>>().toLens()
    private val bodyWith = { orders: ResourceCollection<OrderResource> -> { response: Response -> response.with(extractOrdersFrom of orders) } }

    private fun collectionOf(orders: List<Order>): ResourceCollection<OrderResource> {
        return ResourceCollection("/orders", "", emptyList(), orders.map {
            OrderResource("/orders/${it.order}", "", emptyList(), it.order, it.products, it.dateTime)
        })
    }

    private fun batchInsert(createOrders: CreateOrders) {
        val orders = createOrders.orders.map { order ->
            Order(UUID.randomUUID().toString(), order.order, order.dateTime, order.products.map { product ->
                Product(UUID.randomUUID().toString(), product.serial, product.model)
            })
        }
        orderService.create(orders)
    }

    override fun apiRoutes() = routes(
        "/orders" bind routes(
            Method.GET to { _: Request -> Response(OK).with(bodyWith(collectionOf(orderService.listOrders()))) },
            Method.POST to { request: Request ->
                batchInsert(extractCreateOrdersFrom(request))
                Response(CREATED)
            }
        )
    )
}

data class OrderResource(
    override val id: String,
    override val context: String,
    override val operations: List<ApiOperation>,
    val order: String,
    val products: List<Product>,
    val dateTime: Long
) : Resource(id, context, operations)