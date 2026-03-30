package uz.vv.order

import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
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
    fun trash(id: UUID): T?
    fun trashList(ids: List<UUID>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, UUID>,
    entityManager: EntityManager,
) : SimpleJpaRepository<T, UUID>(entityInformation, entityManager), BaseRepository<T> {

    private val isNotDeletedSpecification = Specification<T> { root, _, cb ->
        cb.equal(root.get<Boolean>("deleted"), false)
    }

    override fun findByIdAndDeletedFalse(id: UUID): T? =
        findByIdOrNull(id)?.takeIf { !it.deleted }

    @Transactional
    override fun trash(id: UUID): T? = findByIdOrNull(id)?.apply {
        deleted = true
        save(this)
    }

    @Transactional
    override fun trashList(ids: List<UUID>): List<T?> = ids.map { trash(it) }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)

    override fun findAllNotDeleted(pageable: Pageable): Page<T> =
        findAll(isNotDeletedSpecification, pageable)
}


@Repository
interface OrderRepository : BaseRepository<Order> {

    fun findByOrderNumberAndDeletedFalse(orderNumber: String): Order?

    fun existsByOrderNumberAndDeletedFalse(orderNumber: String): Boolean

    fun findAllByStatusAndUserIdAndDeletedFalse(
        status: OrderStatus,
        userId: UUID,
        pageable: Pageable
    ): Page<Order>
}


@Repository
interface OrderItemRepository : BaseRepository<OrderItem> {

    fun findAllByOrderIdAndDeletedFalse(orderId: UUID, pageable: Pageable): Page<OrderItem>

    fun findAllByOrderIdAndDeletedFalse(orderId: UUID): List<OrderItem>

    fun deleteAllByOrderId(orderId: UUID)
}