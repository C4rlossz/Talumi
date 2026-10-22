package com.talumi.financas

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.repeatOnLifecycle
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Violet=Color(0xFF7047EB)
private val Lavender=Color(0xFFEDE7FF)
private val Light=lightColorScheme(primary=Violet,onPrimary=Color.White,primaryContainer=Lavender,onPrimaryContainer=Color(0xFF3F248D),background=Color(0xFFF8F7FC),onBackground=Color(0xFF171322),surface=Color.White,onSurface=Color(0xFF171322),surfaceVariant=Color(0xFFF0ECF8),onSurfaceVariant=Color(0xFF686176),error=Color(0xFFBA3550),outline=Color(0xFF827A91))
private val Dark=darkColorScheme(primary=Color(0xFFB69AFF),onPrimary=Color(0xFF24104E),primaryContainer=Color(0xFF34224F),onPrimaryContainer=Lavender,background=Color(0xFF121019),onBackground=Color(0xFFF5F2FA),surface=Color(0xFF1E192A),onSurface=Color(0xFFF5F2FA),surfaceVariant=Color(0xFF2A2338),onSurfaceVariant=Color(0xFFBAB1CD),error=Color(0xFFFF8FA3),outline=Color(0xFF8E839F))
@Composable private fun positive() = if(MaterialTheme.colorScheme.background==Dark.background) Color(0xFF62D7B0) else Color(0xFF0F765E)

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { Talumi() }
    }
}

@Composable fun ChipMark(modifier: Modifier=Modifier) {
    Canvas(modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Violet)) {
        val u=size.width/10f
        fun cell(x: Float,y: Float,w: Float,h: Float) { drawRoundRect(Lavender,Offset(x*u,y*u),Size(w*u,h*u),CornerRadius(u*.35f)) }
        cell(2f,2f,1.7f,1.7f); cell(4.3f,2f,3.7f,1.7f); cell(2f,4.3f,1.7f,1.7f)
        cell(4.3f,4.3f,1.7f,3.7f); cell(6.6f,4.3f,1.4f,1.7f); cell(2f,6.6f,1.7f,1.4f); cell(6.6f,6.6f,1.4f,1.4f)
    }
}

@Composable fun Talumi(vm: FinanceViewModel=viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val ratesBusy by vm.ratesBusy.collectAsStateWithLifecycle()
    val ratesMessage by vm.ratesMessage.collectAsStateWithLifecycle()
    val theme by vm.theme.collectAsStateWithLifecycle()
    val dark=when(theme) { "Escuro" -> true; "Claro" -> false; else -> isSystemInDarkTheme() }
    MaterialTheme(colorScheme=if(dark) Dark else Light, typography=Typography()) {
        val activity=androidx.activity.compose.LocalActivity.current
        SideEffect {
            activity?.window?.let { window ->
                androidx.core.view.WindowCompat.getInsetsController(window,window.decorView).apply {
                    isAppearanceLightStatusBars=!dark
                    isAppearanceLightNavigationBars=!dark
                }
            }
        }
        val snackbar=remember { SnackbarHostState() }
        LaunchedEffect(vm) { vm.events.collect { snackbar.showSnackbar(it) } }
        val lifecycle=androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
        LaunchedEffect(vm,lifecycle) {
            lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
                vm.refresh()
                while(true) { kotlinx.coroutines.delay(60_000); vm.refresh() }
            }
        }
        var investmentMode by rememberSaveable { mutableStateOf(false) }
        var investmentForm by remember {mutableStateOf<Account?>(null)}
        var investmentMove by remember {mutableStateOf<Pair<Account,InvestmentAction>?>(null)}
        val financeState=state.copy(accounts=state.financeAccounts,entries=state.entries.filter {e->e.kind==EntryKind.TRANSFER || state.financeAccounts.any {it.id==e.accountId}})
        LaunchedEffect(investmentMode,state.loaded) {if(investmentMode && state.loaded) vm.refreshRates()}
        var tab by rememberSaveable { mutableIntStateOf(0) }
        val scrollStates=listOf(rememberLazyListState(),rememberLazyListState(),rememberLazyListState(),rememberLazyListState(),rememberLazyListState())
        var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
        var movementQuery by rememberSaveable { mutableStateOf("") }
        var movementFilter by rememberSaveable { mutableStateOf("Todos") }
        val month=YearMonth.parse(monthText)
        var settings by remember { mutableStateOf(false) }
        var about by remember { mutableStateOf(false) }
        var legalCenter by remember {mutableStateOf(false)}
        var accountForm by remember { mutableStateOf<Account?>(null) }
        var entryForm by remember { mutableStateOf<Entry?>(null) }
        var debtForm by remember { mutableStateOf<Debt?>(null) }
        var settle by remember { mutableStateOf<Debt?>(null) }
        var detail by remember { mutableStateOf<Entry?>(null) }
        var seriesList by remember { mutableStateOf(false) }
        var salaryForm by remember { mutableStateOf(false) }
        var restoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
        val exporter=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { it?.let(vm::export) }
        val importer=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { restoreUri=it }
        fun newEntry(kind: EntryKind=EntryKind.EXPENSE) { entryForm=Entry(title="",amount=0,kind=kind,accountId=state.financeAccounts.firstOrNull()?.id ?: 0,date=LocalDate.now(),posted=true) }
        val tabs=listOf("Início","Movimentos","Agenda","Contas","Pessoas","Relatórios")
        val icons=listOf(Icons.Rounded.Home,Icons.AutoMirrored.Rounded.ReceiptLong,Icons.Rounded.CalendarMonth,Icons.Rounded.AccountBalanceWallet,Icons.Rounded.People,Icons.Rounded.DonutLarge)
        Scaffold(
            containerColor=MaterialTheme.colorScheme.background,
            snackbarHost={ SnackbarHost(snackbar) },
            topBar={
                Column(Modifier.statusBarsPadding()) {
                    Row(Modifier.fillMaxWidth().padding(start=24.dp,end=12.dp,top=12.dp,bottom=8.dp),verticalAlignment=Alignment.CenterVertically) {
                        ChipMark(Modifier.size(36.dp))
                        Text("talumi",Modifier.padding(start=10.dp).weight(1f),fontSize=26.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=(-1).sp)
                        IconButton(onClick={ about=true }) { Icon(Icons.Rounded.Info,"Informações do aplicativo") }
                        IconButton(onClick={ settings=true }) { Icon(Icons.Rounded.Tune,"Configurações e backup") }
                    }
                    WalletSwitcher(investmentMode) {investmentMode=it}
                    if(busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            },
            bottomBar={ if(!investmentMode) NavigationBar(containerColor=MaterialTheme.colorScheme.surface,tonalElevation=0.dp) {
                tabs.forEachIndexed { index,label -> NavigationBarItem(selected=tab==index,onClick={tab=index},icon={Icon(icons[index],null)},label={Text(label,fontSize=10.sp,maxLines=1,overflow=TextOverflow.Ellipsis)},colors=NavigationBarItemDefaults.colors(indicatorColor=MaterialTheme.colorScheme.primaryContainer)) }
            } },
            floatingActionButton={ if(state.loaded && (investmentMode || tab!=5)) FloatingActionButton(onClick={ when { investmentMode -> investmentForm=Account(name="",kind=AccountKind.INVESTMENT); tab==3 -> accountForm=Account(name=""); tab==4 -> debtForm=Debt(person="",title="",amount=0,kind=DebtKind.RECEIVABLE,due=LocalDate.now()); state.financeAccounts.isEmpty() -> accountForm=Account(name=""); else -> newEntry() } },containerColor=Violet,contentColor=Color.White) { Icon(Icons.Rounded.Add,if(investmentMode) "Novo investimento" else when(tab) { 3 -> "Nova conta"; 4 -> "Novo compromisso"; else -> "Novo lançamento" }) } }
        ) { padding ->
            if(!state.loaded) Box(Modifier.fillMaxSize().padding(padding),contentAlignment=Alignment.Center) { Column(horizontalAlignment=Alignment.CenterHorizontally) { CircularProgressIndicator(); Text("Preparando suas finanças…",Modifier.padding(16.dp)) } }
            else if(investmentMode) InvestmentScreen(state,month,{monthText=it.toString()},ratesBusy,ratesMessage,{vm.refreshRates(true)},{investmentForm=it},{a,action->investmentMove=a to action},{detail=it},Modifier.padding(padding))
            else if(tab==5) MonthlyReportScreen(state,month,{monthText=it.toString()},{tab=1},Modifier.padding(padding))
            else LazyColumn(Modifier.fillMaxSize().padding(padding),state=scrollStates[tab],contentPadding=PaddingValues(start=20.dp,end=20.dp,top=12.dp,bottom=96.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                item { Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(if(tab==0) "Seu dinheiro, com clareza." else tabs[tab],fontSize=if(tab==0) 22.sp else 28.sp,fontWeight=FontWeight.Bold); Text(when(tab) {0->"Tudo no seu ritmo. Tudo em um lugar.";1->"Cada movimento conta.";2->"Planeje hoje. Respire amanhã.";3->"Saiba onde seu dinheiro está.";else->"Combinados claros, contas em dia."},style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant) }
                } }
                if(tab in 0..2) item { MonthPicker(month) { monthText=it.toString() } }
                when(tab) {
                    0 -> {
                        item { BalanceCard(financeState,month) }
                        item { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            QuickAction("Entrada",Icons.Rounded.Add,Modifier.weight(1f)) { if(state.financeAccounts.isEmpty()) accountForm=Account(name="") else newEntry(EntryKind.INCOME) }
                            QuickAction("Saída",Icons.Rounded.Remove,Modifier.weight(1f)) { if(state.financeAccounts.isEmpty()) accountForm=Account(name="") else newEntry() }
                            QuickAction("Agendar",Icons.Rounded.Event,Modifier.weight(1f)) { if(state.financeAccounts.isEmpty()) accountForm=Account(name="") else { newEntry(); entryForm=entryForm?.copy(posted=false) } }
                        } }
                        item { SectionTitle("Minhas contas","Ver todas") {tab=3} }
                        if(state.financeAccounts.isEmpty()) item { EmptyCard("Comece pela sua primeira conta","Cadastre um banco, sua carteira ou um cofrinho com o saldo que você tem hoje.","Adicionar conta") {accountForm=Account(name="")} }
                        else items(state.financeAccounts.take(3),key={"home-account-${it.id}"}) { a -> AccountCard(a,state.entries) {accountForm=a} }
                        item { SectionTitle("Próximos compromissos","Ver agenda") {tab=2} }
                        val pending=financeState.entries.filter {!it.posted && YearMonth.from(it.date)<=month}.sortedBy {it.date}.take(4)
                        if(pending.isEmpty()) item { HintCard("Agenda tranquila", "Seus pagamentos e recebimentos agendados aparecem aqui.",Icons.Rounded.EventAvailable) }
                        else items(pending,key={"pending-${it.id}"}) {entry -> EntryCard(entry,state.accounts,onClick={detail=entry})}
                        item { DebtSummary(state) {tab=4} }
                    }
                    1 -> {
                        item { MovementScreenHeader(financeState,month) }
                        item { MovementFilters(movementQuery,{movementQuery=it},movementFilter,{movementFilter=it}) }
                        val movements=financeState.entries.filter {it.posted && YearMonth.from(it.date)==month && (movementFilter=="Todos" || it.kind.label==movementFilter) && (it.title.contains(movementQuery,true) || it.category.contains(movementQuery,true))}.sortedWith(compareByDescending<Entry> {it.date}.thenByDescending {it.id})
                        if(movements.isEmpty()) item {HintCard("Nenhum movimento encontrado","Registre uma entrada ou saída, ou altere o mês e os filtros.",Icons.Rounded.Search)}
                        items(movements,key={"movement-${it.id}"}) {entry -> EntryCard(entry,state.accounts) {detail=entry}}
                    }
                    2 -> {
                        item { HintCard("Sua agenda financeira","Salários automáticos entram no saldo na data configurada. Os demais compromissos aguardam sua confirmação. Nenhuma ação movimenta seu banco.",Icons.Rounded.Info) }
                        item { Button(onClick={salaryForm=true},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)) {Icon(Icons.Rounded.Repeat,null); Spacer(Modifier.width(8.dp)); Text("Cadastrar salário recorrente")} }
                        item { OutlinedButton(onClick={seriesList=true},modifier=Modifier.fillMaxWidth()) {Icon(Icons.Rounded.Repeat,null); Spacer(Modifier.width(8.dp)); Text("Gerenciar recorrências mensais")} }
                        val upcoming=financeState.entries.filter {!it.posted && YearMonth.from(it.date)==month}.sortedBy {it.date}
                        val overdue=financeState.entries.filter {!it.posted && it.date<month.atDay(1) && it.date<LocalDate.now()}.sortedBy {it.date}
                        if(overdue.isNotEmpty()) { item {Text("Pendências anteriores",fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.error)}; items(overdue,key={"overdue-${it.id}"}) {EntryCard(it,state.accounts,onClick={detail=it})} }
                        item {SectionTitle("Agendamentos do mês")}
                        if(upcoming.isEmpty()) item {HintCard("Sem agendamentos neste mês","Use + para agendar uma conta, salário ou transferência.",Icons.Rounded.CalendarMonth)}
                        items(upcoming,key={"upcoming-${it.id}"}) {EntryCard(it,state.accounts,onClick={detail=it})}
                        val dueDebts=state.debts.filter {Ledger.outstanding(it,state.entries)>0 && (YearMonth.from(it.due)==month || (it.due<LocalDate.now() && it.due<month.atDay(1)))}.sortedBy {it.due}
                        if(dueDebts.isNotEmpty()) item {SectionTitle("Combinados com pessoas")}
                        items(dueDebts,key={"due-debt-${it.id}"}) {debt -> DebtCard(debt,state.entries,onEdit={debtForm=debt},onSettle={settle=debt})}
                    }
                    3 -> {
                        item { HintCard("Seu mapa financeiro","Os saldos são informados por você. Ao registrar um gasto, escolha de qual conta o dinheiro saiu.",Icons.Rounded.AccountBalance) }
                        if(state.financeAccounts.isEmpty()) item {EmptyCard("Onde está seu dinheiro?","Adicione bancos, carteira, reservas ou cofrinhos.","Adicionar conta") {accountForm=Account(name="")}}
                        items(state.financeAccounts,key={"account-${it.id}"}) {AccountCard(it,state.entries) {accountForm=it}}
                        if(state.financeAccounts.size>=2) item {OutlinedButton(onClick={newEntry(EntryKind.TRANSFER)},modifier=Modifier.fillMaxWidth()) {Icon(Icons.Rounded.SwapHoriz,null); Spacer(Modifier.width(8.dp)); Text("Transferir entre minhas contas")}}
                    }
                    4 -> {
                        item {DebtSummary(state)}
                        item {HintCard("Dívidas e valores a receber","Cadastrar um compromisso não altera o saldo. Registre cada acerto e escolha a conta usada para pagar ou receber.",Icons.Rounded.Handshake)}
                        if(state.debts.isEmpty()) item {EmptyCard("Nenhum compromisso por aqui","Registre quem deve a você ou o que você precisa pagar, com valor e vencimento.","Adicionar compromisso") {debtForm=Debt(person="",title="",amount=0,kind=DebtKind.RECEIVABLE,due=LocalDate.now())}}
                        items(state.debts.sortedWith(compareBy<Debt> {Ledger.outstanding(it,state.entries)==0L}.thenBy {it.due}),key={"debt-${it.id}"}) { debt -> DebtCard(debt,state.entries,onEdit={debtForm=debt},onSettle={settle=debt}) }
                    }
                }
                item {Text("Só no seu dispositivo · Sem conexão bancária",Modifier.fillMaxWidth().padding(top=8.dp),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
            }
        }
        investmentForm?.let {a->InvestmentForm(a,state.investments.firstOrNull {it.accountId==a.id} ?: Investment(a.id),state,busy,{investmentForm=null},{account,value->vm.investment(account,value){investmentForm=null}},{vm.deleteAccount(a.id){investmentForm=null}})}
        investmentMove?.let {(a,action)->InvestmentMovementForm(a,action,state,busy,{investmentMove=null},{type,amount,cash,date,note->vm.investmentMovement(a.id,type,amount,cash,date,note){investmentMove=null}})}
        accountForm?.let {AccountForm(it,state,busy,onDismiss={accountForm=null},onSave={a->vm.account(a){accountForm=null}},onDelete={vm.deleteAccount(it.id){accountForm=null}})}
        entryForm?.let {EntryForm(it,state,busy,onDismiss={entryForm=null},onSave={e,r->vm.entry(e,r){entryForm=null}})}
        debtForm?.let {DebtForm(it,state,busy,onDismiss={debtForm=null},onSave={d->vm.debt(d){debtForm=null}},onDelete={vm.deleteDebt(it.id){debtForm=null}})}
        settle?.let {SettleForm(it,state,busy,onDismiss={settle=null},onSave={a,v,d->vm.settle(it,a,v,d){settle=null}})}
        detail?.let { entry -> EntryDetail(entry,state,busy,onDismiss={detail=null},onEdit={entryForm=entry;detail=null},onDelete={vm.deleteEntry(entry.id){detail=null}},onConfirm={vm.confirm(entry);detail=null}) }
        if(seriesList) SeriesDialog(state,busy,onDismiss={seriesList=false},onStop={id->vm.stopSeries(id){}},onEnableAutomatic=vm::enableAutomaticIncome)
        if(salaryForm) SalaryForm(state,busy,onDismiss={salaryForm=false},onSave={salary->vm.salary(salary){salaryForm=false}})
        val creator=androidx.compose.ui.res.stringResource(R.string.creator_name)
        val creatorDescription=androidx.compose.ui.res.stringResource(R.string.creator_description)
        val creatorGithub=androidx.compose.ui.res.stringResource(R.string.creator_github_url)
        val uriHandler=androidx.compose.ui.platform.LocalUriHandler.current
        if(about) AboutTalumi {about=false}
        if(legalCenter) LegalCenter(vm,busy) {legalCenter=false}
        if(settings) AlertDialog(onDismissRequest={settings=false},title={Text("Do seu jeito")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick={settings=false;legalCenter=true},modifier=Modifier.fillMaxWidth()) {Text("Privacidade, acessibilidade e licenças")}
            Text("Aparência",fontWeight=FontWeight.Bold)
            Choice("Tema",listOf("Sistema","Claro","Escuro"),theme,vm::theme)
            OutlinedButton(onClick={settings=false;salaryForm=true},modifier=Modifier.fillMaxWidth()) {Text("Configurar salário recorrente")}
            HorizontalDivider()
            Text("Seus registros financeiros ficam somente neste aparelho. Apenas CDI e Selic são consultados no Banco Central, sem enviar seus valores ou contas. Faça um backup antes de trocar de celular ou desinstalar.")
            OutlinedButton(onClick={exporter.launch("talumi-backup-${LocalDate.now()}.json")},enabled=!busy,modifier=Modifier.fillMaxWidth()) {Text("Exportar backup local")}
            OutlinedButton(onClick={importer.launch(arrayOf("application/json","text/*","application/octet-stream"))},enabled=!busy,modifier=Modifier.fillMaxWidth()) {Text("Restaurar um backup")}
            Text("O backup contém seus registros financeiros em texto e não é criptografado. Escolha onde salvá-lo.",style=MaterialTheme.typography.bodySmall)
            HorizontalDivider()
            Text("Sobre o Talumi",fontWeight=FontWeight.Bold)
            Text("Criado por $creator",fontWeight=FontWeight.SemiBold)
            Text(creatorDescription,style=MaterialTheme.typography.bodyMedium)
            if(creatorGithub.isNotBlank()) OutlinedButton(onClick={uriHandler.openUri(creatorGithub)},modifier=Modifier.fillMaxWidth()) {Text("Ver projeto no GitHub")}
            Text("Talumi ${androidx.compose.ui.res.stringResource(R.string.app_version)} · Seu dinheiro, com clareza.",style=MaterialTheme.typography.labelMedium)
        }},confirmButton={TextButton(onClick={settings=false}){Text("Concluir")}})
        restoreUri?.let {uri -> AlertDialog(onDismissRequest={if(!busy) restoreUri=null},title={Text("Restaurar seus registros?")},text={Text("Esta ação substitui todos os registros atuais pelos dados do arquivo escolhido. Exporte um backup antes se quiser manter uma cópia. Se o arquivo for inválido, seus registros atuais serão preservados.")},confirmButton={TextButton(enabled=!busy,onClick={vm.restore(uri){restoreUri=null;settings=false}}){Text("Substituir e restaurar")}},dismissButton={TextButton(enabled=!busy,onClick={restoreUri=null}){Text("Cancelar")}}) }
    }
}

@Composable internal fun MonthPicker(month: YearMonth,onChange:(YearMonth)->Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface),verticalAlignment=Alignment.CenterVertically) {
        IconButton(onClick={onChange(month.minusMonths(1))},enabled=month>YearMonth.of(2000,1)) {Icon(Icons.AutoMirrored.Rounded.ArrowBack,"Mês anterior")}
        Text(month.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy",Locale.forLanguageTag("pt-BR"))).replaceFirstChar {it.titlecase()},Modifier.weight(1f),fontWeight=FontWeight.SemiBold,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        IconButton(onClick={onChange(month.plusMonths(1))},enabled=month<YearMonth.of(2100,12)) {Icon(Icons.AutoMirrored.Rounded.ArrowForward,"Próximo mês")}
    }
}
@Composable private fun BalanceCard(state: FinanceState,month: YearMonth) {
    var visible by rememberSaveable {mutableStateOf(true)}
    val total=state.accounts.sumOf {Ledger.balance(it,state.entries)}
    val projected=state.accounts.sumOf {Ledger.balance(it,state.entries,month.atEndOfMonth(),true)}
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Brush.linearGradient(listOf(Color(0xFF6738E8),Color(0xFF8050D8)))).padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) { Text("Saldo disponível hoje",Modifier.weight(1f),color=Color.White.copy(alpha=.9f)); IconButton(onClick={visible=!visible},modifier=Modifier.size(48.dp)) {Icon(if(visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,if(visible) "Ocultar valores" else "Mostrar valores",tint=Color.White)} }
        Text(if(visible) Money.format(total) else "R$ •••••",fontSize=32.sp,fontWeight=FontWeight.Bold,color=Color.White)
        if(total<0 && visible) Text("Seu saldo disponível está negativo",color=Color.White,fontWeight=FontWeight.SemiBold)
        Text("Contas, carteira, reservas e cofrinhos",color=Color.White.copy(alpha=.9f),fontSize=12.sp)
        HorizontalDivider(color=Color.White.copy(alpha=.25f))
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {Text("Entradas no mês",fontSize=12.sp,color=Color.White); Text(if(visible) "+ ${Money.format(Ledger.monthly(state.entries,month,EntryKind.INCOME))}" else "•••••",color=Color.White,fontWeight=FontWeight.Bold)}
            Column(Modifier.weight(1f)) {Text("Saídas no mês",fontSize=12.sp,color=Color.White); Text(if(visible) "− ${Money.format(Ledger.monthly(state.entries,month,EntryKind.EXPENSE))}" else "•••••",color=Color.White,fontWeight=FontWeight.Bold)}
        }
        Text(if(visible) "Previsão até ${month.atEndOfMonth().br()}: ${Money.format(projected)}" else "Previsão: •••••",fontSize=12.sp,color=Color.White.copy(alpha=.95f))
    }
}
@Composable private fun QuickAction(label: String,icon: ImageVector,modifier: Modifier,onClick:()->Unit) {
    Surface(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.primaryContainer) { Column(Modifier.padding(vertical=16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {Icon(icon,null,tint=MaterialTheme.colorScheme.primary);Text(label,style=MaterialTheme.typography.labelLarge)} }
}
@Composable internal fun SectionTitle(title: String,action: String?=null,onClick:()->Unit={}) {Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {Text(title,Modifier.weight(1f),fontSize=18.sp,fontWeight=FontWeight.Bold); if(action!=null) TextButton(onClick=onClick) {Text(action)}}}
@Composable internal fun HintCard(title:String,text:String,icon:ImageVector) {Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),shape=RoundedCornerShape(20.dp)) {Row(Modifier.fillMaxWidth().padding(18.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {Icon(icon,null,tint=MaterialTheme.colorScheme.primary);Column {Text(title,fontWeight=FontWeight.SemiBold);Text(text,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=5.dp))}}}}
@Composable private fun EmptyCard(title:String,text:String,action:String,onClick:()->Unit) {Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),shape=RoundedCornerShape(24.dp)) {Column(Modifier.fillMaxWidth().padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {ChipMark();Text(title,fontSize=21.sp,fontWeight=FontWeight.Bold);Text(text,color=MaterialTheme.colorScheme.onSurfaceVariant);Button(onClick=onClick,shape=RoundedCornerShape(14.dp)){Text(action)}}}}
private fun accountIcon(kind: AccountKind):ImageVector=when(kind){AccountKind.BANK->Icons.Rounded.AccountBalance;AccountKind.CASH->Icons.Rounded.Wallet;AccountKind.SAVINGS->Icons.Rounded.Savings;AccountKind.INVESTMENT->Icons.AutoMirrored.Rounded.TrendingUp;AccountKind.JAR->Icons.Rounded.EmojiObjects}
@Composable private fun AccountCard(account:Account,entries:List<Entry>,onClick:()->Unit) {
    val balance=Ledger.balance(account,entries)
    Surface(onClick=onClick,shape=RoundedCornerShape(22.dp),color=MaterialTheme.colorScheme.surface,modifier=Modifier.fillMaxWidth()) {Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer),contentAlignment=Alignment.Center) {Icon(accountIcon(account.kind),null,tint=MaterialTheme.colorScheme.primary)}
        Column(Modifier.weight(1f)) {Text(account.name,fontWeight=FontWeight.SemiBold);Text(listOf(account.institution,account.kind.label).filter {it.isNotBlank()}.joinToString(" · "),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
        Column(horizontalAlignment=Alignment.End) {Text(Money.format(balance),fontWeight=FontWeight.Bold,color=if(balance<0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface); if(balance<0) Text("Negativo",fontSize=11.sp,color=MaterialTheme.colorScheme.error)}
    }}
}
@Composable internal fun EntryCard(entry:Entry,accounts:List<Account>,onClick:()->Unit) {
    val color=when(entry.kind) {EntryKind.INCOME->positive();EntryKind.EXPENSE->MaterialTheme.colorScheme.error;EntryKind.TRANSFER->MaterialTheme.colorScheme.primary}
    val overdue=!entry.posted && entry.date<LocalDate.now()
    Surface(onClick=onClick,shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surface,modifier=Modifier.fillMaxWidth()) {Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(alpha=.12f)),contentAlignment=Alignment.Center) {Icon(when(entry.kind){EntryKind.INCOME->Icons.Rounded.SouthWest;EntryKind.EXPENSE->Icons.Rounded.NorthEast;EntryKind.TRANSFER->Icons.Rounded.SwapHoriz},null,tint=color)}
        Column(Modifier.weight(1f)) {Text(entry.title,fontWeight=FontWeight.SemiBold,maxLines=2,overflow=TextOverflow.Ellipsis);Text("${entry.date.br()} · ${accounts.firstOrNull {it.id==entry.accountId}?.name.orEmpty()}",fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant); Text(if(entry.posted) "${if(entry.autoPost) "Automático" else "Confirmado"} · ${entry.category}" else if(overdue) "Pendente · Vencido" else "Agendado${if(entry.autoPost) " · Automático" else if(entry.seriesId!=null) " · Mensal" else ""}",fontSize=11.sp,color=if(overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)}
        Text("${when(entry.kind){EntryKind.INCOME->"+";EntryKind.EXPENSE->"−";else->"↔"}} ${Money.format(entry.amount)}",fontSize=14.sp,fontWeight=FontWeight.Bold,color=color)
    }}
}
@Composable private fun DebtSummary(state:FinanceState,onClick:()->Unit={}) {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {DebtKind.entries.forEach {kind -> Surface(onClick=onClick,modifier=Modifier.weight(1f),shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surface) {Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {Text(kind.label,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(Money.format(state.debts.filter {it.kind==kind}.sumOf {Ledger.outstanding(it,state.entries)}),fontWeight=FontWeight.Bold,color=if(kind==DebtKind.RECEIVABLE) positive() else MaterialTheme.colorScheme.error)}}}}}
@Composable private fun DebtCard(debt:Debt,entries:List<Entry>,onEdit:()->Unit,onSettle:()->Unit) {
    val remaining=Ledger.outstanding(debt,entries)
    Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),shape=RoundedCornerShape(22.dp)) {Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Row {Column(Modifier.weight(1f)) {Text(debt.person,fontWeight=FontWeight.Bold,fontSize=18.sp);Text(debt.title,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text(debt.kind.label,color=if(debt.kind==DebtKind.RECEIVABLE) positive() else MaterialTheme.colorScheme.error,fontSize=12.sp)}
        Text(Money.format(remaining),fontWeight=FontWeight.Bold,fontSize=24.sp)
        Text(if(remaining==0L) "Quitado · Total ${Money.format(debt.amount)}" else "Vence em ${debt.due.br()}${if(debt.due<LocalDate.now()) " · Vencido" else ""} · Total ${Money.format(debt.amount)}",fontSize=12.sp,color=if(remaining>0 && debt.due<LocalDate.now()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {TextButton(onClick=onEdit){Text("Detalhes / editar")}; if(remaining>0) Button(onClick=onSettle,shape=RoundedCornerShape(12.dp)){Text("Registrar acerto")}}
    }}
}
@Composable private fun MovementScreenHeader(state:FinanceState,month:YearMonth) {
    val income=Ledger.monthly(state.entries,month,EntryKind.INCOME); val expense=Ledger.monthly(state.entries,month,EntryKind.EXPENSE)
    HintCard("Resultado do mês: ${Money.format(income-expense)}","Entradas e saídas confirmadas. Transferências entre suas contas não alteram o resultado.",Icons.Rounded.Insights)
}
@Composable private fun MovementFilters(query:String,onQuery:(String)->Unit,filter:String,onFilter:(String)->Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value=query,onValueChange=onQuery,label={Text("Buscar título ou categoria")},leadingIcon={Icon(Icons.Rounded.Search,null)},modifier=Modifier.fillMaxWidth(),singleLine=true,shape=RoundedCornerShape(16.dp))
        Choice("Filtrar movimentos",listOf("Todos","Entrada","Saída","Transferência"),filter,onFilter)
    }
}
