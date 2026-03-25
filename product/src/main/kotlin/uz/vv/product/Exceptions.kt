package uz.vv.product

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseVO<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val errorCode: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T?, message: String? = "Operation successful"): ResponseVO<T> {
            return ResponseVO(
                success = true,
                message = message,
                data = data
            )
        }

        fun <T> error(message: String?, errorCode: String? = null): ResponseVO<T> {
            return ResponseVO(
                success = false,
                message = message,
                errorCode = errorCode
            )
        }
    }
}