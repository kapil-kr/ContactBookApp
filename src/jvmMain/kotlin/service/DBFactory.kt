package service

import ContactItem
import com.mongodb.ConnectionString
import kotlinx.coroutines.runBlocking
import model.Contacts
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object DBFactory {
    val connectionString = System.getenv("MONGODB_URI")?.let {
        ConnectionString("$it?retryWrites=false")
    }

    fun getCollection() : CoroutineCollection<Contacts> {
        val client = if (connectionString != null) KMongo.createClient(connectionString).coroutine
        else
            KMongo.createClient().coroutine
        val database = client.getDatabase(connectionString?.database ?: "contactBook")
        val collection = database.getCollection<Contacts>()
        runBlocking {
            collection.ensureUniqueIndex(ContactItem::email)
        }
        return collection
    }

    suspend fun dropCollection(collection: CoroutineCollection<Contacts>) {
        collection.drop()
    }
}