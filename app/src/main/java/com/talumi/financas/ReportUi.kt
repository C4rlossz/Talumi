package com.talumi.financas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import kotlinx.coroutines.launch

@Composable internal fun MonthlyReportScreen(state: FinanceState, month: YearMonth, onMonth: (YearMonth)->Unit, onMovements: ()->Unit, modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val report = remember(state, month, today) { MonthlyReport.from(state, month, today) }
    val colors = MaterialTheme.colorScheme
    val incomeColor = if(colors.background == Color(0xFF121019)) Color(0xFF62D7B0) else Color(0xFF0F765E)
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Relatório do mês", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Uma visão clara de cada entrada e saída.", color = colors.onSurfaceVariant)
        }
        item { MonthPicker(month, onMonth) }
        item {
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text("Seu mês em movimento", fontWeight = FontWeight.SemiBold, color = colors.primary)
                    MonthlyRing(report, month, incomeColor, colors.error)
                    HorizontalDivider()
                    FlowValue("+  Entradas", report.income, incomeColor)
                    FlowValue("−  Saídas", report.expense, colors.error)
                    Text("O anel mostra a proporção entre entradas e saídas confirmadas. No centro: entradas menos saídas.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Saldo das contas", color = colors.onSurfaceVariant)
                    Text(Money.format(report.balance), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = if(report.balance < 0) colors.error else colors.onSurface)
                    Text("Até ${report.through.br()} · inclui saldos iniciais, reservas e cofrinhos; exclui a carteira de investimentos.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    Text("${report.transactionCount} entradas e saídas confirmadas neste mês", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Text("Para onde foi seu dinheiro", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        if(report.categories.isEmpty()) item {
            Text(if(month > YearMonth.from(today)) "Este mês ainda não começou. Agendamentos continuam na Agenda." else "Nenhuma saída confirmada neste mês.", color = colors.onSurfaceVariant)
        }
        items(report.categories, key = {it.name}) { category ->
            val share = if(report.expense == 0L) 0f else (category.amount.toDouble() / report.expense).toFloat()
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(category.name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Text(String.format(Locale.forLanguageTag("pt-BR"), "%.1f%%", share * 100), color = colors.onSurfaceVariant)
                    }
                    Text(Money.format(category.amount), fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(progress = {share}, modifier = Modifier.fillMaxWidth(), color = colors.primary, trackColor = colors.surfaceVariant)
                }
            }
        }
        item {
            Text("Transferências não são receitas nem despesas. Aportes, resgates e rendimentos ficam na carteira de investimentos. Agendamentos só entram no relatório depois de confirmados e quando a data chegar.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            OutlinedButton(onClick = onMovements, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Ver movimentos do mês") }
        }
    }
}

@Composable private fun FlowValue(label: String, amount: Long, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = color, fontWeight = FontWeight.SemiBold)
        Text(Money.format(amount), color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun MonthlyRing(report: MonthlyReport, month: YearMonth, incomeColor: Color, expenseColor: Color) {
    val reveal = remember(month) { Animatable(0f) }
    val share = remember(month) { Animatable(report.incomeShare) }
    LaunchedEffect(month, report.income, report.expense) {
        reveal.snapTo(0f)
        kotlinx.coroutines.coroutineScope {
            launch { reveal.animateTo(if(report.transactionCount == 0) 0f else 1f, tween(850)) }
            launch { share.animateTo(report.incomeShare, tween(850)) }
        }
    }
    val colors = MaterialTheme.colorScheme
    val description = "Gráfico do mês. Entradas: ${Money.format(report.income)}. Saídas: ${Money.format(report.expense)}. Resultado: ${Money.format(report.net)}."
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxWidth().height(244.dp).semantics { contentDescription = description }) {
            val diameter = minOf(size.width, size.height) - 22.dp.toPx()
            val topLeft = Offset((size.width - diameter)/2, (size.height - diameter)/2)
            val ringSize = Size(diameter, diameter)
            val stroke = Stroke(13.dp.toPx(), cap = StrokeCap.Round)
            drawArc(colors.outline.copy(alpha = .25f), -90f, 360f, false, topLeft, ringSize, style = stroke)
            val incoming = 360f * share.value
            val outgoing = 360f - incoming
            // Surface gaps keep the two segments distinct without relying on adjacent colors.
            fun segment(color: Color, start: Float, sweep: Float) {
                if(sweep <= 0f) return
                val gap = if(incoming > 0f && outgoing > 0f) minOf(7f, sweep / 3f) else 0f
                val visible = (sweep - gap) * reveal.value
                if(visible > 0f) drawArc(color, start + gap/2, visible, false, topLeft, ringSize, style = Stroke(13.dp.toPx(), cap = StrokeCap.Butt))
            }
            segment(incomeColor, -90f, incoming)
            segment(expenseColor, -90f + incoming, outgoing)
        }
        Column(Modifier.widthIn(max = 174.dp).padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(Money.format(report.net), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = if(report.net < 0) colors.error else colors.onSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text("Resultado do mês", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Text(when { report.transactionCount == 0 -> "Sem movimentos"; report.net < 0 -> "Resultado negativo"; report.net > 0 -> "Resultado positivo"; else -> "Mês equilibrado" }, fontSize = 11.sp, color = if(report.net < 0) colors.error else colors.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
