package uz.zero.auth.controllers

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import uz.zero.auth.model.requests.UserCreateRequest
import uz.zero.auth.model.requests.UserUpdateRequest
import uz.zero.auth.model.responses.UserInfoResponse
import uz.zero.auth.model.responses.UserResponse
import uz.zero.auth.services.UserService
import java.math.BigDecimal

@RestController
@RequestMapping("user")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(@RequestBody @Valid request: UserCreateRequest): UserResponse = userService.create(request)

    @GetMapping("/me")
    fun userMe(): UserInfoResponse = userService.profile()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): UserResponse = userService.getById(id)

    @GetMapping("/list")
    fun getAllNotDeleted(): List<UserResponse> = userService.getAll()

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody @Valid request: UserUpdateRequest
    ): UserResponse = userService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable id: Long) = userService.softDelete(id)

    @PostMapping("/{id}/deposit")
    fun deposit(
        @PathVariable id: Long,
        @RequestParam amount: BigDecimal
    ) = userService.deposit(id, amount)

    @PostMapping("/{id}/withdraw")
    fun withdraw(
        @PathVariable id: Long,
        @RequestParam amount: BigDecimal
    ) = userService.withdraw(id, amount)
}