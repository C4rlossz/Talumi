package com.talumi.financas

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class AccountKind(val label: String) { BANK("Conta bancária"), CASH("Carteira"), SAVINGS("Dinheiro guardado"), INVESTMENT("Investimento"), JAR("Cofrinho") }
enum class EntryKind(val label: String) { INCOME("Entrada"), EXPENSE("Saída"), TRANSFER("Transferência") }
enum class DebtKind(val label: String) { RECEIVABLE("A receber"), PAYABLE("A pagar") }
enum class PayRule(val label: String) { FIXED_DAY("Dia fixo do mês"), FIFTH_BUSINESS_DAY("Quinto dia útil") }
data class Account(val id: Long = 0, val name: String, val institution: String = "", val kind: AccountKind = AccountKind.BANK, val opening: Long = 0)
data class Entry(val id: Long = 0, val title: String, val amount: Long, val kind: EntryKind, val accountId: Long, val toAccountId: Long? = null, val date: LocalDate, val posted: Boolean = false, val category: String = "Outros", val note: String = "", val seriesId: Long? = null, val debtId: Long? = null, val autoPost: Boolean = false)
data class Series(val id: Long = 0, val title: String, val amount: Long, val kind: EntryKind, val accountId: Long, val start: LocalDate, val category: String, val active: Boolean = true, val rule: PayRule = PayRule.FIXED_DAY, val day: Int = start.dayOfMonth, val autoPost: Boolean = false, val includeSaturday: Boolean = false, val holidays: Set<LocalDate> = emptySet())
data class Debt(val id: Long = 0, val person: String, val title: String, val amount: Long, val kind: DebtKind, val due: LocalDate, val note: String = "")
data class FinanceState(val accounts: List<Account> = emptyList(), val entries: List<Entry> = emptyList(), val debts: List<Debt> = emptyList(), val series: List<Series> = emptyList(), val loaded: Boolean = false, val investments: List<Investment> = emptyList(), val benchmarks: List<Benchmark> = emptyList())
val FinanceState.financeAccounts: List<Account> get() = accounts.filter {it.kind!=AccountKind.INVESTMENT}

object Money {
    const val MAX_CENTS = 999_999_999_999L
    fun parse(text: String, signed: Boolean = false): Long {
        val clean = text.trim().replace("R$", "").replace(" ", "")
        require(clean.matches(Regex("-?\\d+(?:,\\d{1,2})?"))) { "Use um valor como 1500,50, sem pontos." }
        val cents = BigDecimal(clean.replace(',', '.')).setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact()
        require(cents in -MAX_CENTS..MAX_CENTS && (signed || cents > 0)) { if (signed) "Valor fora do limite." else "Informe um valor maior que zero." }
        return cents
    }
    fun format(cents: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(BigDecimal.valueOf(cents, 2))
    fun edit(cents: Long): String = BigDecimal.valueOf(cents, 2).toPlainString().replace('.', ',')
}
object Ledger {
    fun balance(account: Account, entries: List<Entry>, until: LocalDate = LocalDate.now(), planned: Boolean = false): Long = entries.filter { (it.posted || planned) && it.date <= until }.fold(account.opening) { total, entry ->
        total + when {
            entry.kind == EntryKind.TRANSFER && entry.toAccountId == account.id -> entry.amount
            entry.accountId != account.id -> 0
            entry.kind == EntryKind.INCOME -> entry.amount
            else -> -entry.amount
        }
    }
    fun outstanding(debt: Debt, entries: List<Entry>): Long = debt.amount - entries.filter { it.debtId == debt.id && it.posted }.sumOf { it.amount }
    fun occurrence(start: LocalDate, month: YearMonth): LocalDate = month.atDay(minOf(start.dayOfMonth, month.lengthOfMonth()))
    fun occurrence(series: Series, month: YearMonth): LocalDate {
        if(series.rule==PayRule.FIXED_DAY) return month.atDay(minOf(series.day,month.lengthOfMonth()))
        var day=month.atDay(1)
        var count=0
        while(YearMonth.from(day)==month) {
            val weekday=day.dayOfWeek.value
            if(weekday!=7 && (weekday!=6 || series.includeSaturday) && day !in series.holidays) count++
            if(count==5) return day
            day=day.plusDays(1)
        }
        throw IllegalArgumentException("Não há cinco dias úteis neste mês. Confira os feriados e folgas.")
    }
    fun nextOccurrence(series: Series, from: LocalDate = LocalDate.now()): LocalDate {
        val minimum=maxOf(from,series.start)
        var month=YearMonth.from(minimum)
        while(month.year<=2100) {
            val date=occurrence(series,month)
            if(date>=minimum) return date
            month=month.plusMonths(1)
        }
        throw IllegalArgumentException("Não há ocorrência até 2100. Ajuste a data de início.")
    }
    fun monthly(entries: List<Entry>, month: YearMonth, kind: EntryKind): Long = entries.filter { it.posted && it.kind == kind && YearMonth.from(it.date) == month }.sumOf { it.amount }
    fun available(state: FinanceState): Long = state.financeAccounts.sumOf {balance(it,state.entries)}
    fun cashMonthly(state: FinanceState,month: YearMonth,kind: EntryKind): Long = monthly(state.entries.filter {e->state.financeAccounts.any {it.id==e.accountId}},month,kind)
}
val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
fun LocalDate.br(): String = format(dateFormat)
fun parseDate(text: String): LocalDate = try { LocalDate.parse(text, DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT)) } catch (_: Exception) { throw IllegalArgumentException("Informe uma data válida: dia/mês/ano.") }
