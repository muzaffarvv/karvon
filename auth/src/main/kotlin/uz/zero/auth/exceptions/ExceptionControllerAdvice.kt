package uz.zero.auth.exceptions

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import uz.davrbank.auth.models.responses.BaseMessage
import uz.zero.auth.enums.ErrorCode
import java.util.stream.Collectors

@ControllerAdvice
class ExceptionControllerAdvice(
    private val source: ResourceBundleMessageSource
) {

    @ExceptionHandler(DavrException::class)
    fun handleDavrException(exception: DavrException): ResponseEntity<BaseMessage> {
        // Logda handleDavrException NOT_FOUND (404) qaytargan,
        // handleDBusinessException esa badRequest (400).
        // Odatda biznes xatolar uchun 400 ishlatiladi.
        return ResponseEntity.badRequest().body(exception.getErrorMessage(source))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<BaseMessage> {
        val fieldErrorList: MutableList<BaseMessage.ValidationFieldError> = ex.bindingResult.fieldErrors.stream()
            .map { error ->
                BaseMessage.ValidationFieldError(
                    error.field,
                    error.defaultMessage ?: "Validation error"
                )
            }
            .collect(Collectors.toList())

        return ResponseEntity(
            BaseMessage(
                ErrorCode.VALIDATION_ERROR.code,
                getErrorMessage(ErrorCode.VALIDATION_ERROR.name, null, source),
                fieldErrorList
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    fun getErrorMessage(
        errorCode: String,
        errorMessageArguments: Array<Any?>?,
        errorMessageSource: ResourceBundleMessageSource
    ): String? {
        return try {
            errorMessageSource.getMessage(errorCode, errorMessageArguments, LocaleContextHolder.getLocale())
        } catch (e: Exception) {
            e.message
        }
    }
}