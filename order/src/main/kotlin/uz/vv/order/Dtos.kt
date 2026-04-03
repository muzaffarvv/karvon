package uz.vv.order

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID


// ─── Order Request DTOs ────────────────────────────────────────────────────────

data class OrderCreateRequest(
    val userId: UUID,
    @field:NotBlank(message = "Order number cannot be blank")
    @field:PositiveOrZero(message = "Order number cannot be negative")
    val orderNumber: String,
    val items: List<OrderItemCreateRequest>
)

data class OrderUpdateRequest(
    val status: OrderStatus? = null,
    @field:PositiveOrZero(message = "Total price cannot be negative")
    val totalPrice: BigDecimal? = null
)

data class OrderStatusUpdateRequest(
    val status: OrderStatus
)


// ─── Order Response DTOs ───────────────────────────────────────────────────────

data class OrderFullResponse(
    val id: UUID,
    val userId: UUID,
    val orderNumber: String,
    val totalPrice: BigDecimal,
    val status: OrderStatus,
    val items: List<OrderItemShortResponse>,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val createdBy: UUID?,
    val updatedBy: UUID?,
)

data class OrderShortResponse(
    val id: UUID,
    val orderNumber: String,
    val totalPrice: BigDecimal,
    val status: OrderStatus
)


// ─── OrderItem Request DTOs ────────────────────────────────────────────────────

data class OrderItemCreateRequest(
    val productId: UUID,
    @field:PositiveOrZero(message = "Quantity cannot be negative")
    val quantity: BigDecimal,
    @field:PositiveOrZero(message = "Unit price cannot be negative")
    val unitPrice: BigDecimal
)

data class OrderItemUpdateRequest(
    @field:PositiveOrZero(message = "Quantity cannot be negative")
    val quantity: BigDecimal? = null,
    @field:PositiveOrZero(message = "Unit price cannot be negative")
    val unitPrice: BigDecimal? = null
)


// ─── OrderItem Response DTOs ───────────────────────────────────────────────────

data class OrderItemFullResponse(
    val id: UUID,
    val orderId: UUID,
    val productId: UUID,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val createdAt: Instant?,
    val updatedAt: Instant?
)

data class OrderItemShortResponse(
    val id: UUID,
    val productId: UUID,
    val quantity: BigDecimal,
    val totalPrice: BigDecimal
)


// ─── Product Response DTOs ────────────────────────────────────────────────────


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