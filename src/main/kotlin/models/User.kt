package models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var name:String,
    var password:ByteArray,
    var rol:String
) {
}