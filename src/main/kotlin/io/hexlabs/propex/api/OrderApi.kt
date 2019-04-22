package io.hexlabs.propex.api

import io.hexlabs.propex.instantFrom
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
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.string
import java.util.UUID

class OrderApi(val orderService: OrderService) : Api {
    private val extractCreateOrdersFrom = Body.auto<CreateOrders>().toLens()
    private val extractOrdersFrom = Body.auto<ResourceCollection<OrderResource>>().toLens()
    private val extractOrderFrom = Body.auto<OrderResource>().toLens()
    private val orderIdFrom = Path.string().of(name = "orderId")
    private val startDateFrom = Query.string().optional("startDate")
    private val endDateFrom = Query.string().optional("endDate")
    private fun bodyWith(orders: ResourceCollection<OrderResource>) = { response: Response -> response.with(extractOrdersFrom of orders) }
    private fun bodyWith(order: OrderResource) = { response: Response -> response.with(extractOrderFrom of order) }

    private fun collectionOf(orders: List<Order>): ResourceCollection<OrderResource> {
        return ResourceCollection("/orders", "", member = orders.map {
            orderResourceFrom(it)
        }, operations = listOf(
            ApiOperation(Method.GET.toString()),
            ApiOperation(Method.POST.toString())
        ))
    }
    private fun orderResourceFrom(order: Order) = OrderResource(
        id = "/orders/${order.identifier}",
        context = "",
        operations = listOf(
            ApiOperation(Method.GET.toString()),
            ApiOperation(Method.POST.toString()),
            ApiOperation(Method.DELETE.toString())
        ),
        identifier = order.identifier,
        order = order.order,
        dateTime = order.dateTime,
        products = order.products
    )

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
            Method.GET to { request: Request ->
                Thread.sleep(20000)
                val startDate = startDateFrom(request)?.let { instantFrom(it) }
                val endDate = endDateFrom(request)?.let { instantFrom(it) }
                Response(OK).with(bodyWith(collectionOf(orderService.readAll(startDate, endDate)))) },
            Method.POST to { request: Request ->
                batchInsert(extractCreateOrdersFrom(request))
                Response(CREATED)
            }
        ),
        "/orders/{orderId}" bind routes(
            Method.GET to { request: Request ->
                orderService.read(orderIdFrom(request))?.let {
                    Response(OK).with(bodyWith(orderResourceFrom(it)))
                } ?: Response(NOT_FOUND)
            },
            Method.DELETE to { request: Request ->
                if (orderService.delete(orderIdFrom(request))) Response(OK)
                else Response(NOT_FOUND)
            }
        )
    )
}

data class OrderResource(
    override val id: String,
    override val context: String,
    override val operations: List<ApiOperation>,
    val identifier: String,
    val order: String,
    val products: List<Product>,
    val dateTime: String
) : Resource(id, context, operations)