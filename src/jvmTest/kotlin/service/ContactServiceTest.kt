package service

import ContactItem
import ServerTest
import kotlinx.coroutines.runBlocking
import model.Contacts
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContactServiceTest : ServerTest() {

    private val contactService = ContactService()

    @Test
    fun getContacts(): Unit = runBlocking {
        val contact1 = Contacts("test1", "test1@test1.com")
        val contact2 = Contacts("test2", "test2@test2.com")
        contactService.addContact(contact1)
        contactService.addContact(contact2)

        val contactsFromDB = contactService.getContacts(0, 10)
        assertThat(contactsFromDB).hasSize(2)
        assertThat(contactsFromDB).extracting("name").containsExactlyInAnyOrder(contact1.name, contact2.name)
        assertThat(contactsFromDB).extracting("email").containsExactlyInAnyOrder(contact1.email, contact2.email)
    }

    @Test
    fun addContact(): Unit = runBlocking {
        val contact = Contacts("test1", "test1@test1.com")
        contactService.addContact(contact)
        val contactsFromDB = contactService.getContacts(0, 10)
        assertThat(contactsFromDB).hasSize(1)
        assertThat(contactsFromDB.get(0).name).isEqualTo(contact.name)
        assertThat(contactsFromDB.get(0).email).isEqualTo(contact.email)
    }

    @Test
    fun deleteContact(): Unit = runBlocking {
        val contact = Contacts("test1", "test1@test1.com")
        contactService.addContact(contact)

        val contactsFromDB = contactService.getContacts(0, 10)
        assertThat(contactsFromDB).hasSize(1)
        contactService.deleteContact(contactsFromDB.get(0).id)

        assertThat(contactService.getContacts(0,10)).isEmpty()
    }

    @Test
    fun updateContact(): Unit = runBlocking {
        val contact = Contacts("test1", "test1@test1.com")
        val newContact = Contacts("test2", "test2@test2.com")
        contactService.addContact(contact)

        val contactsFromDB = contactService.getContacts(0, 10)
        assertThat(contactsFromDB).hasSize(1)
        contactService.updateContact(contactsFromDB.get(0).id, newContact)

        val updatedContactFromDB = contactService.getContacts(0,10)
        assertThat(updatedContactFromDB).hasSize(1)
        assertThat(updatedContactFromDB.get(0).name).isEqualTo(newContact.name)
        assertThat(updatedContactFromDB.get(0).email).isEqualTo(newContact.email)
    }
}