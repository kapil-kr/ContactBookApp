import java.io.FileInputStream
import java.util.*

object AppConfiguration {
    private val properties = Properties()

    val host: String?
    val port: Int?
    val password: String

    init {
        val file =  FileInputStream(object {}.javaClass.getResource("application.properties").path)
        properties.load(file)
        host = properties.getProperty("server.host")
        port = properties.getProperty("server.port")?.toInt()
        password = properties.getProperty("server.auth.password")!!
    }
}
