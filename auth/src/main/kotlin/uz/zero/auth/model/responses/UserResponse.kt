package uz.zero.auth.model.responses

import uz.zero.auth.enums.UserStatus
import java.math.BigDecimal

data class UserResponse(
    val id: Long,
    val fullName: String,
    val username: String,
    val balance: BigDecimal,
    val role: String,
    val status: UserStatus
)