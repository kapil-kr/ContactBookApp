package resources

import ContactItem
import ServerTest
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class APITests: ServerTest() {
    @Test
    fun testGetContact(): Unit = runBlocking {
        val contact1 = ContactItem("test1", "test1@test1.com")
        val contact2 = ContactItem( "test2", "test2@test2.com")
        addContact(contact1)
        addContact(contact2)
        val contactList = RestAssured.get("/contactList")
                .then()
                .statusCode(200)
                .extract().to<List<ContactItem>>()

        assertThat(contactList).hasSize(2)
        assertThat(contactList).extracting("name").containsExactlyInAnyOrder(contact1.name, contact2.name)
        assertThat(contactList).extracting("email").containsExactlyInAnyOrder(contact1.email, contact2.email)
    }

    @Test
    fun testUpdateContact(): Unit = runBlocking {
        val contact1 = ContactItem( "test1", "test1@test1.com")
        addContact(contact1)
        val contactList = RestAssured.get("/contactList")
            .then()
            .statusCode(200)
            .extract().to<List<Map<*,*>>>()

        assertThat(contactList).hasSize(1)
        assertThat(contactList).extracting("name").containsExactly(contact1.name)
        assertThat(contactList).extracting("email").containsExactly(contact1.email)

        val newContact = ContactItem( "test2", "test2@test2.com")
        given()
            .contentType(ContentType.JSON)
            .body(newContact)
            .When()
            .put("/contactList/${contactList.get(0).get("id")}")
            .then()
            .statusCode(200)

        val contactListAfterUpdate = RestAssured.get("/contactList")
                .then()
                .statusCode(200)
                .extract().to<List<ContactItem>>()

        assertThat(contactListAfterUpdate).hasSize(1)
        assertThat(contactListAfterUpdate).extracting("name").containsOnly(newContact.name)
        assertThat(contactListAfterUpdate).extracting("email").containsOnly(newContact.email)
    }

    @Test
    fun testDeleteContact(): Unit = runBlocking{

        val contact1 = ContactItem("test1", "test1@test1.com")
        addContact(contact1)

        val contactList = RestAssured.get("/contactList")
            .then()
            .statusCode(200)
            .extract().to<List<Map<*,*>>>()

        assertThat(contactList).hasSize(1)
        assertThat(contactList).extracting("name").containsExactly(contact1.name)
        assertThat(contactList).extracting("email").containsExactly(contact1.email)

        RestAssured.delete("/contactList/${contactList.get(0).get("id")}")
                .then()
                .statusCode(200)

        val contactListAfterDelete = RestAssured.get("/contactList")
                .then()
                .statusCode(200)
                .extract().to<List<ContactItem>>()
        assertThat(contactListAfterDelete).hasSize(0)
    }

    private fun addContact(contact: ContactItem) {
        given().contentType(ContentType.JSON)
                .body(contact).When().post("/contactList").then()
                .statusCode(200)
    }
}