package io.hexlabs.propex.api
import io.hexlabs.propex.service.ConnectedOrderService
import io.hexlabs.propex.service.ConnectedProductService
import io.hexlabs.propex.service.OrderService
import io.hexlabs.propex.service.ProductService
import org.http4k.core.HttpHandler
import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.serverless.AppLoader
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>) {
    Root().apiRoutes().asServer(SunHttp(8080)).start()
    println("Server started on port 8080")
}

object RootApi : AppLoader {
    override fun invoke(environment: Map<String, String>): HttpHandler = Root().apiRoutes()
}

class Root(
    orderService: OrderService = ConnectedOrderService(),
    productService: ProductService = ConnectedProductService()
) : Api {
    private val orderApi = OrderApi(orderService)
    private val productApi = ProductApi(productService)
    override fun apiRoutes() =
        Filters.TRACING
        .then(Filters.CATCH_ALL)
        .then(Filters.CORS)
        .then(routes(
            orderApi.apiRoutes(),
            productApi.apiRoutes()
        ))

    companion object {
        init {
            val endpoint = (System.getenv("DATABASE_ENDPOINT") ?: "localhost") + (System.getenv("DATABASE_PORT")?.let { ":$it" } ?: "")
            println("Connecting to Database at $endpoint")
            Database.connect("jdbc:postgresql://$endpoint/postgres", "org.postgresql.Driver", "postgres", "postgres")
        }
    }
}