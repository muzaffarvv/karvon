package uz.vv.order

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID


data class OrderCreateRequest(
    val userId: UUID,
    val orderNumber: String,
    val items: List<OrderItemCreateRequest>
)

data class OrderUpdateRequest(
    val status: OrderStatus? = null,
    val totalPrice: BigDecimal? = null
)

data class OrderResponse(
    val id: UUID,
    val userId: UUID,
    val orderNumber: String,
    val totalPrice: BigDecimal,
    val status: OrderStatus,
    val items: List<OrderItemShortResponse>,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val createdBy: UUID?,
    val updatedBy: UUID?
)

data class OrderShortResponse(
    val id: UUID,
    val orderNumber: String,
    val totalPrice: BigDecimal,
    val status: OrderStatus
)


data class OrderItemCreateRequest(
    val productId: UUID,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal
)

data class OrderItemUpdateRequest(
    val quantity: BigDecimal? = null,
    val unitPrice: BigDecimal? = null
)

data class OrderItemResponse(
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
