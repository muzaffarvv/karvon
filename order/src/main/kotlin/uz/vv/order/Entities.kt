package uz.vv.order

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    var id: UUID? = null,

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: Instant? = null,

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    var createdBy: UUID? = null,

    @LastModifiedBy
    @Column(name = "updated_by")
    var updatedBy: UUID? = null,

    @Column(nullable = false)
    @ColumnDefault("false")
    var deleted: Boolean = false
)


@Entity
@Table(name = "orders")
class Order(
    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Column(name = "total_price", nullable = false)
    var totalPrice: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<OrderItem> = mutableListOf()
) : BaseEntity()


@Entity
@Table(name = "order_items")
class OrderItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null,

    @Column(name = "product_id", nullable = false)
    var productId: UUID,

    @Column(nullable = false)
    var quantity: Int,

    @Column(name = "unit_price", nullable = false)
    var unitPrice: BigDecimal,

    @Column(name = "total_price", nullable = false)
    var totalPrice: BigDecimal
) : BaseEntity()

