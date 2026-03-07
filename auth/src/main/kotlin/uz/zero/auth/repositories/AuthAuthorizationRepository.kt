package uz.zero.auth.repositories

import org.springframework.data.jpa.repository.JpaRepository
import uz.zero.auth.entities.AuthAuthorization


interface AuthAuthorizationRepository : JpaRepository<AuthAuthorization, String> {
    fun findByAccessTokenValue(accessTokenValue: String): AuthAuthorization?
    fun findByRefreshTokenValue(refreshTokenValue: String): AuthAuthorization?
    fun findByRegisteredClientIdAndPrincipalName(registeredClientId: String, principalName: String): AuthAuthorization?
}