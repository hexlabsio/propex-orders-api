package io.hexlabs.propex.model

data class ResourceCollection<T : Resource>(
    override val id: String,
    override val context: String,
    override val operations: List<ApiOperation>,
    val member: List<T>,
    val totalItems: Int = member.size
) : Resource(id, context, operations)
open class Resource(open val id: String, open val context: String, open val operations: List<ApiOperation>)
data class ApiOperation(val method: String, val returns: Schema? = null, val expects: Schema? = null)
data class Schema(val `$ref`: String)