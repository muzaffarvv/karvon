package uz.vv.order

import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID


data class OrderCreateRequest(
    val userId: UUID,
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

data class OrderFullResponse(
    val id: UUID,
    val userId: UUID,
    val orderNumber: String,
    val totalPrice: BigDecimal,
    val status: OrderStatus,
    val items: List<OrderItemShortResponse>,
    val createdAt: Instant?,
)

data class OrderShortResponse(
    val id: UUID,
    val orderNumber: String,
    val totalPrice: BigDecimal,
    val status: OrderStatus
)


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
