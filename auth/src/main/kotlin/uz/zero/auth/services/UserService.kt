package uz.zero.auth.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.zero.auth.entities.BaseEntity
import uz.zero.auth.mappers.UserEntityMapper
import uz.zero.auth.repositories.BaseRepository
import uz.zero.auth.repositories.UserRepository
import uz.zero.auth.entities.User
import uz.zero.auth.enums.Operation
import uz.zero.auth.enums.Role
import uz.zero.auth.exceptions.InsufficientFundsException
import uz.zero.auth.exceptions.UserNotFoundException
import uz.zero.auth.exceptions.UsernameAlreadyTakenException
import uz.zero.auth.model.requests.UserCreateRequest
import uz.zero.auth.model.requests.UserUpdateRequest
import uz.zero.auth.model.responses.UserInfoResponse
import uz.zero.auth.model.responses.UserResponse
import uz.zero.auth.utils.userId
import java.math.BigDecimal

abstract class BaseService<T : BaseEntity, CreateDto, UpdateDto, ResponseDto>(
    protected val repository: BaseRepository<T>
) {

    protected fun getEntity(id: Long): T? = repository.findByIdAndDeletedFalse(id)

    fun findAll(): List<T> = repository.findAll()

    fun findAllNotDeleted(): List<T> = repository.findAllNotDeleted()

    @Transactional
    open fun update(id: Long, dto: UpdateDto): ResponseDto {
        val entity = getEntity(id) ?: throw RuntimeException("Entity not found with id: $id")
        val updatedEntity = updateEntity(dto, entity)
        val savedEntity = repository.save(updatedEntity)
        return toResponse(savedEntity)
    }

    @Transactional
    open fun softDelete(id: Long) {
        getEntity(id)?.let {
            it.deleted = true
            repository.save(it)
        }
    }

    protected abstract fun toEntity(dto: CreateDto): T
    protected abstract fun updateEntity(dto: UpdateDto, entity: T): T
    protected abstract fun toResponse(entity: T): ResponseDto
}

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserEntityMapper
) : BaseService<User, UserCreateRequest, UserUpdateRequest, UserResponse>(userRepository), AccountService {

    fun profile(): UserInfoResponse {
        val currentUserId = userId()
        val user = getUser(currentUserId)
        return userMapper.toUserInfo(user)
    }

    @Transactional
    fun create(request: UserCreateRequest): UserResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw UsernameAlreadyTakenException("Username ${request.username} band!")
        }
        val user = userMapper.toEntity(request, Role.USER)
        user.balance = BigDecimal("50.00")
        val savedUser = repository.save(user)
        return toResponse(savedUser)
    }

    override fun updateEntity(dto: UserUpdateRequest, entity: User): User {
        entity.fullName = dto.fullName
        entity.username = dto.username
        return entity
    }

    fun getAll(): List<UserResponse> = super.findAll().map { toResponse(it) }

    fun getAllNotDeleted(): List<UserResponse> = super.findAllNotDeleted().map { toResponse(it) }

    override fun toEntity(dto: UserCreateRequest): User = userMapper.toEntity(dto, Role.USER)

    override fun toResponse(entity: User) = userMapper.toResponse(entity)

    fun getUser(id: Long): User = super.getEntity(id) ?: throw UserNotFoundException("User not found with id: $id")

    fun getById(id: Long): UserResponse = toResponse(getUser(id))

    @Transactional
    override fun withdraw(userId: Long, amount: BigDecimal) = updateBalance(userId, amount, Operation.WITHDRAW)

    @Transactional
    override fun deposit(userId: Long, amount: BigDecimal) = updateBalance(userId, amount, Operation.DEPOSIT)

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
    }
}