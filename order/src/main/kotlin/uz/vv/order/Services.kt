package uz.vv.order

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class ProductClientService(
    private val productClient: ProductClient
) {

    fun getProductOrThrow(productId: UUID): ProductResponse {
        val response = productClient.getById(productId)

        if (!response.success) {
            val errorMsg = response.message ?: "External API error"
            throw ProductServiceException("$errorMsg (Code: ${response.errorCode})")
        }

        return response.data
            ?: throw ProductServiceException("Product not found with id: $productId")
    }

    fun validateProductActive(productId: UUID): ProductResponse {
        val product = getProductOrThrow(productId)
        if (!product.active) {
            throw ProductServiceException("Product is not active: $productId")
        }
        if (product.stockQuantity <= 0) {
            throw ProductServiceException("Product out of stock: $productId")
        }
        return product
    }
}


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
    private val orderMapper: OrderMapper,
    private val orderItemMapper: OrderItemMapper
) : OrderService {

    @Transactional
    override fun create(createDto: OrderCreateRequest): OrderFullResponse {
        if (orderRepository.existsByOrderNumberAndDeletedFalse(createDto.orderNumber)) {
            throw OrderAlreadyExistsException(createDto.orderNumber)
        }

        val order = orderMapper.toEntity(createDto)
        orderRepository.save(order)

        val items = createDto.items.map { itemRequest ->
            val item = orderItemMapper.toEntity(itemRequest, order)
            orderItemRepository.save(item)
        }

        order.totalPrice = items.sumOf { it.totalPrice }
        orderRepository.save(order)

        return orderMapper.toFullResponse(order, items)
    }

    @Transactional
    override fun update(id: UUID, updateDto: OrderUpdateRequest): OrderFullResponse {
        val order = getOrderOrThrow(id)
        checkOrderIsModifiable(order)

        orderMapper.updateEntity(order, updateDto)
        orderRepository.save(order)

        val items = orderItemRepository.findAllByOrderIdAndDeletedFalse(order.id!!)
        return orderMapper.toFullResponse(order, items)
    }

    /**
     * Faqat status ni yangilaydi.
     *   PENDING     -> PROCESSING | CANCELLED
     *   PROCESSING  -> SHIPPED    | CANCELLED
     *   SHIPPED     -> DELIVERED
     *   DELIVERED   -> (terminal, o'zgartirib bo'lmaydi)
     *   CANCELLED   -> (terminal, o'zgartirib bo'lmaydi)
     */
    @Transactional
    override fun updateOrderStatus(id: UUID, statusUpdateDto: OrderStatusUpdateRequest): OrderFullResponse {
        val order = getOrderOrThrow(id)
        validateStatusTransition(order, statusUpdateDto.status)

        order.status = statusUpdateDto.status
        orderRepository.save(order)

        val items = orderItemRepository.findAllByOrderIdAndDeletedFalse(order.id!!)
        return orderMapper.toFullResponse(order, items)
    }

    @Transactional(readOnly = true)
    override fun getById(id: UUID): OrderFullResponse {
        val order = getOrderOrThrow(id)
        val items = orderItemRepository.findAllByOrderIdAndDeletedFalse(order.id!!)
        return orderMapper.toFullResponse(order, items)
    }

    @Transactional(readOnly = true)
    override fun getByOrderNumber(orderNumber: String): OrderFullResponse {
        val order = orderRepository.findByOrderNumberAndDeletedFalse(orderNumber)
            ?: throw OrderNotFoundException(null)
        val items = orderItemRepository.findAllByOrderIdAndDeletedFalse(order.id!!)
        return orderMapper.toFullResponse(order, items)
    }

    @Transactional(readOnly = true)
    override fun getByStatusAndUserId(
        status: OrderStatus,
        userId: UUID,
        pageable: Pageable
    ): Page<OrderShortResponse> {
        return orderRepository
            .findAllByStatusAndUserIdAndDeletedFalse(status, userId, pageable)
            .map { orderMapper.toShortResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getAll(pageable: Pageable): Page<OrderShortResponse> {
        return orderRepository
            .findAllNotDeleted(pageable)
            .map { orderMapper.toShortResponse(it) }
    }


    @Transactional
    override fun clearItems(orderId: UUID): OrderFullResponse {
        val order = getOrderOrThrow(orderId)
        checkOrderIsModifiable(order)

        val items = orderItemRepository.findAllByOrderIdAndDeletedFalse(order.id!!)
        items.forEach { item ->
            item.deleted = true
            orderItemRepository.save(item)
        }

        order.totalPrice = BigDecimal.ZERO
        orderRepository.save(order)

        return orderMapper.toFullResponse(order, emptyList())
    }

    @Transactional
    override fun delete(id: UUID): OrderFullResponse {
        val order = getOrderOrThrow(id)

        val items = orderItemRepository.findAllByOrderIdAndDeletedFalse(order.id!!)
        val response = orderMapper.toFullResponse(order, items)

        items.forEach { item ->
            item.deleted = true
            orderItemRepository.save(item)
        }

        order.deleted = true
        orderRepository.save(order)

        return response
    }


    private fun getOrderOrThrow(id: UUID): Order =
        orderRepository.findByIdAndDeletedFalse(id) ?: throw OrderNotFoundException(id)


    private fun validateStatusTransition(order: Order, newStatus: OrderStatus) {
        val allowedTransitions = mapOf(
            OrderStatus.PENDING     to setOf(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderStatus.PROCESSING  to setOf(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED     to setOf(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED   to emptySet(),
            OrderStatus.CANCELLED   to emptySet()
        )

        val allowed = allowedTransitions[order.status] ?: emptySet()
        if (newStatus !in allowed) {
            if (order.status == OrderStatus.DELIVERED || order.status == OrderStatus.CANCELLED) {
                throw OrderCannotBeModifiedException(order.id!!, order.status)
            }
            throw OrderCannotBeModifiedException(order.id!!, order.status)
        }
    }

    private fun checkOrderIsModifiable(order: Order) {
        if (order.status == OrderStatus.DELIVERED || order.status == OrderStatus.CANCELLED) {
            throw OrderCannotBeModifiedException(order.id!!, order.status)
        }
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
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderItemMapper: OrderItemMapper
) : OrderItemService {


    @Transactional
    override fun create(orderId: UUID, createDto: OrderItemCreateRequest): OrderItemFullResponse {
        val order = getOrderOrThrow(orderId)
        checkOrderIsModifiable(order)

        val item = orderItemMapper.toEntity(createDto, order)
        orderItemRepository.save(item)

        recalculateOrderTotal(order)

        return orderItemMapper.toFullResponse(item)
    }


    @Transactional
    override fun update(id: UUID, updateDto: OrderItemUpdateRequest): OrderItemFullResponse {
        val item = getOrderItemOrThrow(id)
        checkOrderIsModifiable(item.order)

        orderItemMapper.updateEntity(item, updateDto)
        orderItemRepository.save(item)

        recalculateOrderTotal(item.order)

        return orderItemMapper.toFullResponse(item)
    }

    @Transactional(readOnly = true)
    override fun getById(id: UUID): OrderItemFullResponse {
        val item = getOrderItemOrThrow(id)
        return orderItemMapper.toFullResponse(item)
    }

    @Transactional(readOnly = true)
    override fun getByOrderId(orderId: UUID, pageable: Pageable): Page<OrderItemFullResponse> {
        getOrderOrThrow(orderId)
        return orderItemRepository
            .findAllByOrderIdAndDeletedFalse(orderId, pageable)
            .map { orderItemMapper.toFullResponse(it) }
    }


    @Transactional
    override fun delete(id: UUID): OrderItemFullResponse {
        val item = getOrderItemOrThrow(id)
        checkOrderIsModifiable(item.order)

        val response = orderItemMapper.toFullResponse(item)

        item.deleted = true
        orderItemRepository.save(item)

        recalculateOrderTotal(item.order)

        return response
    }


    private fun getOrderItemOrThrow(id: UUID): OrderItem =
        orderItemRepository.findByIdAndDeletedFalse(id) ?: throw OrderItemNotFoundException(id)

    private fun getOrderOrThrow(orderId: UUID): Order =
        orderRepository.findByIdAndDeletedFalse(orderId) ?: throw OrderNotFoundException(orderId)

    private fun checkOrderIsModifiable(order: Order) {
        if (order.status == OrderStatus.DELIVERED || order.status == OrderStatus.CANCELLED) {
            throw OrderCannotBeModifiedException(order.id!!, order.status)
        }
    }

    private fun recalculateOrderTotal(order: Order) {
        val items = orderItemRepository.findAllByOrderIdAndDeletedFalse(order.id!!)
        order.totalPrice = items.sumOf { it.totalPrice }
        orderRepository.save(order)
    }
}