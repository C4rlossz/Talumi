package com.talumi.financas

import java.time.LocalDate
import java.time.YearMonth

data class ExpenseCategory(val name: String, val amount: Long)

data class MonthlyReport(
    val income: Long,
    val expense: Long,
    val balance: Long,
    val through: LocalDate,
    val categories: List<ExpenseCategory>,
    val transactionCount: Int
) {
    val net: Long get() = income - expense
    val incomeShare: Float get() = if(income == 0L && expense == 0L) 0f else (income.toDouble() / (income.toDouble() + expense)).toFloat()

    companion object {
        fun from(state: FinanceState, month: YearMonth, today: LocalDate = LocalDate.now()): MonthlyReport {
            val through = minOf(month.atEndOfMonth(), today)
            val accountIds = state.financeAccounts.map { it.id }.toSet()
            val movements = state.entries.filter {
                it.posted && it.date <= through && YearMonth.from(it.date) == month &&
                    it.accountId in accountIds && it.kind != EntryKind.TRANSFER
            }
            val expenses = movements.filter { it.kind == EntryKind.EXPENSE }
            return MonthlyReport(
                income = movements.filter { it.kind == EntryKind.INCOME }.sumOf { it.amount },
                expense = expenses.sumOf { it.amount },
                balance = state.financeAccounts.sumOf { Ledger.balance(it, state.entries, through) },
                through = through,
                categories = expenses.groupBy { it.category.ifBlank { "Outros" } }.map { (name, entries) ->
                    ExpenseCategory(name, entries.sumOf { it.amount })
                }.sortedWith(compareByDescending<ExpenseCategory> { it.amount }.thenBy { it.name }),
                transactionCount = movements.size
            )
        }
    }
}
