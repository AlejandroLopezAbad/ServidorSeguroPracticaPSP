package server

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLServerSocketFactory
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}


fun main(): Unit = runBlocking {
    val properties = Properties()
    properties.load(javaClass.classLoader.getResourceAsStream("config.properties"))

    val port = properties.getProperty("port").toInt()

    println("🔵 Iniciando Servidor")

    logger.debug { "Consultando fichero llavero del Servidor" }

    val file = System.getProperty("user.dir") + File.separator + "cert" + File.separator + "clave_servidor.p12"
    if (!Files.exists(Path.of(file))) {
        System.err.println(" ❌ No se encuentra el fichero de certificado del servidor ❌ ")
        exitProcess(0)
    }

    logger.debug { "✔Cargando el fichero de propiedades" }

    System.getProperty("javax.net.ssl.keyStore", file)//Llavero
    System.getProperty("javax.net.ssl.keyStorePassword", properties.getProperty("keyServer").toString())

    //creamos el servidor de tipo SSL
    val serverFactory = SSLServerSocketFactory.getDefault() as SSLServerSocketFactory
    val serverSocket = serverFactory.createServerSocket(port) as SSLServerSocket



    while (true) {
        println("Servidor listo para recibir clientes ✔")

        val socket = serverSocket.accept()
        println("✳ Cliente conectado desde: ${socket.remoteSocketAddress}")

        thread {
            GestorClientes(socket).go()
        }


    }


}