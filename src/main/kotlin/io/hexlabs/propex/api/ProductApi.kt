package io.hexlabs.propex.api

import com.fasterxml.jackson.annotation.JsonInclude
import io.hexlabs.propex.model.ApiOperation
import io.hexlabs.propex.model.Product
import io.hexlabs.propex.model.Resource
import io.hexlabs.propex.service.ProductService
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.lens.string

class ProductApi(val productService: ProductService) : Api {
    private val extractProductFrom = Body.auto<ProductResource>().toLens()
    private val productIdFrom = Path.string().of(name = "productId")
    private val productFrom = Body.auto<Product>().toLens()
    private fun bodyWith(product: ProductResource) = { response: Response -> response.with(extractProductFrom of product) }

    private fun productResourceFrom(product: Product) = ProductResource(
        id = "/products/${product.identifier}",
        context = "",
        operations = listOf(
            ApiOperation(Method.GET.toString()),
            ApiOperation(Method.POST.toString()),
            ApiOperation(Method.DELETE.toString())
        ),
        identifier = product.identifier,
        model = product.model,
        serial = product.serial
    )

    override fun apiRoutes() = routes(
        "/products/{productId}" bind routes(
            Method.GET to { request: Request ->
                productService.read(productIdFrom(request))?.let {
                    Response(OK).with(bodyWith(productResourceFrom(it)))
                } ?: Response(NOT_FOUND)
            },
            Method.POST to { request: Request ->
                productService.update(productIdFrom(request), productFrom(request))
                Response(OK)
            },
            Method.DELETE to { request: Request ->
                if (productService.delete(productIdFrom(request))) Response(OK)
                else Response(NOT_FOUND)
            }
        )
    )
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductResource(
    override val id: String,
    override val context: String,
    override val operations: List<ApiOperation>,
    val identifier: String,
    val model: String?,
    val serial: String?
) : Resource(id, context, operations)