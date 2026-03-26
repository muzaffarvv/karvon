package uz.vv.product

import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.LoggerFactory
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
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


class ProductNotFoundException(id: Any) : KarvonException("Product not found with id: $id") {
    override fun errorType() = ErrorCode.PRODUCT_NOT_FOUND
}

class ProductAlreadyExistsException(field: String, value: Any) :
    KarvonException("Product already exists with $field: $value") {
    override fun errorType() = ErrorCode.PRODUCT_ALREADY_EXISTS
}


class CategoryNotFoundException(id: Any) : KarvonException("Category not found with id: $id") {
    override fun errorType() = ErrorCode.CATEGORY_NOT_FOUND
}

class CategoryAlreadyExistsException(name: String) : KarvonException("Category already exists with name: $name") {
    override fun errorType() = ErrorCode.CATEGORY_ALREADY_EXISTS
}

class CategoryHasChildrenException(id: Any) :
    KarvonException("Category has children and cannot be deleted: $id") {
    override fun errorType() = ErrorCode.CATEGORY_HAS_CHILDREN
}

class CategoryHasProductsException(id: Any) :
    KarvonException("Category has products and cannot be deleted: $id") {
    override fun errorType() = ErrorCode.CATEGORY_HAS_PRODUCTS
}

class CategoryCircularReferenceException(id: Any) :
    KarvonException("Setting parent would create a circular reference for category: $id") {
    override fun errorType() = ErrorCode.CATEGORY_CIRCULAR_REFERENCE
}

class DatabaseSequenceException(val sequenceName: String) :
    KarvonException("Sequence error: $sequenceName") {
    override fun errorType() = ErrorCode.DATABASE_SEQUENCE_ERROR
    override fun getErrorMessageArguments(): Array<Any?> = arrayOf(sequenceName)
}


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