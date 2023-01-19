package server

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import common.Request
import common.Response
import common.TypeResponseEnum
import data.DataUsers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.User
import models.UserDto
import mu.KotlinLogging
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*


private var logger = KotlinLogging.logger {}

class GestorClientes(private val client: Socket) {

    private var input: DataInputStream? = null
    private var output: DataOutputStream? = null
    private var aceptado = true
    private var listUser = DataUsers().lista
    private var json = Json

    //nos creamos un algoritmo para sha-512
    private var algorithm = Algorithm.HMAC256("Algoritmoporfavorqueelexamenseafacil")
    private var verifier = JWT.require(algorithm).build()


    fun go() {
        input = DataInputStream(client.inputStream)
        output = DataOutputStream(client.outputStream)

        logger.debug { "Recibiendo los datos de login" }
        var login = input?.readUTF()
        var requestLogin = json.decodeFromString<Request<UserDto>>(login!!)
        //metodo de respuesta al login ya que estamos en el lado servidor
        responseLogin(requestLogin.data)
        logger.debug { "Datos de login enviados ➡" }

        if (aceptado) {
            var inputTiempo = input?.readUTF()
            var requestTiempo = json.decodeFromString<Request<String>>(inputTiempo!!)
            checkToken(requestTiempo.data)
            logger.debug { "Enviado la respuesta con la hora ⏳ " }
            cerrarConexion()
        }


    }


    private fun responseLogin(user: UserDto) {
        var pepe = listUser.firstOrNull { it.name == user.name }
        if (pepe == null){
           println("Usuario o contraseña incorrecto")
        }else {
            if (BCrypt.verifyer().verify(user.password.toByteArray(StandardCharsets.UTF_8), pepe.password).verified) {
                createToken(pepe)
            } else {
                println("Usuario o contraseña incorrecto")
            }
        }
    }

    //metodo que crea el token de JWT y lo envia
    private fun createToken(usuario: User) {
        val token = JWT.create()
            .withIssuer("Servidor de Byalexitto")
            .withSubject("Programacion de Servicios y Procesos")
            .withClaim("name", usuario.name)
            .withClaim("rol", usuario.rol)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + 50000L ))//1000000L / 1000 % 60
            .withJWTId(UUID.randomUUID().toString())
            .sign(algorithm)

        var response = Response<String>(TypeResponseEnum.OK, token)
        output?.writeUTF((json.encodeToString(response)) + "\n")
    }


    private fun checkToken(token: String) {
        var decode = verifier.verify(token)
        if (decode.expiresAt > Date(System.currentTimeMillis())) {
            if (decode.getClaim("rol").asString() == "admin") {
                var sendTiempo = Response<String>(TypeResponseEnum.OK, LocalDateTime.now().toString())

                output?.writeUTF((json.encodeToString(sendTiempo))+"\n")

            }
        } else {
            var error = Response<String>(
                TypeResponseEnum.ERROR,
                "Su token ha caducado o no tienes permisos para realizar esta accion"
            )
        }
    }

    private fun cerrarConexion() {

        output!!.close()
        input!!.close()
        client!!.close()
    }


}