package com.example



import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.*
import org.jetbrains.exposed.sql.*

fun Application.configureDatabases() {
    val config = HikariConfig().apply {
        jdbcUrl = environment.config.property("db.url").getString()
        driverClassName = environment.config.property("db.driver").getString()
        username = environment.config.property("db.username").getString()
        maximumPoolSize = environment.config.property("db.maxPoolSize").getString().toInt()
        password = environment.config.property("db.password").getString()

    }
    val dataSource = HikariDataSource(config)
    setUpLiquibase(dataSource)
    Database.connect(dataSource)

    val isDevelopment = environment.config.property("ktor.environment").getString() == "development"

//    transaction {
//        if (isDevelopment) {
//
//            SchemaUtils.drop(
//                Users,
//                Message,
//                Chat,
//                UsersChatsRoles
//            )
//        }
//
//        SchemaUtils.create(
//            Users,
//            Message,
//            Chat,
//            UsersChatsRoles
//        )
//    }
}


private fun setUpLiquibase(dataSource: HikariDataSource)
{
    dataSource.connection.use { connection ->
        Liquibase(
            "db/changelog/db.changelog-master.yaml",
            ClassLoaderResourceAccessor(),
            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
        ).update(Contexts(), LabelExpression())
    }
}
