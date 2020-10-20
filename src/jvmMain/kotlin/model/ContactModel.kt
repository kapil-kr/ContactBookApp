package model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class Contacts(
        val name: String,
        val email: String,
        //@SerialName("_id")
        // mapping issues on filer while updating data
        @Contextual
        val _id : Id<Contacts> = newId()
)