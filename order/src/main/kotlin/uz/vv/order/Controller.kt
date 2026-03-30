package uz.vv.order

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID


@RestController
@RequestMapping("/orders")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: OrderCreateRequest
    ): ResponseEntity<ResponseVO<OrderFullResponse>> {
        val response = orderService.create(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ResponseVO.success(response, "Order created successfully"))
    }

    @GetMapping
    fun getAll(
        @PageableDefault(size = 20, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<ResponseVO<Page<OrderShortResponse>>> {
        val response = orderService.getAll(pageable)
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID
    ): ResponseEntity<ResponseVO<OrderFullResponse>> {
        val response = orderService.getById(id)
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @GetMapping("/number/{orderNumber}")
    fun getByOrderNumber(
        @PathVariable orderNumber: String
    ): ResponseEntity<ResponseVO<OrderFullResponse>> {
        val response = orderService.getByOrderNumber(orderNumber)
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @GetMapping("/user/{userId}")
    fun getByStatusAndUserId(
        @PathVariable userId: UUID,
        @RequestParam status: OrderStatus,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ResponseVO<Page<OrderShortResponse>>> {
        val response = orderService.getByStatusAndUserId(status, userId, pageable)
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: OrderUpdateRequest
    ): ResponseEntity<ResponseVO<OrderFullResponse>> {
        val response = orderService.update(id, request)
        return ResponseEntity.ok(ResponseVO.success(response, "Order updated successfully"))
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: OrderStatusUpdateRequest
    ): ResponseEntity<ResponseVO<OrderFullResponse>> {
        val response = orderService.updateOrderStatus(id, request)
        return ResponseEntity.ok(ResponseVO.success(response, "Order status updated successfully"))
    }

    @DeleteMapping("/{id}/items")
    fun clearItems(
        @PathVariable id: UUID
    ): ResponseEntity<ResponseVO<OrderFullResponse>> {
        val response = orderService.clearItems(id)
        return ResponseEntity.ok(ResponseVO.success(response, "Order items cleared successfully"))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ): ResponseEntity<ResponseVO<OrderFullResponse>> {
        val response = orderService.delete(id)
        return ResponseEntity.ok(ResponseVO.success(response, "Order deleted successfully"))
    }
}


@RestController
@RequestMapping("/orders/{orderId}/items")
class OrderItemController(private val orderItemService: OrderItemService) {

    @PostMapping
    fun create(
        @PathVariable orderId: UUID,
        @Valid @RequestBody request: OrderItemCreateRequest
    ): ResponseEntity<ResponseVO<OrderItemFullResponse>> {
        val response = orderItemService.create(orderId, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ResponseVO.success(response, "Order item created successfully"))
    }

    @GetMapping
    fun getByOrderId(
        @PathVariable orderId: UUID,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ResponseVO<Page<OrderItemFullResponse>>> {
        val response = orderItemService.getByOrderId(orderId, pageable)
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable orderId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ResponseVO<OrderItemFullResponse>> {
        val response = orderItemService.getById(id)
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable orderId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: OrderItemUpdateRequest
    ): ResponseEntity<ResponseVO<OrderItemFullResponse>> {
        val response = orderItemService.update(id, request)
        return ResponseEntity.ok(ResponseVO.success(response, "Order item updated successfully"))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable orderId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ResponseVO<OrderItemFullResponse>> {
        val response = orderItemService.delete(id)
        return ResponseEntity.ok(ResponseVO.success(response, "Order item deleted successfully"))
    }
}