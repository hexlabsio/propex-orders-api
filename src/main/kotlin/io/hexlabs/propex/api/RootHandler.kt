package io.hexlabs.propex.api
import io.hexlabs.propex.model.Orders
import io.hexlabs.propex.model.Products
import io.hexlabs.propex.service.ConnectedOrderService
import io.hexlabs.propex.service.OrderService
import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction

fun main(args: Array<String>) {
    Database.connect("jdbc:postgresql://localhost/propex", "org.postgresql.Driver", "postgres", "postgres")
    org.jetbrains.exposed.sql.transactions.transaction {
        createTables()
    }
    Root().apiRoutes().asServer(SunHttp(8080)).start()
    println("Server started on port 8080")
}

class Root(
    val orderService: OrderService = ConnectedOrderService()
) : Api {
    private val orderApi = OrderApi(orderService)
    override fun apiRoutes() =
        Filters.TRACING
        .then(Filters.CATCH_ALL)
        .then(routes(orderApi.apiRoutes()))
}

fun Transaction.createTables() {
    SchemaUtils.create(Orders, Products)
}