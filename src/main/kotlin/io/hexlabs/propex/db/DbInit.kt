package io.hexlabs.propex.db

import org.flywaydb.core.Flyway

fun main() { DbInit().handle("") }

class DbInit {
    fun handle(input: Any): String {
        val endpoint = (System.getenv("DATABASE_ENDPOINT") ?: "localhost") + (System.getenv("DATABASE_PORT")?.let { ":$it" } ?: "")
        println("Migrating $endpoint ...")
        with(Flyway.configure().dataSource("jdbc:postgresql://$endpoint/postgres", "postgres", "postgres").mixed(true).load()) {
            migrate()
        }
        return "Success"
    }
}