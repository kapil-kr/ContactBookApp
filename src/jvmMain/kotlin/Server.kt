import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

import org.litote.kmongo.*
import org.litote.kmongo.coroutine.*
import org.litote.kmongo.reactivestreams.KMongo
import com.mongodb.ConnectionString
import com.mongodb.client.model.IndexOptions
import io.ktor.auth.*

val connectionString: ConnectionString? = System.getenv("MONGODB_URI")?.let {
    ConnectionString("$it?retryWrites=false")
}

val client = if (connectionString != null) KMongo.createClient(connectionString).coroutine
                else
                    KMongo.createClient().coroutine
val database = client.getDatabase(connectionString?.database ?: "contactBook")
//val database = client.getDatabase("contactBook")
val collection = database.getCollection<ContactItem>()

suspend fun dropCollecion() {
    collection.drop()
}

fun main() {
    val port = System.getenv("PORT")?.toInt()?:9090
    embeddedServer(Netty, port) {
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
                    if (credentials.password == "kapil")
                        UserIdPrincipal(credentials.name)
                    else
                        null
                }
            }
        }

        routing {
                get("/") {
                    collection.ensureUniqueIndex(ContactItem::email)
                    call.respondText(
                            this::class.java.classLoader.getResource("index.html")!!.readText(),
                            ContentType.Text.Html
                    )
                }
                static("/") {
                    resources("")
                }
                route(ContactItem.path) {
                    authenticate("bookAuth") {
                    get("/{from}") {
                        val from = call.parameters["from"]?.toInt() ?: error("Invalid search Request")
                        call.respond(collection.find().skip(from).limit(10).toList())
                    }
                    get("/searchbyname/{name}/{from}") {
                        val name = call.parameters["name"] ?: error("Invalid search request")
                        val from = call.parameters["from"]?.toInt() ?: error("Invalid search Request")
                        call.respond(collection.find(ContactItem::name eq name)
                                .skip(from).limit(10).toList())
                        call.respond(HttpStatusCode.OK)
                    }
                    get("/searchbyemail/{email}") {
                        val email = call.parameters["email"] ?: error("Invalid search request")
                        call.respond(collection.find(ContactItem::email eq email).toList())
                        call.respond(HttpStatusCode.OK)
                    }
                    post {
                        try {
                            collection.insertOne(call.receive<ContactItem>())
                            call.respond(HttpStatusCode.OK)
                        } catch (e: Exception) {
                            error("Invalid Request")
                        }
                    }
                    delete("/{id}") {
                        val id = call.parameters["id"]?.toInt() ?: error("Invalid delete request")
                        collection.deleteOne(ContactItem::id eq id)
                        call.respond(HttpStatusCode.OK)
                    }
                    put("/{id}") {
                        val id = call.parameters["id"]?.toInt() ?: error("Invalid Update request")
                        try {
                            collection.updateOne(ContactItem::id eq id,
                                    call.receive<ContactItem>())
                            call.respond(HttpStatusCode.OK)
                        } catch (e: Exception) {
                            error("Invalid Request")
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}