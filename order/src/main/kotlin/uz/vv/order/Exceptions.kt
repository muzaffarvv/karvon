package uz.vv.order

import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.LoggerFactory
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant
import java.util.Locale
import java.util.UUID


@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseVO<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val errorCode: String? = null,
    val errors: Map<String, String>? = null,
    val timestamp: Instant = Instant.now()
) {
    companion object {
        fun <T> success(data: T?, message: String? = "Operation successful"): ResponseVO<T> =
            ResponseVO(success = true, message = message, data = data)

        fun <T> error(message: String?, errorCode: String? = null): ResponseVO<T> =
            ResponseVO(success = false, message = message, errorCode = errorCode)

        fun <T> validationError(errors: Map<String, String>): ResponseVO<T> =
            ResponseVO(
                success = false,
                message = "Validation failed",
                errorCode = ErrorCode.INVALID_REQUEST.name,
                errors = errors
            )
    }
}

data class BaseMessage(val code: Int? = null, val message: String? = null) {
    companion object {
        val OK = BaseMessage(0, "OK")
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


class OrderNotFoundException(id: UUID?) : KarvonException("Order not found with id: $id") {
    override fun errorType() = ErrorCode.ORDER_NOT_FOUND
}

class OrderAlreadyExistsException(orderNumber: String) :
    KarvonException("Order already exists with orderNumber: $orderNumber") {
    override fun errorType() = ErrorCode.ORDER_ALREADY_EXISTS
}

class OrderCannotBeModifiedException(id: UUID, status: OrderStatus) :
    KarvonException("Order [$id] cannot be modified because its status is: $status") {
    override fun errorType() = ErrorCode.ORDER_CANNOT_BE_MODIFIED
}

class OrderCannotBeCancelledException(id: UUID, status: OrderStatus) :
    KarvonException("Order [$id] cannot be cancelled because its status is: $status") {
    override fun errorType() = ErrorCode.ORDER_CANNOT_BE_CANCELLED
}


class OrderItemNotFoundException(id: UUID) : KarvonException("Order item not found with id: $id") {
    override fun errorType() = ErrorCode.ORDER_ITEM_NOT_FOUND
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class ProductServiceException(message: String) : RuntimeException(message)


@RestControllerAdvice
class GlobalExceptionHandler(private val messageSource: ResourceBundleMessageSource) {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(KarvonException::class)
    fun handleKarvonException(ex: KarvonException): ResponseEntity<ResponseVO<Any>> {
        log.warn("Business exception [{}]: {}", ex.errorType(), ex.message)
        return try {
            val baseMessage = ex.getErrorMessage(messageSource)
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseVO.error(message = baseMessage.message, errorCode = ex.errorType().name))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseVO.error(message = ex.message, errorCode = ex.errorType().name))
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ResponseVO<Any>> {
        val fieldErrors = ex.bindingResult.allErrors.associate { error ->
            val field = if (error is FieldError) error.field else error.objectName
            field to (error.defaultMessage ?: "Invalid value")
        }
        log.warn("Validation failed: {}", fieldErrors)
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ResponseVO.validationError(fieldErrors))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ResponseVO<Any>> {
        log.error("Unexpected error", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ResponseVO.error(message = "Internal server error"))
    }
}

