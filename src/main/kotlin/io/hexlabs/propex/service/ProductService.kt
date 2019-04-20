package io.hexlabs.propex.service

import io.hexlabs.propex.model.Order
import io.hexlabs.propex.model.OrderTable
import io.hexlabs.propex.model.Product
import io.hexlabs.propex.model.ProductTable
import io.hexlabs.propex.model.asRow
import io.hexlabs.propex.model.insert
import io.hexlabs.propex.model.orderFrom
import io.hexlabs.propex.model.ordersFrom
import io.hexlabs.propex.model.productFrom
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

interface ProductService: CrudService<Product>

class ConnectedProductService : ProductService {

    override fun create(item: Product): String = transaction {
        TODO()
    }

    override fun read(identifier: String): Product? = transaction {
        ProductTable.select { ProductTable.IDENTIFIER eq identifier }
            .map { productFrom(ProductTable.ProductRow.from(it)) }
            .firstOrNull()
    }

    override fun readAll(): List<Product> = transaction {
        ProductTable.selectAll().map { productFrom(ProductTable.ProductRow.from(it)) }
    }

    override fun update(identifier: String, item: Product) {
        read(identifier)?.also { transaction {
            ProductTable.update({ ProductTable.IDENTIFIER eq identifier }) { update ->
                update[MODEL] = item.model
                update[SERIAL] = item.serial
            }
        } }
    }

    override fun delete(identifier: String): Boolean {
        return read(identifier)?.also { transaction {
            ProductTable.deleteWhere { ProductTable.IDENTIFIER eq identifier }
        } } != null
    }
}