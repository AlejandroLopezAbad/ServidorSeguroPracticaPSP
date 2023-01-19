package client

import common.Request
import common.Response
import common.TypeRequestEnum
import common.TypeResponseEnum
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.UserDto
import mu.KotlinLogging
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlin.system.exitProcess

private var logger = KotlinLogging.logger {}
private var input: DataInputStream? = null
private var output: DataOutputStream? = null
private var server: SSLSocket? = null
private var properties = Properties()


fun main() = runBlocking {

    properties.load(javaClass.classLoader.getResourceAsStream("config.properties"))

    var json = Json
    var token: String? = null

    setConnection()

    var factory = SSLSocketFactory.getDefault() as SSLSocketFactory
    server = factory.createSocket("localhost", properties.getProperty("port").toInt()
    ) as SSLSocket


    logger.debug { "Protocolos soportados: ${server!!.supportedProtocols.contentToString()}" }
    server!!.enabledCipherSuites = arrayOf("TLS_AES_128_GCM_SHA256")
    server!!.enabledProtocols = arrayOf("TLSv1.3")

    //creamos la conexion

    println("✔ Cliente conectado correctamente.✔")
    input = DataInputStream(server!!.inputStream)
    output = DataOutputStream(server!!.outputStream)


    println("✔ Datos de la sesión✔")
    println(
        "--> Session: ${server?.session} \n" +
                "--> Session Created At: ${server?.session?.creationTime} \n" +
                "--> Session Port: ${server?.session?.peerPort} \n" +
                "--> Session Context: ${server?.session?.sessionContext} \n" +
                "--> Session Protocol: ${server?.session?.protocol}"
    )


    logger.debug { "Creamos y enviamos la peticion de login" }

    var login = UserDto("pepe", "pepe1234")
    var requestLog = Request<UserDto>(type = TypeRequestEnum.LOGIN, data = login)
    logger.debug { "Peticion enviada" }
    output?.writeUTF((json.encodeToString(requestLog)) + "\n")

    logger.debug { "Recibimos la respuesta del servidor" }
    var inputLog = input?.readUTF()
    var responseLog = json.decodeFromString<Response<String>>(inputLog!!)
    when (responseLog.type) {
        TypeResponseEnum.OK -> {
            token = responseLog.data //aqui me llega el token del servidor
            println("Token recibido: $token")
        }

        TypeResponseEnum.ERROR -> {
            error(responseLog.data)
        }
    }

    logger.debug { "Creamos la peticion de Time" }

    var requestTime = Request<String>(type = TypeRequestEnum.TIME, data = token!!)
    output?.writeUTF((json.encodeToString(requestTime)) + "\n")


    var inputTime = input?.readUTF()
    var responseTime = json.decodeFromString<Response<String>>(inputTime!!)
    when (responseTime.type) {
        TypeResponseEnum.ERROR -> error(responseTime.data)
        TypeResponseEnum.OK -> println("✅ Respuesta Recibida --> ${responseTime.data}")
    }


    close()
}


private fun setConnection() {
    println("Cliente")


    val file =
        System.getProperty("user.dir") + File.separator + "cert" + File.separator + "clave_cliente.p12"

    if (!Files.exists(Path.of(file))) {
        System.err.println(" ❌ No se encuentra el fichero de certificado del cliente ❌ ")
        exitProcess(0)
    }

    logger.debug { "Conectandose con el servidor....." }
    System.setProperty("javax.net.debug", "ssl, keymanager, handshake") // Debug
    System.setProperty("javax.net.ssl.trustStore", file)
    logger.debug { "Añadiendo la clave del cliente" }
    System.setProperty("javax.net.ssl.trustStorePassword", properties.getProperty("keyClient"))


}

fun close() {
    server!!.close()
    output!!.close()
    input!!.close()
}

fun error(error: String) {
    close()
    println("Ha ocurrido un error $error")
    exitProcess(0)
}


