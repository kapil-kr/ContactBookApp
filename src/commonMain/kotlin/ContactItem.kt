import kotlinx.serialization.Serializable

@Serializable
data class ContactItem(val name: String, val email: String) {
    //TODO: Generate a more reliable ID
    val id: Int = email.hashCode()

    companion object {
        const val path = "/contactList"
    }
}