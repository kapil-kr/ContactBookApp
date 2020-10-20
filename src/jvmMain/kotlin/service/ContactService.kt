package service

import ContactItem
import com.mongodb.client.result.UpdateResult
import model.Contacts
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.id.toId

class ContactService {

    val collection = DBFactory.getCollection()

    private fun toContactItem(contact: Contacts): ContactItem {
        val contactItem = ContactItem (
            name = contact.name,
            email = contact.email
        )
        contactItem.id = contact._id.toString()
        return contactItem
    }

    suspend fun getContacts(from: Int, limit: Int): List<ContactItem> =
        collection.find().skip(from).limit(limit).toList().map {
            toContactItem(it)
        }

    suspend fun addContact(newContact: Contacts) =
            collection.insertOne(newContact)

    suspend fun deleteContact(id: String) : Long =
        collection.deleteOne(Contacts::_id eq ObjectId(id).toId()).deletedCount

    suspend fun getContactById(id: String): Contacts? =
        collection.findOne(Contacts::_id eq ObjectId(id).toId())

    suspend fun updateContact(id: String, newContact: Contacts) : UpdateResult =
            collection.updateOne(Contacts::_id eq ObjectId(id).toId(), newContact)

    suspend fun searchByname(name: String, from: Int, limit: Int): List<ContactItem> =
            collection.find(Contacts::name eq name)
                    .skip(from).limit(limit).toList().map {
                    toContactItem(it)
                }

    suspend fun searchByemail(email: String): List<ContactItem> =
            collection.find(Contacts::email eq email).toList().map {
                toContactItem(it)
            }
}