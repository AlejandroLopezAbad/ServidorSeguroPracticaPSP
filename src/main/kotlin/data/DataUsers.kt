package data

import at.favre.lib.crypto.bcrypt.BCrypt
import models.User
import java.nio.charset.StandardCharsets

private val ROUNDS=12
class DataUsers {
var lista:MutableList<User> = mutableListOf()

    init {
        lista.addAll(listOf(
            User("pepe",BCrypt.withDefaults().hash(ROUNDS,"pepe1234".toByteArray(StandardCharsets.UTF_8)),"admin"),
            User("alexitto",BCrypt.withDefaults().hash(ROUNDS,"alexitto1234".toByteArray(StandardCharsets.UTF_8)),"user"),
            User("jose luis",BCrypt.withDefaults().hash(ROUNDS,"pspMola1234".toByteArray(StandardCharsets.UTF_8)),"admin"),
            User("card",BCrypt.withDefaults().hash(ROUNDS,"csgo1234".toByteArray(StandardCharsets.UTF_8)),"user")
        ))
    }

}