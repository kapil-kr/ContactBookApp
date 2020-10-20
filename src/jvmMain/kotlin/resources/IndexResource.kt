package resources

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.index() {

    val indexPage = javaClass.getResource("/index.html").readText()
    get("/") {
        call.respondText(indexPage, ContentType.Text.Html)
    }

    static("/") {
        resources("")
    }
}