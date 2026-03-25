package uz.vv.product

enum class ErrorCode(val code: Int) {
    PRODUCT_NOT_FOUND(100),

    DATABASE_SEQUENCE_ERROR(999),
}