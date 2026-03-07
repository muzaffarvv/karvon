package uz.zero.auth.services

import java.math.BigDecimal

interface AccountService {
    fun withdraw(userId: Long, amount: BigDecimal)
    fun deposit(userId: Long, amount: BigDecimal)

}