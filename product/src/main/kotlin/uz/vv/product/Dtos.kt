package uz.vv.product

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ProductCreateRequest(
    @field:NotBlank(message = "Name cannot be empty")
    @field:Size(max = 72)
    val name: String,

    @field:Size(max = 160)
    val description: String?,

    @field:PositiveOrZero(message = "Price cannot be negative")
    val price: BigDecimal,

    @field:PositiveOrZero(message = "Stock quantity cannot be negative")
    val stockQuantity: Int,
)

data class ProductUpdateRequest(
    @field:NotBlank(message = "Name cannot be empty")
    @field:Size(max = 72)
    val name: String,

    @field:Size(max = 160)
    val description: String?,

    @field:PositiveOrZero(message = "Price cannot be negative")
    val price: BigDecimal,

    @field:PositiveOrZero(message = "Stock quantity cannot be negative")
    val stockQuantity: Int,

    val active: Boolean
)

data class ProductResponse(
    val id: UUID?,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val stockQuantity: Int,
    val active: Boolean,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val deleted: Boolean
)