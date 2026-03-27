package uz.vv.order

import org.springframework.stereotype.Component

@Component
class OrderMapper(private val orderItemMapper: OrderItemMapper) {

    fun toEntity(request: CreateOrderRequest): Order {
        return Order(
            userId = request.userId,
            orderNumber = request.orderNumber
        )
    }

    fun updateEntity(order: Order, request: UpdateOrderRequest): Order {
        request.status?.let { order.status = it }
        request.totalPrice?.let { order.totalPrice = it }
        return order
    }

    fun toResponse(order: Order, items: List<OrderItem>): OrderResponse {
        return OrderResponse(
            id = order.id!!,
            userId = order.userId,
            orderNumber = order.orderNumber,
            totalPrice = order.totalPrice,
            status = order.status,
            items = items.map { orderItemMapper.toShortResponse(it) },
            createdAt = order.createdAt,
            updatedAt = order.updatedAt,
            createdBy = order.createdBy,
            updatedBy = order.updatedBy
        )
    }

    fun toShortResponse(order: Order): OrderShortResponse {
        return OrderShortResponse(
            id = order.id!!,
            orderNumber = order.orderNumber,
            totalPrice = order.totalPrice,
            status = order.status
        )
    }
}

@Component
class OrderItemMapper {

    fun toEntity(request: CreateOrderItemRequest, order: Order): OrderItem {
        return OrderItem(
            order = order,
            productId = request.productId,
            quantity = request.quantity,
            unitPrice = request.unitPrice,
            totalPrice = request.quantity * request.unitPrice
        )
    }

    fun updateEntity(orderItem: OrderItem, request: UpdateOrderItemRequest): OrderItem {
        request.quantity?.let { orderItem.quantity = it }
        request.unitPrice?.let { orderItem.unitPrice = it }
        orderItem.totalPrice = orderItem.quantity * orderItem.unitPrice
        return orderItem
    }

    fun toResponse(orderItem: OrderItem): OrderItemResponse {
        return OrderItemResponse(
            id = orderItem.id!!,
            orderId = orderItem.order.id!!,
            productId = orderItem.productId,
            quantity = orderItem.quantity,
            unitPrice = orderItem.unitPrice,
            totalPrice = orderItem.totalPrice,
            createdAt = orderItem.createdAt,
            updatedAt = orderItem.updatedAt
        )
    }

    fun toShortResponse(orderItem: OrderItem): OrderItemShortResponse {
        return OrderItemShortResponse(
            id = orderItem.id!!,
            productId = orderItem.productId,
            quantity = orderItem.quantity,
            totalPrice = orderItem.totalPrice
        )
    }
}