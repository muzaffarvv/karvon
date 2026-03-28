package uz.vv.order

interface OrderService {
    fun create(createDto : OrderCreateRequest)
}