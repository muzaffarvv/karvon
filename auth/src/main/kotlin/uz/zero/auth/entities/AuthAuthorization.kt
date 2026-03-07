package uz.zero.auth.entities

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.security.oauth2.core.AuthorizationGrantType
import uz.zero.auth.utils.StringSetConverter
import java.time.Instant

@Entity
@Table(name = "auth_authorizations")
class AuthAuthorization(
    @Id var id: String,
    var registeredClientId: String,
    var principalName: String,
    var authorizationGrantType: AuthorizationGrantType,
    @Column(length = 1000)
    @Convert(converter = StringSetConverter::class)
    var authorizedScopes: MutableSet<String>,
    @Column(length = 4000)
    var attributes: String?,
    @Column(length = 4000)
    var accessTokenValue: String,
    var accessTokenIssuedAt: Instant?,
    var accessTokenExpiresAt: Instant?,
    @Column(length = 2000)
    var accessTokenMetadata: String?,
    @Column(length = 1000)
    @Convert(converter = StringSetConverter::class)
    var accessTokenScopes: Set<String>,
    @Column(length = 4000)
    var refreshTokenValue: String?,
    var refreshTokenIssuedAt: Instant?,
    var refreshTokenExpiresAt: Instant?,
    @Column(length = 2000)
    var refreshTokenMetadata: String?,
)