import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.restassured.RestAssured
import io.restassured.RestAssured.*
import io.restassured.response.ResponseBodyExtractionOptions
import io.restassured.specification.RequestSpecification
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

open class ServerTest {

    protected fun RequestSpecification.When(): RequestSpecification {
        return this.`when`()
    }

    protected inline fun <reified T> ResponseBodyExtractionOptions.to(): T {
        return this.`as`(T::class.java)
    }

    companion object {

        private var serverStarted = false

        private lateinit var server: ApplicationEngine

        @BeforeAll
        @JvmStatic
        fun startServer() {
            if(!serverStarted) {
                server = embeddedServer(Netty, 9090) {}
                server.start()
                serverStarted = true

                RestAssured.baseURI = "http://localhost"
                RestAssured.port = 9090
                Runtime.getRuntime().addShutdownHook(Thread { server.stop(0, 0, TimeUnit.SECONDS) })
            }
        }
    }
    @BeforeEach
    fun before() = runBlocking {
            dropCollecion()
            Unit
    }
}

class APITests: ServerTest() {
    @Test
    fun testGetContact() = runBlocking {
        val contact1 = ContactItem("test1", "test1@test1.com")
        val contact2 = ContactItem("test2", "test2@test2.com")
        addContact(contact1)
        addContact(contact2)
        val contactList = get("/contactList/0")
                .then()
                .statusCode(200)
                .extract().to<List<ContactItem>>()

        assertThat(contactList).hasSize(2)
        assertThat(contactList).extracting("name").containsExactlyInAnyOrder(contact1.name, contact2.name)
        assertThat(contactList).extracting("email").containsExactlyInAnyOrder(contact1.email, contact2.email)
    }

    @Test
    fun testUpdateContact() = runBlocking {
        val contact1 = ContactItem("test1", "test1@test1.com")
        addContact(contact1)

        val newContact = ContactItem("test2", "test2@test2.com")
        given()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(newContact)
                .When()
                .put("/contactList/${contact1.id}")
                .then()
                .statusCode(200)

        val contactList = get("/contactList/0")
                .then()
                .statusCode(200)
                .extract().to<List<ContactItem>>()

        assertThat(contactList).isNotNull
        assertThat(contactList).hasSize(1)
        assertThat(contactList).extracting("name").containsOnly(newContact.name)
        assertThat(contactList).extracting("email").containsOnly(newContact.email)
    }

    @Test
    fun testDeleteContact() = runBlocking{

        val contact1 = ContactItem("test1", "test1@test1.com")
        addContact(contact1)

        delete("/contactList/${contact1.id}")
                .then()
                .statusCode(200)

        get("/contactList/0")
                .then()
                .statusCode(404)
    }

    private fun addContact(contact: ContactItem) = runBlocking {
        given().contentType(io.restassured.http.ContentType.JSON)
                .body(contact).When().post("/contactList").then().statusCode(201)
    }
}