package uz.vv.product

import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class CodeGenerator(private val entityManager: EntityManager) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val schemaName = "product"
    private val seqName = "product_code_seq"

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun getNextPluCode(): String {
        return try {
            val query = "SELECT nextval('${schemaName}.${seqName}')"
            val result = entityManager.createNativeQuery(query).singleResult
            result.toString()
        } catch (e: Exception) {
            logger.error("Failed to get next PLU code from sequence ${schemaName}.${seqName}", e)
            throw DatabaseSequenceException("${schemaName}.${seqName}")
        }
    }
}