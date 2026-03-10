package uz.vv.product

import org.springframework.stereotype.Component

@Component
class ProductMapper {

    fun toEntity(request: ProductCreateRequest): Product {
        return Product(
            name = request.name,
            description = request.description,
            price = request.price,
            stockQuantity = request.stockQuantity
        )
    }

    fun toResponse(product: Product): ProductResponse {
        return ProductResponse(
            id = product.id,
            name = product.name,
            description = product.description,
            price = product.price,
            stockQuantity = product.stockQuantity,
            active = product.active,
            createdAt = product.createdAt,
            updatedAt = product.updatedAt,
            deleted = product.deleted
        )
    }

    fun update(product: Product, request: ProductUpdateRequest): Product {
        product.name = request.name
        product.description = request.description
        product.price = request.price
        product.stockQuantity = request.stockQuantity
        product.active = request.active
        return product
    }
}