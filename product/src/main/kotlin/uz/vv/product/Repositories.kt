package uz.vv.product

import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID


@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: UUID): T?
    fun trash(id: UUID): Boolean
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
}


class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, UUID>,
    private val entityManager: EntityManager,
) : SimpleJpaRepository<T, UUID>(entityInformation, entityManager), BaseRepository<T> {

    private val domainClass: Class<T> = entityInformation.javaType

    private val isNotDeletedSpecification = Specification<T> { root, _, cb ->
        cb.equal(root.get<Boolean>("deleted"), false)
    }

    override fun findByIdAndDeletedFalse(id: UUID): T? =
        findByIdOrNull(id)?.takeIf { !it.deleted }

    @Transactional
    override fun trash(id: UUID): Boolean {
        val updatedRows = entityManager.createQuery(
            "UPDATE ${domainClass.simpleName} e SET e.deleted = true WHERE e.id = :id AND e.deleted = false"
        )
            .setParameter("id", id)
            .executeUpdate()

        return updatedRows > 0
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)

    override fun findAllNotDeleted(pageable: Pageable): Page<T> =
        findAll(isNotDeletedSpecification, pageable)

    private fun findByIdOrNull(id: UUID): T? = findById(id).orElse(null)
}


@Repository
interface CategoryRepository : BaseRepository<Category> {
    fun existsByIdAndDeletedFalse(id: UUID): Boolean
    fun existsByNameAndDeletedFalse(name: String): Boolean
    fun existsByNameAndDeletedFalseAndIdNot(name: String, id: UUID): Boolean
    fun existsByParentIdAndDeletedFalse(parentId: UUID): Boolean

    @Query("""
        SELECT c FROM Category c
        LEFT JOIN FETCH c.parent
        WHERE c.deleted = false
        ORDER BY c.name
    """)
    fun findAllNotDeletedWithParent(): List<Category>
}


@Repository
interface ProductRepository : BaseRepository<Product> {
    fun findByCodeAndActiveTrueAndDeletedFalse(code: String): Product?
    fun existsByCategoryIdAndDeletedFalse(categoryId: UUID): Boolean
    fun findAllByCategoryIdAndDeletedFalse(categoryId: UUID): List<Product>
}