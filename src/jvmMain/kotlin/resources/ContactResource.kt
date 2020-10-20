package resources

import ContactItem
import com.mongodb.MongoWriteException
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import model.Contacts
import service.ContactService

fun Route.contact(contactService: ContactService) {

    route(ContactItem.path) {
        authenticate("bookAuth") {
            get {
                val from = call.request.queryParameters["from"]?.toInt() ?: 0
                val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
                try {
                    val contactList = contactService.getContacts(from, limit)
                    call.respond(contactList)
                }
                catch (e: Exception) {
                    error("Invalid Request")
                }
            }
            get("/searchbyname/{name}") {
                val name = call.parameters["name"]
                val from = call.request.queryParameters["from"]?.toInt() ?: 0
                val limit = call.request.queryParameters["limit"]?.toInt() ?: 10
                if(name == null) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                else {
                    try {
                        call.respond(contactService.searchByname(name, from, limit))
                    }
                    catch (e: Exception) {
                        error("Invalid Request")
                    }
                }
            }
            get("/searchbyemail/{email}") {
                val email = call.parameters["email"]
                if(email == null) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                else {
                    try {
                        call.respond(contactService.searchByemail(email))
                    }
                    catch (e: Exception) {
                        error("Invalid Request")
                    }
                }
            }
            post {
                try {
                    val contactItem = call.receive<ContactItem>()
                    if(contactItem.name.trim().isEmpty() || contactItem.email.trim().isEmpty())
                        call.respond(HttpStatusCode.BadRequest)
                    else {
                        val newContact = Contacts(contactItem.name, contactItem.email)
                        contactService.addContact(newContact)
                        call.respond(HttpStatusCode.OK)
                    }
                }
                catch (e: Exception) {
                    when(e) {
                        is MongoWriteException ->
                            call.respond(HttpStatusCode.MethodNotAllowed, "emailId already exist")
                        else ->
                            error("Invalid Request")
                    }
                }
            }
            delete("/{id}") {
                val id = call.parameters["id"]?: error("Invalid delete request")
                try {
                    val count = contactService.deleteContact(id)
                    if(count == 0L)
                        call.respond(HttpStatusCode.NotFound, "this contact doesn't exists")
                    else
                        call.respond(HttpStatusCode.OK)
                }
                catch (e: Exception) {
                    error("Invalid Request")
                }
            }
            put("/{id}") {
                val id = call.parameters["id"] ?: error("Invalid Update request")
                try {
                    val newContactItem = call.receive<ContactItem>()
                    val newContact = Contacts(newContactItem.name, newContactItem.email)
                    val updateResult = contactService.updateContact(id,
                            newContact)
                    when {
                        updateResult.matchedCount == 0L -> call.respond(HttpStatusCode.NotFound, "this contact doesn't exists")
                        updateResult.modifiedCount >= 1 -> call.respond(HttpStatusCode.OK)
                        updateResult.matchedCount >= 1 && updateResult.modifiedCount == 0L ->
                            call.respond(HttpStatusCode.MethodNotAllowed, "emailId already exist")
                        else -> error("Unable to process request")
                    }
                }
                catch (e: Exception) {
                        error("Invalid Request")
                }
            }
        }
    }
}