package uz.zero.auth.repositories

import org.springframework.data.jpa.repository.JpaRepository
import uz.zero.auth.entities.AuthClient


interface AuthClientRepository : JpaRepository<AuthClient, String> {
    fun findByClientIdAndActiveTrue(clientId: String): AuthClient?
    fun existsByClientId(clientId: String): Boolean
    fun findByIdAndActiveTrue(id: String): AuthClient?
}