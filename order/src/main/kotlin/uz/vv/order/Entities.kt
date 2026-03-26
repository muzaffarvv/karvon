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
@Table(name = "orders", schema = "order")
class Order(
    @Column( nullable = false)
    var userId: UUID,

    @Column(nullable = false, unique = true)
    var orderNumber: String,

    @Column(nullable = false)
    var totalPrice: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderStatus = OrderStatus.PENDING
) : BaseEntity()


@Entity
@Table(name = "order_items", schema = "order")
class OrderItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    var order: Order,

    @Column(nullable = false)
    var productId: UUID,

    @Column(nullable = false)
    var quantity: BigDecimal,

    @Column(nullable = false)
    var unitPrice: BigDecimal,

    @Column(nullable = false)
    var totalPrice: BigDecimal
) : BaseEntity()
