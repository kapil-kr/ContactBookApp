import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*

import kotlinx.browser.window

val endpoint = window.location.origin

val jsonClient = HttpClient {
    install(JsonFeature) { serializer = KotlinxSerializer() }
    install(Auth) {
        basic {
            realm ="ktor"
            sendWithoutRequest= true
            username = "kapil"
            password = "kapil"
        }
    }
}

suspend fun getContactList(from: Int): List<ContactItem> {
    return jsonClient.get(endpoint + ContactItem.path + "/$from")
}

suspend fun addContactItem(cItem: ContactItem) {
    jsonClient.post<Unit>(endpoint + ContactItem.path) {
        contentType(ContentType.Application.Json)
        body = cItem
    }
}

suspend fun deleteContactItem(cItem: ContactItem) {
    jsonClient.delete<Unit>(endpoint + ContactItem.path + "/${cItem.id}")
}

suspend fun updateContactItem(cItem: ContactItem, newItem: ContactItem) {
    jsonClient.put<Unit>(endpoint + ContactItem.path + "/${cItem.id}") {
        contentType(ContentType.Application.Json)
        body = newItem
    }
}

suspend fun searchByName(name: String, from: Int): List<ContactItem> {
    return jsonClient.get(endpoint + ContactItem.path + "/searchbyname/$name/$from")
}

suspend fun searchByEmail(email: String): List<ContactItem> {
    return jsonClient.get(endpoint + ContactItem.path + "/searchbyemail/$email")
}