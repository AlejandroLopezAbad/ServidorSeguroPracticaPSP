package server

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}
fun main():Unit = runBlocking {
    val properties = Properties()
    properties.load(javaClass.classLoader.getResourceAsStream("config.properties"))


    val port = properties.getProperty("port").toInt()











}