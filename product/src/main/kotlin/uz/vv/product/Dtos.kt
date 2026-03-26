package uz.vv.product

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID


data class ProductCreateRequest(
    @field:NotBlank(message = "Name cannot be empty")
    @field:Size(max = 72, message = "Name must be less than 72 characters")
    val name: String,

    @field:Size(max = 160, message = "Description must be less than 160 characters")
    val description: String?,

    @field:PositiveOrZero(message = "Price cannot be negative")
    val price: BigDecimal,

    @field:PositiveOrZero(message = "Stock quantity cannot be negative")
    val stockQuantity: Int = 0,

    val categoryId: UUID
)

data class ProductUpdateRequest(
    @field:NotBlank(message = "Name cannot be empty")
    @field:Size(max = 72, message = "Name must be less than 72 characters")
    val name: String,

    @field:Size(max = 160, message = "Description must be less than 160 characters")
    val description: String?,

    @field:PositiveOrZero(message = "Price cannot be negative")
    val price: BigDecimal,

    @field:PositiveOrZero(message = "Stock quantity cannot be negative")
    val stockQuantity: Int,

    val active: Boolean,

    val categoryId: UUID
)

data class ProductResponse(
    val id: UUID?,
    val name: String,
    val code: String,
    val description: String?,
    val price: BigDecimal,
    val stockQuantity: Int,
    val active: Boolean,
    val categoryId: UUID?,
    val categoryName: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val deleted: Boolean
)


data class CategoryCreateRequest(
    @field:NotBlank(message = "Name cannot be empty")
    @field:Size(max = 60, message = "Name must be less than 60 characters")
    val name: String,

    @field:Size(max = 100, message = "Description must be less than 100 characters")
    val description: String?,

    val parentId: UUID? = null
)

data class CategoryUpdateRequest(
    @field:NotBlank(message = "Name cannot be empty")
    @field:Size(max = 60, message = "Name must be less than 60 characters")
    val name: String,

    @field:Size(max = 100, message = "Description must be less than 100 characters")
    val description: String?,

    val parentId: UUID? = null,

    val active: Boolean
)

data class CategoryResponse(
    val id: UUID?,
    val name: String,
    val description: String?,
    val active: Boolean,
    val parentId: UUID?,
    val parentName: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val deleted: Boolean
)


data class CategoryTreeResponse(
    val id: UUID?,
    val name: String,
    val description: String?,
    val active: Boolean,
    val children: List<CategoryTreeResponse> = emptyList()
)