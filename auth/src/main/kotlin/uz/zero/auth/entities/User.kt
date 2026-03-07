package uz.zero.auth.entities

import jakarta.persistence.*
import jakarta.persistence.EnumType.STRING
import org.springframework.boot.context.properties.bind.DefaultValue
import uz.zero.auth.enums.Role
import uz.zero.auth.enums.UserStatus
import java.math.BigDecimal

@Entity(name = "users")
class User(
    @Column(length = 132)
    var fullName: String,

    @Column(length = 32, unique = true)
    var username: String,

    @Column(length = 255)
    var password: String,

    @Column(nullable = false)
    @DefaultValue("0.00")
    var balance: BigDecimal,

    @Enumerated(STRING)
    @Column(length = 32)
    var role: Role,

    @Enumerated(STRING)
    @Column(length = 32)
    var status: UserStatus = UserStatus.ACTIVE
) : BaseEntity()