package models

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    var name:String,
    var password:String,
) {
}