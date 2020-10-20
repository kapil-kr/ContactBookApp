import kotlinx.serialization.Serializable

@Serializable
data class ContactItem(val name: String, val email: String) {
    var id = ""
    companion object {
        const val path = "/contactList"
    }
}