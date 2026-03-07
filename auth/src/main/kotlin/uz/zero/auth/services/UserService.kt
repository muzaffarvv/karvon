package uz.zero.auth.services

import uz.zero.auth.entities.User
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import uz.zero.auth.model.requests.UserCreateRequest
import uz.zero.auth.model.responses.UserInfoResponse
import uz.zero.auth.model.responses.UserResponse
import uz.zero.auth.enums.Operation
import uz.zero.auth.enums.Role
import uz.zero.auth.exceptions.InsufficientFundsException
import uz.zero.auth.exceptions.UserNotFoundException
import uz.zero.auth.exceptions.UsernameAlreadyTakenException
import uz.zero.auth.mappers.UserEntityMapper
import uz.zero.auth.model.requests.UserUpdateRequest
import uz.zero.auth.repositories.UserRepository
import uz.zero.auth.utils.userId
import java.math.BigDecimal

interface UserService : AccountService {
    fun profile(): UserInfoResponse
    fun create(request: UserCreateRequest): UserResponse
    fun update(id: Long, dto: UserUpdateRequest): UserResponse
    fun softDelete(id: Long)
    fun getAll(): List<UserResponse>
    fun getAllNotDeleted(): List<UserResponse>
    fun getUser(id: Long): User
    fun getById(id: Long): UserResponse
}


@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userMapper: UserEntityMapper
) : UserService {

    override fun profile(): UserInfoResponse {
        val currentUserId = userId()
        val user = getUser(currentUserId)
        return userMapper.toUserInfo(user)
    }

    @Transactional
    override fun create(request: UserCreateRequest): UserResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw UsernameAlreadyTakenException("Username ${request.username} band!")
        }
        val user = userMapper.toEntity(request, Role.USER)
        user.balance = BigDecimal("50.00")
        val savedUser = userRepository.save(user)
        return userMapper.toResponse(savedUser)
    }

    @Transactional
    override fun update(id: Long, dto: UserUpdateRequest): UserResponse {
        val user = getUser(id)
        user.fullName = dto.fullName
        user.username = dto.username
        val savedUser = userRepository.save(user)
        return userMapper.toResponse(savedUser)
    }

    @Transactional
    override fun softDelete(id: Long) {
        val user = getUser(id)
        user.deleted = true
        userRepository.save(user)
    }

    override fun getAll(): List<UserResponse> {
        return userRepository.findAll().map { userMapper.toResponse(it) }
    }

    override fun getAllNotDeleted(): List<UserResponse> {
        return userRepository.findAllNotDeleted().map { userMapper.toResponse(it) }
    }

    override fun getUser(id: Long): User {
        return userRepository.findByIdAndDeletedFalse(id)
            ?: throw UserNotFoundException("User not found with id: $id")
    }

    override fun getById(id: Long): UserResponse {
        return userMapper.toResponse(getUser(id))
    }

    @Transactional
    override fun withdraw(userId: Long, amount: BigDecimal) {
        updateBalance(userId, amount, Operation.WITHDRAW)
    }

    @Transactional
    override fun deposit(userId: Long, amount: BigDecimal) {
        updateBalance(userId, amount, Operation.DEPOSIT)
    }

    private fun updateBalance(userId: Long, amount: BigDecimal, operation: Operation) {
        val user = getUser(userId)

        when (operation) {
            Operation.DEPOSIT -> user.balance = user.balance.add(amount)
            Operation.WITHDRAW -> {
                if (user.balance < amount) {
                    throw InsufficientFundsException("Insufficient funds")
                }
                user.balance = user.balance.subtract(amount)
            }
        }
        userRepository.save(user)
    }
}