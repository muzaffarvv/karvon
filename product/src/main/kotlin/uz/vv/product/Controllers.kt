package uz.vv.product

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID


@RestController
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: ProductCreateRequest
    ): ResponseEntity<ResponseVO<ProductResponse>> {
        val response = productService.create(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ResponseVO.success(response, "Product created successfully"))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ProductUpdateRequest
    ): ResponseEntity<ResponseVO<ProductResponse>> {
        val response = productService.update(id, request)
        return ResponseEntity.ok(ResponseVO.success(response, "Product updated successfully"))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID
    ): ResponseEntity<ResponseVO<ProductResponse>> {
        val response = productService.getById(id)
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @GetMapping
    fun getAll(
        @PageableDefault(size = 20, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<ResponseVO<Page<ProductResponse>>> {
        val response = productService.getAll(pageable)
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @GetMapping("/category/{categoryId}")
    fun getByCategoryId(
        @PathVariable categoryId: UUID
    ): ResponseEntity<ResponseVO<List<ProductResponse>>> {
        val response = productService.getByCategoryId(categoryId)
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID
    ): ResponseEntity<ResponseVO<Nothing>> {
        productService.delete(id)
        return ResponseEntity.ok(ResponseVO.success(null, "Product deleted successfully"))
    }
}


@RestController
@RequestMapping("/categories")
class CategoryController(private val categoryService: CategoryService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CategoryCreateRequest
    ): ResponseEntity<ResponseVO<CategoryResponse>> {
        val response = categoryService.create(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ResponseVO.success(response, "Category created successfully"))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CategoryUpdateRequest
    ): ResponseEntity<ResponseVO<CategoryResponse>> {
        val response = categoryService.update(id, request)
        return ResponseEntity.ok(ResponseVO.success(response, "Category updated successfully"))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID
    ): ResponseEntity<ResponseVO<CategoryResponse>> {
        val response = categoryService.getById(id)
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @GetMapping
    fun getAll(): ResponseEntity<ResponseVO<List<CategoryResponse>>> {
        val response = categoryService.getAll()
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @GetMapping("/{id}/children")
    fun getChildren(
        @PathVariable id: UUID
    ): ResponseEntity<ResponseVO<List<CategoryResponse>>> {
        val response = categoryService.getChildren(id)
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @GetMapping("/tree")
    fun getTree(): ResponseEntity<ResponseVO<List<CategoryTreeResponse>>> {
        val response = categoryService.getTree()
        return ResponseEntity.ok(ResponseVO.success(response))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID
    ): ResponseEntity<ResponseVO<Nothing>> {
        categoryService.delete(id)
        return ResponseEntity.ok(ResponseVO.success(null, "Category deleted successfully"))
    }
}