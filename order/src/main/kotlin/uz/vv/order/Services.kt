package uz.vv.order

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

interface OrderService {
    fun create(createDto: OrderCreateRequest): OrderFullResponse
    fun update(id: UUID, updateDto: OrderUpdateRequest): OrderFullResponse
    fun updateOrderStatus(id: UUID, statusUpdateDto: OrderStatusUpdateRequest): OrderFullResponse
    fun getById(id: UUID): OrderFullResponse
    fun getByOrderNumber(orderNumber: String): OrderFullResponse
    fun getByStatusAndUserId(status: OrderStatus, userId: UUID, pageable: Pageable): Page<OrderShortResponse>
    fun getAll(pageable: Pageable): Page<OrderShortResponse>
    fun clearItems(orderId: UUID): OrderFullResponse
    fun delete(id: UUID): OrderFullResponse
}


@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderMapper: OrderMapper
) : OrderService {

    override fun create(createDto: OrderCreateRequest): OrderFullResponse {
        TODO("Not yet implemented")
    }

    override fun update(id: UUID, updateDto: OrderUpdateRequest): OrderFullResponse {
        TODO("Not yet implemented")
    }

    override fun updateOrderStatus(id: UUID, statusUpdateDto: OrderStatusUpdateRequest): OrderFullResponse {
        TODO("Not yet implemented")
    }

    override fun getById(id: UUID): OrderFullResponse {
        TODO("Not yet implemented")
    }

    override fun getByOrderNumber(orderNumber: String): OrderFullResponse {
        TODO("Not yet implemented")
    }

    override fun getByStatusAndUserId(status: OrderStatus, userId: UUID, pageable: Pageable): Page<OrderShortResponse> {
        TODO("Not yet implemented")
    }

    override fun getAll(pageable: Pageable): Page<OrderShortResponse> {
        TODO("Not yet implemented")
    }

    override fun clearItems(orderId: UUID): OrderFullResponse {
        TODO("Not yet implemented")
    }

    override fun delete(id: UUID): OrderFullResponse {
        TODO("Not yet implemented")
    }

    private fun getByIdOrThrow(id: UUID) {
        orderRepository.findByIdAndDeletedFalse(id) ?: throw OrderNotFoundException(id)
    }
}


interface OrderItemService {
    fun create(orderId: UUID, createDto: OrderItemCreateRequest): OrderItemFullResponse
    fun update(id: UUID, updateDto: OrderItemUpdateRequest): OrderItemFullResponse
    fun getById(id: UUID): OrderItemFullResponse
    fun getByOrderId(orderId: UUID, pageable: Pageable): Page<OrderItemFullResponse>
    fun delete(id: UUID): OrderItemFullResponse
}

@Service
class OrderItemServiceImpl(
    private val orderItemRepository: OrderItemRepository,
    private val orderItemMapper: OrderItemMapper
) : OrderItemService {

    override fun create(
        orderId: UUID,
        createDto: OrderItemCreateRequest
    ): OrderItemFullResponse {
        TODO("Not yet implemented")
    }

    override fun update(
        id: UUID,
        updateDto: OrderItemUpdateRequest
    ): OrderItemFullResponse {
        TODO("Not yet implemented")
    }

    override fun getById(id: UUID): OrderItemFullResponse {
        TODO("Not yet implemented")
    }

    override fun getByOrderId(
        orderId: UUID,
        pageable: Pageable
    ): Page<OrderItemFullResponse> {
        TODO("Not yet implemented")
    }

    override fun delete(id: UUID): OrderItemFullResponse {
        TODO("Not yet implemented")
    }

    private fun getByIdOrThrow(id: UUID) {
        orderItemRepository.findByIdAndDeletedFalse(id) ?: throw OrderItemNotFoundException(id)
    }
}