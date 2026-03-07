package uz.zero.auth.entities

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import uz.zero.auth.utils.StringSetConverter
import java.time.Instant

@Entity
@Table(name = "auth_clients")
class AuthClient(
    @Id var id: String,
    @Column(unique = true)
    var clientId: String,
    var clientIdIssuedAt: Instant?,
    var clientSecret: String?,
    var clientSecretExpiresAt: Instant? = null,
    var clientName: String? = null,
    @Column(length = 1000)
    @Convert(converter = StringSetConverter::class)
    var clientAuthenticationMethods: Set<String>,
    @Column(length = 1000)
    @Convert(converter = StringSetConverter::class)
    var authorizationGrantTypes: Set<String>,
    @Column(length = 1000)
    @Convert(converter = StringSetConverter::class)
    var redirectUris: Set<String>,
    @Column(length = 1000)
    @Convert(converter = StringSetConverter::class)
    var postLogoutRedirectUris: Set<String>,
    @Column(length = 1000)
    @Convert(converter = StringSetConverter::class)
    var scopes: Set<String>,
    @Column(length = 2000)
    var clientSettings: String?,
    @Column(length = 2000)
    var tokenSettings: String?,
    var active: Boolean = true
)