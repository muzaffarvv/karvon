package uz.vv.order


enum class OrderStatus {
    PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}

enum class ErrorCode(val code: Int) {
    ORDER_NOT_FOUND(101),

    ORDER_ITEM_NOT_FOUND(103),

    INVALID_REQUEST(301),


}