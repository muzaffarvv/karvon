package uz.vv.order

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@FeignClient(
    name = "product-service",
    url = "\${services.product.url}"
)
interface ProductClient {

    @GetMapping("/products/{id}")
    fun getById(@PathVariable id: UUID): ResponseVO<ProductResponse>

    @GetMapping("/categories/{id}")
    fun getCategoryById(@PathVariable id: UUID): ResponseVO<CategoryResponse>
}