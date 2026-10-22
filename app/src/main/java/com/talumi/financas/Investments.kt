package com.talumi.financas

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.pow

enum class InvestmentProduct(val label:String) { CDB("CDB"), TESOURO_SELIC("Tesouro Selic"), LCI("LCI"), LCA("LCA"), FUND("Fundo"), OTHER("Outro / a classificar") }
enum class YieldMode(val label:String) { CDI_PERCENT("Percentual do CDI"), SELIC_PLUS("Selic + taxa anual"), ANNUAL("Taxa prefixada ao ano"), MONTHLY("Taxa fixa ao mês") }
enum class Liquidity(val label:String) { DAILY("Resgate disponível"), MATURITY("Bloqueado até o vencimento") }
enum class InvestmentAction(val label:String) { DEPOSIT("Aportar"), WITHDRAW("Resgatar"), GAIN("Registrar rendimento"), LOSS("Registrar perda / custo") }
data class Investment(val accountId:Long,val product:InvestmentProduct=InvestmentProduct.OTHER,val mode:YieldMode=YieldMode.MONTHLY,val rate:String="0",val maturity:LocalDate?=null,val liquidity:Liquidity=Liquidity.DAILY,val valuationDate:LocalDate=LocalDate.now(),val holidays:Set<LocalDate> = emptySet())
data class Benchmark(val code:String,val annual:String,val observed:LocalDate,val fetchedAt:String)
data class YieldEstimate(val amount:Long,val businessDays:Int,val calendarDays:Int)

object InvestmentMath {
    fun parseRate(text:String):String {
        val clean=text.trim().replace(',','.')
        require(clean.matches(Regex("\\d+(?:\\.\\d{1,4})?"))) {"Informe uma taxa positiva ou zero, com até quatro casas decimais."}
        val rate=BigDecimal(clean)
        require(rate>=BigDecimal.ZERO && rate<=BigDecimal("1000")) {"A taxa deve estar entre 0 e 1000%."}
        return rate.stripTrailingZeros().toPlainString()
    }
    fun estimate(investment:Investment,balance:Long,month:YearMonth,benchmarks:List<Benchmark>):YieldEstimate? {
        val start=maxOf(month.atDay(1),investment.valuationDate.plusDays(1))
        val end=minOf(month.plusMonths(1).atDay(1),investment.maturity?.plusDays(1) ?: month.plusMonths(1).atDay(1))
        if(end<=start || balance<=0) return YieldEstimate(0,0,0)
        val calendarDays=ChronoUnit.DAYS.between(start,end).toInt()
        var businessDays=0
        var day=start
        while(day<end) {if(day.dayOfWeek.value<=5 && day !in investment.holidays) businessDays++;day=day.plusDays(1)}
        val r=BigDecimal(parseRate(investment.rate)).toDouble()/100.0
        fun annual(code:String):Double?=benchmarks.firstOrNull {it.code==code}?.annual?.let {BigDecimal(parseRate(it)).toDouble()/100.0}
        val factor=when(investment.mode) {
            YieldMode.MONTHLY -> (1+r).pow(calendarDays.toDouble()/month.lengthOfMonth())
            YieldMode.ANNUAL -> (1+r).pow(businessDays/252.0)
            YieldMode.CDI_PERCENT -> {val base=annual("CDI") ?: return null; (1+((1+base).pow(1.0/252)-1)*r).pow(businessDays)}
            YieldMode.SELIC_PLUS -> {val base=annual("SELIC") ?: return null; ((1+base)*(1+r)).pow(businessDays/252.0)}
        }
        val result=BigDecimal.valueOf(balance).multiply(BigDecimal.valueOf(factor-1)).setScale(0,RoundingMode.HALF_UP).longValueExact()
        require(result in 0..Money.MAX_CENTS) {"A estimativa excede o limite. Revise a taxa."}
        return YieldEstimate(result,businessDays,calendarDays)
    }
    fun groupedByBank(state:FinanceState):List<Pair<String,Long>> = state.accounts.filter {it.kind==AccountKind.INVESTMENT}.groupBy {it.institution.trim().ifBlank {"Banco não informado"}}.map { (bank,accounts)->bank to accounts.sumOf {Ledger.balance(it,state.entries)} }.sortedByDescending {it.second}
    fun realized(state:FinanceState,month:YearMonth):Long = state.entries.filter {e->e.posted && YearMonth.from(e.date)==month && state.accounts.any {it.id==e.accountId && it.kind==AccountKind.INVESTMENT} && e.kind!=EntryKind.TRANSFER}.sumOf {if(it.kind==EntryKind.INCOME) it.amount else -it.amount}
}
