package com.talumi.financas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@Composable internal fun WalletSwitcher(investments:Boolean,onChange:(Boolean)->Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=6.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        listOf(false to "Finanças",true to "Investimentos").forEach {(value,label)->
            FilterChip(selected=investments==value,onClick={onChange(value)},label={Text(label)},modifier=Modifier.weight(1f))
        }
    }
}

private fun estimateLabel(value:YieldEstimate?)=value?.let {Money.format(it.amount)} ?: "Taxa indisponível"
private val chartColors=listOf(Color(0xFF7047EB),Color(0xFF219F8C),Color(0xFF9A6500),Color(0xFFD76B9C),Color(0xFF5189D7),Color(0xFF8C788F))

@Composable internal fun InvestmentScreen(state:FinanceState,month:YearMonth,onMonth:(YearMonth)->Unit,ratesBusy:Boolean,ratesMessage:String,onRefresh:()->Unit,onEdit:(Account)->Unit,onAction:(Account,InvestmentAction)->Unit,onDetail:(Entry)->Unit,modifier:Modifier=Modifier) {
    val accounts=state.accounts.filter {it.kind==AccountKind.INVESTMENT}
    val total=accounts.sumOf {Ledger.balance(it,state.entries)}
    val estimates=accounts.map {a->state.investments.firstOrNull {it.accountId==a.id}?.let {runCatching {InvestmentMath.estimate(it,Ledger.balance(a,state.entries),month,state.benchmarks)}.getOrNull()}}
    LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(start=20.dp,end=20.dp,top=12.dp,bottom=100.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item {Text("Sua carteira de investimentos",fontSize=26.sp,fontWeight=FontWeight.Bold);Text("Separada do dinheiro disponível para gastar.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        item {Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer),shape=RoundedCornerShape(24.dp)) {Column(Modifier.fillMaxWidth().padding(22.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("Total investido hoje");Text(Money.format(total),fontSize=30.sp,fontWeight=FontWeight.Bold)
            Text("Saldo disponível nas Finanças: ${Money.format(Ledger.available(state))}",style=MaterialTheme.typography.bodySmall)
        }}}
        item {MonthPicker(month,onMonth)}
        item {Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) {Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("Previsão bruta no mês",fontWeight=FontWeight.Bold)
            Text(if(estimates.any {it==null}) "Previsão incompleta" else Money.format(estimates.sumOf {it?.amount ?: 0}),fontSize=24.sp,fontWeight=FontWeight.Bold)
            if(estimates.any {it==null}) Text("Atualize as taxas ou confira as taxas cadastradas. Os valores disponíveis aparecem em cada aplicação.")
            Text("Estimativa a partir do dia seguinte à data do saldo de cada aplicação, limitada ao mês e ao vencimento. Usa saldo e taxas atuais constantes; não inclui novos aportes, impostos, custos ou variação de preço. Não é pagamento mensal nem dinheiro recebido.",style=MaterialTheme.typography.bodySmall)
            HorizontalDivider();Text("Rendimentos e perdas registrados: ${Money.format(InvestmentMath.realized(state,month))}",fontWeight=FontWeight.SemiBold)
        }}}
        item {Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) {Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("CDI e Selic · Banco Central",fontWeight=FontWeight.Bold)
            listOf("CDI","SELIC").forEach {code->val rate=state.benchmarks.firstOrNull {it.code==code};Text(if(rate==null) "$code: aguardando consulta" else "$code: ${rate.annual.replace('.',',')}% a.a. · referência ${rate.observed.br()}")}
            if(state.benchmarks.any {it.observed<LocalDate.now().minusDays(7)}) Text("Taxa salva antiga: a previsão usa a referência exibida acima. Atualize quando houver internet.",color=MaterialTheme.colorScheme.error)
            Text("Taxas anuais efetivas, base 252 dias úteis. A última consulta fica salva para uso offline. Feriados podem ser informados em cada aplicação.",style=MaterialTheme.typography.bodySmall)
            state.benchmarks.maxOfOrNull {it.fetchedAt}?.let {Text("Última consulta: "+runCatching {java.time.Instant.parse(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate().br()}.getOrDefault(it),style=MaterialTheme.typography.bodySmall)}
            if(ratesMessage.isNotBlank()) Text(ratesMessage,style=MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick=onRefresh,enabled=!ratesBusy) {Text(if(ratesBusy) "Consultando taxas…" else "Atualizar CDI e Selic")}
        }}}
        item {InvestmentPie(InvestmentMath.groupedByBank(state))}
        if(accounts.isEmpty()) item {Text("Use + para cadastrar uma aplicação já existente. Para aplicar dinheiro das Finanças, cadastre valor zero e depois use Aportar.")}
        items(accounts,key={"investment-${it.id}"}) {a->
            val investment=state.investments.firstOrNull {it.accountId==a.id} ?: Investment(a.id)
            val balance=Ledger.balance(a,state.entries)
            val estimate=runCatching {InvestmentMath.estimate(investment,balance,month,state.benchmarks)}.getOrNull()
            Card(shape=RoundedCornerShape(22.dp)) {Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(a.name,fontSize=20.sp,fontWeight=FontWeight.Bold);Text("${a.institution.ifBlank {"Banco não informado"}} · ${investment.product.label}")
                Text(Money.format(balance),fontSize=25.sp,fontWeight=FontWeight.Bold)
                Text("${investment.mode.label}: ${investment.rate.replace('.',',')}%")
                Text("${investment.liquidity.label}${investment.maturity?.let {" · ${it.br()}"}.orEmpty()}",style=MaterialTheme.typography.bodySmall)
                Text("Saldo de referência: ${investment.valuationDate.br()}",style=MaterialTheme.typography.bodySmall)
                Text("Previsão bruta: ${estimateLabel(estimate)}${estimate?.let {" · ${it.businessDays} dias úteis / ${it.calendarDays} corridos"}.orEmpty()}")
                if(balance<0) Text("Saldo legado negativo: revise os registros desta aplicação.",color=MaterialTheme.colorScheme.error)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick={onAction(a,InvestmentAction.DEPOSIT)}) {Text("Aportar")}
                    OutlinedButton(onClick={onAction(a,InvestmentAction.WITHDRAW)},enabled=balance>0 && (investment.liquidity!=Liquidity.MATURITY || investment.maturity?.let {it<=LocalDate.now()}==true)) {Text("Resgatar")}
                }
                TextButton(onClick={onAction(a,InvestmentAction.GAIN)}) {Text("Registrar rendimento ou perda")}
                TextButton(onClick={onEdit(a)}) {Text("Editar aplicação")}
            }}
        }
        item {SectionTitle("Movimentos da carteira no mês")}
        val ids=accounts.map {it.id}.toSet()
        val entries=state.entries.filter {(it.accountId in ids || it.toAccountId in ids) && YearMonth.from(it.date)==month}.sortedWith(compareByDescending<Entry> {it.date}.thenByDescending {it.id})
        if(entries.isEmpty()) item {Text("Nenhum aporte, resgate ou rendimento registrado neste mês.")}
        items(entries,key={"investment-entry-${it.id}"}) {EntryCard(it,state.accounts) {onDetail(it)}}
        item {Text("Carteira local · Sem conexão com bancos",style=MaterialTheme.typography.bodySmall)}
    }
}

@Composable private fun InvestmentPie(banks:List<Pair<String,Long>>) {
    val positive=banks.filter {it.second>0};val total=positive.sumOf {it.second}
    val separator=MaterialTheme.colorScheme.surface
    Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) {Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Distribuição por banco",fontWeight=FontWeight.Bold)
        if(total==0L) Text("O gráfico aparecerá quando houver saldo investido positivo.") else {
            Canvas(Modifier.fillMaxWidth().height(210.dp).semantics {contentDescription="Distribuição dos investimentos por banco: "+positive.joinToString("; ") {"${it.first}, ${Money.format(it.second)}, ${String.format(Locale.forLanguageTag("pt-BR"),"%.1f",it.second.toDouble()/total*100)} por cento"}}) {
                var start=-90f;val diameter=minOf(size.width,size.height);val offset=androidx.compose.ui.geometry.Offset((size.width-diameter)/2,(size.height-diameter)/2)
                positive.forEachIndexed {i,(_,value)->val sweep=(value.toDouble()/total*360).toFloat();drawArc(chartColors[i%chartColors.size],start,sweep,true,topLeft=offset,size=androidx.compose.ui.geometry.Size(diameter,diameter));start+=sweep}
                var boundary=-90.0
                val center=androidx.compose.ui.geometry.Offset(size.width/2,size.height/2)
                if(positive.size>1) positive.forEach {(_,value)->val radians=Math.toRadians(boundary);drawLine(separator,center,center+androidx.compose.ui.geometry.Offset((kotlin.math.cos(radians)*diameter/2).toFloat(),(kotlin.math.sin(radians)*diameter/2).toFloat()),strokeWidth=2.dp.toPx());boundary+=value.toDouble()/total*360}

            }
            positive.forEachIndexed {i,(name,value)->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(14.dp).background(chartColors[i%chartColors.size],RoundedCornerShape(4.dp)))
                Text(name,Modifier.weight(1f));Text("${Money.format(value)} · ${String.format(Locale.forLanguageTag("pt-BR"),"%.1f",value.toDouble()/total*100)}%")
            }}
        }
        if(banks.any {it.second<0}) Text("Saldos negativos não entram nas fatias do gráfico.",style=MaterialTheme.typography.bodySmall)
    }}
}

@Composable internal fun InvestmentForm(original:Account,investment:Investment,state:FinanceState,busy:Boolean,onDismiss:()->Unit,onSave:(Account,Investment)->Unit,onDelete:()->Unit) {
    var name by remember {mutableStateOf(original.name)};var bank by remember {mutableStateOf(original.institution)}
    var opening by remember {mutableStateOf(Money.edit(original.opening))}
    var product by remember {mutableStateOf(investment.product)};var mode by remember {mutableStateOf(investment.mode)};var rate by remember {mutableStateOf(investment.rate.replace('.',','))}
    var date by remember {mutableStateOf(investment.valuationDate.br())};var hasMaturity by remember {mutableStateOf(investment.maturity!=null)}
    var maturity by remember {mutableStateOf((investment.maturity ?: LocalDate.now().plusYears(1)).br())};var liquidity by remember {mutableStateOf(investment.liquidity)}
    var holidays by remember {mutableStateOf(investment.holidays.sorted().joinToString("\n") {it.br()})};var error by remember {mutableStateOf("")}
    FormDialog(if(original.id==0L) "Novo investimento" else "Editar investimento",busy,error,onDismiss,{
        try {
            require(name.isNotBlank() && bank.isNotBlank()) {"Informe o nome e o banco da aplicação."}
            val value=if(original.id==0L) Money.parse(opening,true) else original.opening
            require(original.id>0 || value>=0) {"O valor inicial não pode ser negativo."}
            require(liquidity!=Liquidity.MATURITY || hasMaturity) {"Informe o vencimento para bloquear o resgate."}
            onSave(original.copy(name=name,institution=bank,kind=AccountKind.INVESTMENT,opening=value),investment.copy(product=product,mode=mode,rate=InvestmentMath.parseRate(rate),valuationDate=parseDate(date),maturity=if(hasMaturity) parseDate(maturity) else null,liquidity=liquidity,holidays=holidays.lineSequence().filter {it.isNotBlank()}.map {parseDate(it.trim())}.toSet()))
        } catch(e:Exception) {error=e.message ?: "Confira os dados."}
    }) {
        Input("Nome da aplicação",name,{name=it});Input("Banco / instituição",bank,{bank=it})
        Choice("Produto",InvestmentProduct.entries.map {it.label},product.label,{label->product=InvestmentProduct.entries.first {it.label==label}})
        Choice("Como rende?",YieldMode.entries.map {it.label},mode.label,{label->mode=YieldMode.entries.first {it.label==label};rate=if(mode==YieldMode.CDI_PERCENT) "100" else "0"})
        Input(when(mode) {YieldMode.CDI_PERCENT->"Percentual do CDI (%)";YieldMode.SELIC_PLUS->"Taxa adicional à Selic (% ao ano)";YieldMode.ANNUAL->"Taxa (% ao ano)";YieldMode.MONTHLY->"Taxa (% ao mês)"},rate,{rate=it},money=true)
        Text("CDI é um índice de referência. Informe o percentual do contrato (ex.: 100 ou 110). CDI e Selic são consultados online; sem taxa salva, a previsão correspondente fica indisponível.",style=MaterialTheme.typography.bodySmall)
        if(original.id==0L) {Input("Valor já investido (R$)",opening,{opening=it},money=true);Text("Este valor representa uma aplicação que você já possui e não desconta uma conta das Finanças. Para transferir dinheiro disponível, comece com zero e use Aportar.",style=MaterialTheme.typography.bodySmall)}
        else Text("Saldo atual: ${Money.format(Ledger.balance(original,state.entries))}. Use aportes, resgates, rendimentos ou perdas para atualizar.")
        DateInput("Data do saldo de referência",date,{date=it})
        Row {Text("Possui vencimento",Modifier.weight(1f));Switch(hasMaturity,{hasMaturity=it;if(!it) liquidity=Liquidity.DAILY},modifier=Modifier.semantics {contentDescription="Possui vencimento"})}
        if(hasMaturity) DateInput("Vencimento",maturity,{maturity=it})
        Choice("Disponibilidade",Liquidity.entries.map {it.label},liquidity.label,{label->liquidity=Liquidity.entries.first {it.label==label}})
        Input("Feriados para a previsão (dd/mm/aaaa, um por linha)",holidays,{holidays=it},multiline=true)
        Text("Estimativa bruta para acompanhamento. Tesouro Selic pode variar com o preço de venda antecipada; valores efetivos devem ser registrados conforme o extrato.",style=MaterialTheme.typography.bodySmall)
        if(original.id>0) DeleteButton("Excluir aplicação sem histórico",busy,onDelete)
    }
}

@Composable internal fun InvestmentMovementForm(account:Account,initial:InvestmentAction,state:FinanceState,busy:Boolean,onDismiss:()->Unit,onSave:(InvestmentAction,Long,Long?,LocalDate,String)->Unit) {
    var action by remember {mutableStateOf(initial)};var amount by remember {mutableStateOf("")};var cash by remember {mutableLongStateOf(state.financeAccounts.firstOrNull()?.id ?: 0)}
    var date by remember {mutableStateOf(LocalDate.now().br())};var note by remember {mutableStateOf("")};var error by remember {mutableStateOf("")}
    val transfer=action==InvestmentAction.DEPOSIT || action==InvestmentAction.WITHDRAW
    FormDialog("Movimentar investimento",busy,error,onDismiss,{
        try {onSave(action,Money.parse(amount),if(transfer) cash else null,parseDate(date),note)} catch(e:Exception) {error=e.message ?: "Confira os dados."}
    },saveLabel="Registrar movimento",saveEnabled=!transfer || state.financeAccounts.isNotEmpty()) {
        Text("${account.name} · ${Money.format(Ledger.balance(account,state.entries))}",fontWeight=FontWeight.Bold)
        Choice("Operação",InvestmentAction.entries.map {it.label},action.label,{label->action=InvestmentAction.entries.first {it.label==label}})
        Input("Valor (R$)",amount,{amount=it},money=true)
        if(transfer) {
            if(state.financeAccounts.isEmpty()) Text("Cadastre uma conta nas Finanças para aportar ou resgatar.")
            AccountChoice(if(action==InvestmentAction.DEPOSIT) "Retirar da conta" else "Receber na conta",state.financeAccounts,cash,{cash=it})
            state.financeAccounts.firstOrNull {it.id==cash}?.let {Text("Saldo disponível: ${Money.format(Ledger.balance(it,state.entries))}")}
        }
        DateInput("Data do movimento",date,{date=it});Input("Observação (opcional)",note,{note=it},multiline=true)
        Text(if(transfer) "A transferência altera a carteira e a conta escolhida juntas. Não gera receita ou despesa. Resgates bloqueados aguardam o vencimento." else "Registre apenas rendimentos ou perdas confirmados no extrato. Este ajuste altera somente o investimento; para usar o dinheiro nas Finanças, registre um resgate.",style=MaterialTheme.typography.bodySmall)
    }
}
