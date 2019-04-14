package io.hexlabs.propex.api
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters

object Filters {
    val TRACING = ServerFilters.RequestTracing({ request, zipkin ->
        val origin = request.header("Origin") ?: "None"
        println("START [${zipkin.traceId.value} ${request.method.name}] - ${request.uri} ORIGIN - $origin")
    }, { request, response, zipkin ->
        println("END [${zipkin.traceId.value} ${request.method.name}] - ${request.uri} RESPONSE - ${response.status}")
    })
    val CATCH_ALL = Filter { next -> { request ->
        try { next(request) } catch (e: Exception) {
            e.printStackTrace()
            Response(Status.INTERNAL_SERVER_ERROR)
        }
    } }
    val CORS = ServerFilters.Cors(CorsPolicy(
        origins = listOf("*"),
        headers = listOf("Content-Type", "X-Amz-Date", "Authorization", "X-Api-Key", "X-Amz-Security-Token", "X-Amz-User-Agent"),
        methods = listOf(Method.GET, Method.POST)))
}