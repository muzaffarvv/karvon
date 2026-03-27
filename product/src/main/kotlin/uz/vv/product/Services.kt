package uz.vv.product

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID


interface ProductService {
    fun create(request: ProductCreateRequest): ProductResponse
    fun update(id: UUID, request: ProductUpdateRequest): ProductResponse
    fun getById(id: UUID): ProductResponse
    fun getByCode(code: String): ProductResponse
    fun getAll(pageable: Pageable): Page<ProductResponse>
    fun getByCategoryId(categoryId: UUID): List<ProductResponse>
    fun delete(id: UUID)
}


@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val productMapper: ProductMapper
) : ProductService {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun create(request: ProductCreateRequest): ProductResponse {

        val category = categoryRepository.findByIdAndDeletedFalse(request.categoryId)
            ?: throw CategoryNotFoundException(request.categoryId)

        val product = productMapper.toEntity(request, category)
        val saved = productRepository.save(product)

        return productMapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: UUID, request: ProductUpdateRequest): ProductResponse {

        val product = productRepository.findByIdAndDeletedFalse(id)
            ?: throw ProductNotFoundException(id)

        val category = categoryRepository.findByIdAndDeletedFalse(request.categoryId)
            ?: throw CategoryNotFoundException(request.categoryId)

        val updated = productMapper.updateEntity(product, request, category)
        val saved = productRepository.save(updated)

        return productMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    override fun getById(id: UUID): ProductResponse {
        val product = productRepository.findByIdAndDeletedFalse(id)
            ?: throw ProductNotFoundException(id)
        return productMapper.toResponse(product)
    }

    @Transactional(readOnly = true)
    override fun getByCode(code: String): ProductResponse {
        val product = productRepository.findByCodeAndActiveTrueAndDeletedFalse(code)
            ?: throw ProductNotFoundException(code)
        return productMapper.toResponse(product)
    }

    @Transactional(readOnly = true)
    override fun getAll(pageable: Pageable): Page<ProductResponse> {
        return productRepository.findAllNotDeleted(pageable)
            .map { productMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getByCategoryId(categoryId: UUID): List<ProductResponse> {
        categoryRepository.findByIdAndDeletedFalse(categoryId)
            ?: throw CategoryNotFoundException(categoryId)

        return productRepository.findAllByCategoryIdAndDeletedFalse(categoryId)
            .map { productMapper.toResponse(it) }
    }

    @Transactional
    override fun delete(id: UUID) {
        productRepository.trash(id) ?: throw ProductNotFoundException(id)
    }
}


interface CategoryService {
    fun create(request: CategoryCreateRequest): CategoryResponse
    fun update(id: UUID, request: CategoryUpdateRequest): CategoryResponse
    fun getById(id: UUID): CategoryResponse
    fun getAll(): List<CategoryResponse>
    fun getChildren(parentId: UUID): List<CategoryResponse>
    fun getTree(): List<CategoryTreeResponse>
    fun delete(id: UUID)
}


@Service
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val categoryMapper: CategoryMapper
) : CategoryService {

    @Transactional
    override fun create(request: CategoryCreateRequest): CategoryResponse {

        if (categoryRepository.existsByNameAndDeletedFalse(request.name)) {
            throw CategoryAlreadyExistsException(request.name)
        }

        val parent = resolveParent(request.parentId, excludeId = null)

        val category = categoryMapper.toEntity(request, parent)
        val saved = categoryRepository.save(category)

        return categoryMapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: UUID, request: CategoryUpdateRequest): CategoryResponse {

        val category = categoryRepository.findByIdAndDeletedFalse(id)
            ?: throw CategoryNotFoundException(id)

        if (categoryRepository.existsByNameAndDeletedFalseAndIdNot(request.name, id)) {
            throw CategoryAlreadyExistsException(request.name)
        }

        if (request.parentId != null) {
            checkCircularReference(categoryId = id, newParentId = request.parentId)
        }

        val parent = resolveParent(request.parentId, excludeId = id)
        val updated = categoryMapper.updateEntity(category, request, parent)
        val saved = categoryRepository.save(updated)

        return categoryMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    override fun getById(id: UUID): CategoryResponse {
        val category = categoryRepository.findByIdAndDeletedFalse(id)
            ?: throw CategoryNotFoundException(id)
        return categoryMapper.toResponse(category)
    }

    @Transactional(readOnly = true)
    override fun getAll(): List<CategoryResponse> {
        return categoryRepository.findAllNotDeleted()
            .map { categoryMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getChildren(parentId: UUID): List<CategoryResponse> {
        categoryRepository.findByIdAndDeletedFalse(parentId)
            ?: throw CategoryNotFoundException(parentId)

        return categoryRepository.findAllNotDeletedWithParent()
            .filter { it.parent?.id == parentId }
            .map { categoryMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getTree(): List<CategoryTreeResponse> {
        val all = categoryRepository.findAllNotDeletedWithParent()

        val childrenMap = HashMap<UUID?, MutableList<Category>>(all.size * 2)
        for (cat in all) {
            val key = cat.parent?.id
            childrenMap.getOrPut(key) { mutableListOf() }.add(cat)
        }

        fun buildNode(cat: Category): CategoryTreeResponse {
            val kids = childrenMap[cat.id]
                ?.map { buildNode(it) }
                ?: emptyList()
            return CategoryTreeResponse(
                id = cat.id,
                name = cat.name,
                description = cat.description,
                active = cat.active,
                children = kids
            )
        }

        return childrenMap[null]
            ?.map { buildNode(it) }
            ?: emptyList()
    }

    @Transactional
    override fun delete(id: UUID) {

        if (!categoryRepository.existsByIdAndDeletedFalse(id)) {
            throw CategoryNotFoundException(id)
        }

        if (categoryRepository.existsByParentIdAndDeletedFalse(id)) {
            throw CategoryHasChildrenException(id)
        }

        if (productRepository.existsByCategoryIdAndDeletedFalse(id)) {
            throw CategoryHasProductsException(id)
        }

        categoryRepository.trash(id)
    }

    private fun resolveParent(parentId: UUID?, excludeId: UUID?): Category? {
        return parentId?.let {
            categoryRepository.findByIdAndDeletedFalse(it)
                ?: throw CategoryNotFoundException(it)
        }
    }

    private fun checkCircularReference(categoryId: UUID, newParentId: UUID) {
        var currentId: UUID? = newParentId
        val visited = mutableSetOf<UUID>()

        while (currentId != null) {
            if (currentId == categoryId) {
                throw CategoryCircularReferenceException(categoryId)
            }
            if (!visited.add(currentId)) break

            val current = categoryRepository.findByIdAndDeletedFalse(currentId) ?: break
            currentId = current.parent?.id
        }
    }
}
