package uz.vv.product

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.hibernate.annotations.ColumnDefault
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
    @ColumnDefault("true")
    var active: Boolean = true,

    @Column(nullable = false)
    @ColumnDefault("false")
    var deleted: Boolean = false
)


@Entity
@Table(name = "products")
class Product(
    @Column(nullable = false, length = 72)
    var name: String,

    @Column(nullable = false, unique = true, length = 20)
    var code: String,

    @Column(length = 160)
    var description: String? = null,

    @Column(nullable = false)
    var price: BigDecimal,

    @Column(nullable = false)
    @ColumnDefault("0")
    var stockQuantity: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    var category: Category
) : BaseEntity()


@Entity
@Table(name = "categories")
class Category(
    @Column(nullable = false, length = 60, unique = true)
    var name: String,

    @Column(length = 100)
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null
) : BaseEntity()