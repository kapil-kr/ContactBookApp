import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

import io.ktor.auth.*
import resources.contact
import resources.index
import service.ContactService

fun Application.serverConfig() {
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Put)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)
        anyHost()
    }
    install(Compression) {
        gzip()
    }
    install(Authentication) {
        basic("bookAuth") {
            realm = "ktor"
            validate { credentials ->
                if (credentials.password == AppConfiguration.password)
                    UserIdPrincipal(credentials.name)
                else
                    null
            }
        }
    }
    val contactService = ContactService()
    routing {
        index()
        contact(contactService)
    }
}

fun main() {
    val port = System.getenv("PORT")?.toInt()?:
                    AppConfiguration.port?: 9090
    embeddedServer(Netty, port) {
        serverConfig()
    }.start(wait = true)
}