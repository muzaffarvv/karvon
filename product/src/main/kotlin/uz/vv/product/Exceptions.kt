package uz.vv.product

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant
import java.util.Locale

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseVO<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val errorCode: String? = null,
    val timestamp: Instant = Instant.now()
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

@RestControllerAdvice
class GlobalExceptionHandler(private val messageSource: ResourceBundleMessageSource) {

    @ExceptionHandler(KarvonException::class)
    fun handleKarvonException(ex: KarvonException): ResponseEntity<ResponseVO<Any>> {
        val baseMessage = ex.getErrorMessage(messageSource)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResponseVO.error(
                message = baseMessage.message,
                errorCode = ex.errorType().name
            ))
    }
}

data class BaseMessage(val code: Int? = null, val message: String? = null) {
    companion object {
        var OK = BaseMessage(0, "OK")
    }
}

sealed class KarvonException(message: String? = null) : RuntimeException(message) {
    abstract fun errorType(): ErrorCode
    open fun getErrorMessageArguments(): Array<Any?>? = null
    fun getErrorMessage(errorMessageSource: ResourceBundleMessageSource): BaseMessage {
        return BaseMessage(
            errorType().code,
            errorMessageSource.getMessage(
                errorType().toString(),
                getErrorMessageArguments(),
                Locale(LocaleContextHolder.getLocale().language)
            )
        )
    }
}

class DatabaseSequenceException(val sequenceName: String) : KarvonException("Sequence error: $sequenceName") {
    override fun errorType(): ErrorCode = ErrorCode.DATABASE_SEQUENCE_ERROR
    override fun getErrorMessageArguments(): Array<Any?> = arrayOf(sequenceName)
}

