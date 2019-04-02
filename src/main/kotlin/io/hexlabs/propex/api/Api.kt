package io.hexlabs.propex.api

import org.http4k.routing.RoutingHttpHandler

interface Api {
    fun apiRoutes(): RoutingHttpHandler
}