package uz.zero.auth.model.requests

import jakarta.validation.constraints.Size
import uz.zero.auth.utils.NotSpace

data class UserUpdateRequest(
    @field:Size(max = 255)
    val fullName: String,

    @field:Size(min = 3, max = 32)
    @field:NotSpace
    val username: String,

    val roleId: Long
)