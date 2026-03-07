package uz.zero.auth.exceptions

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import uz.davrbank.auth.models.responses.BaseMessage
import uz.zero.auth.enums.ErrorCode

sealed class DavrException : RuntimeException() {

    abstract fun errorCode(): ErrorCode

    open fun getErrorMessageArguments(): Array<Any?>? = null

    fun getErrorMessage(errorMessageSource: ResourceBundleMessageSource): BaseMessage {
        val messageKey = errorCode().name
        val errorMessage = try {
            errorMessageSource.getMessage(messageKey, getErrorMessageArguments(), LocaleContextHolder.getLocale())
        } catch (e: Exception) {
            "Message not found for key: $messageKey"
        }
        return BaseMessage(errorCode().code, errorMessage)
    }
}
class UserNotFoundException(private vararg val args: Any?) : DavrException() {

    override fun errorCode(): ErrorCode = ErrorCode.USER_NOT_FOUND

    override fun getErrorMessageArguments(): Array<Any?> = args.toList().toTypedArray()
}

class UsernameAlreadyTakenException(private vararg val args: Any?) : DavrException() {
    override fun errorCode(): ErrorCode = ErrorCode.USERNAME_ALREADY_TAKEN

    override fun getErrorMessageArguments(): Array<Any?> = args.toList().toTypedArray()
}

class InsufficientFundsException(private vararg val args: Any?) : DavrException() {
    override fun errorCode(): ErrorCode = ErrorCode.INSUFFICIENT_FUNDS

    override fun getErrorMessageArguments(): Array<Any?> = args.toList().toTypedArray()
}