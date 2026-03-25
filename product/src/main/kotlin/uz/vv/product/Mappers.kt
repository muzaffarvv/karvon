package uz.vv.product

import org.springframework.stereotype.Component

@Component
class ProductMapper(private val codeGenerator: CodeGenerator) {

    fun toEntity(request: ProductCreateRequest, category: Category): Product {
        return Product(
            name = request.name,
            code = codeGenerator.getNextPluCode(),
            description = request.description,
            price = request.price,
            stockQuantity = request.stockQuantity,
            category = category
        )
    }

    fun updateEntity(product: Product, request: ProductUpdateRequest, category: Category): Product {
        product.name = request.name
        product.description = request.description
        product.price = request.price
        product.stockQuantity = request.stockQuantity
        product.active = request.active
        product.category = category
        return product
    }

    fun toResponse(product: Product): ProductResponse {
        return ProductResponse(
            id = product.id,
            name = product.name,
            code = product.code,
            description = product.description,
            price = product.price,
            stockQuantity = product.stockQuantity,
            active = product.active,
            categoryId = product.category.id,
            categoryName = product.category.name,
            createdAt = product.createdAt,
            updatedAt = product.updatedAt,
            deleted = product.deleted
        )
    }
}


@Component
class CategoryMapper {
    fun toEntity(request: CategoryCreateRequest, parent: Category? = null): Category {
        return Category(
            name = request.name,
            description = request.description,
            parent = parent
        )
    }

    fun updateEntity(category: Category, request: CategoryUpdateRequest, parent: Category? = null): Category {
        category.name = request.name
        category.description = request.description
        category.parent = parent
        category.active = request.active
        return category
    }

    fun toResponse(category: Category): CategoryResponse {
        return CategoryResponse(
            id = category.id,
            name = category.name,
            description = category.description,
            active = category.active,
            parentId = category.parent?.id,
            parentName = category.parent?.name,
            createdAt = category.createdAt,
            updatedAt = category.updatedAt,
            deleted = category.deleted
        )
    }

}