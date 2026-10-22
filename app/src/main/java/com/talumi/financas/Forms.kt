package com.talumi.financas

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate

@Composable fun Choice(label:String,options:List<String>,selected:String,onSelect:(String)->Unit) {
    var expanded by remember {mutableStateOf(false)}
    Column {
        Text(label,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Box {
            OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth(),shape=androidx.compose.foundation.shape.RoundedCornerShape(14.dp)) {Text(selected.ifBlank {"Escolha uma opção"},Modifier.weight(1f));Text("▾")}
            DropdownMenu(expanded=expanded,onDismissRequest={expanded=false},modifier=Modifier.heightIn(max=320.dp)) { options.forEach {option -> DropdownMenuItem(text={Text(option)},onClick={onSelect(option);expanded=false})} }
        }
    }
}
@Composable internal fun AccountChoice(label:String,accounts:List<Account>,id:Long,onSelect:(Long)->Unit) {
    val labels=accounts.map {"${it.name} · ${it.institution.ifBlank {it.kind.label}}"}
    val names=labels.mapIndexed {index,label -> if(labels.count {it==label}>1) "$label (${index+1})" else label}
    Choice(label,names,accounts.indexOfFirst {it.id==id}.let {names.getOrNull(it).orEmpty()}) {selected -> onSelect(accounts[names.indexOf(selected)].id)}
}
@Composable internal fun Input(label:String,value:String,onChange:(String)->Unit,money:Boolean=false,multiline:Boolean=false) {
    OutlinedTextField(value=value,onValueChange={onChange(it.take(if(multiline) 1000 else 120))},label={Text(label)},modifier=Modifier.fillMaxWidth(),singleLine=!multiline,minLines=if(multiline) 2 else 1,keyboardOptions=KeyboardOptions(keyboardType=if(money) KeyboardType.Decimal else KeyboardType.Text),shape=androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
}
@Composable internal fun DateInput(label:String,value:String,onChange:(String)->Unit) {
    val context=LocalContext.current
    val date=runCatching {parseDate(value)}.getOrDefault(LocalDate.now())
    OutlinedTextField(value=value,onValueChange={},readOnly=true,label={Text(label)},trailingIcon={IconButton(onClick={DatePickerDialog(context,{_,y,m,d->onChange(LocalDate.of(y,m+1,d).br())},date.year,date.monthValue-1,date.dayOfMonth).apply {
        datePicker.minDate=LocalDate.of(2000,1,1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        datePicker.maxDate=LocalDate.of(2100,12,31).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }.show()}) {Icon(Icons.Rounded.CalendarMonth,"Escolher data")}},modifier=Modifier.fillMaxWidth(),shape=androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
}
@Composable internal fun FormDialog(title:String,busy:Boolean,error:String,onDismiss:()->Unit,onSave:()->Unit,saveLabel:String="Salvar",saveEnabled:Boolean=true,content:@Composable ColumnScope.()->Unit) {
    Dialog(onDismissRequest={if(!busy) onDismiss()},properties=DialogProperties(usePlatformDefaultWidth=false,dismissOnBackPress=!busy,dismissOnClickOutside=!busy)) {
        Surface(modifier=Modifier.fillMaxWidth(.95f).widthIn(max=560.dp).fillMaxHeight(.94f).imePadding(),shape=androidx.compose.foundation.shape.RoundedCornerShape(28.dp),color=MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {Text(title,Modifier.weight(1f),fontSize=23.sp,fontWeight=FontWeight.Bold);IconButton(onClick=onDismiss,enabled=!busy) {Icon(Icons.Rounded.Close,"Fechar")}}
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical=12.dp),verticalArrangement=Arrangement.spacedBy(14.dp),content=content)
                if(error.isNotEmpty()) Text(error,color=MaterialTheme.colorScheme.error,modifier=Modifier.padding(vertical=8.dp).semantics {liveRegion=LiveRegionMode.Polite})
                Button(onClick=onSave,enabled=!busy && saveEnabled,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),shape=androidx.compose.foundation.shape.RoundedCornerShape(16.dp)) {Text(if(busy) "Salvando…" else saveLabel)}
            }
        }
    }
}
@Composable internal fun DeleteButton(label:String="Excluir registro",busy:Boolean,onDelete:()->Unit) {
    var confirm by remember {mutableStateOf(false)}
    TextButton(onClick={confirm=true},enabled=!busy,colors=ButtonDefaults.textButtonColors(contentColor=MaterialTheme.colorScheme.error)) {Icon(Icons.Rounded.DeleteOutline,null);Spacer(Modifier.width(8.dp));Text(label)}
    if(confirm) AlertDialog(onDismissRequest={confirm=false},title={Text("Excluir este registro?")},text={Text("A exclusão é permanente. Os saldos e valores pendentes serão recalculados. Para guardar uma cópia, exporte um backup antes.")},confirmButton={TextButton(enabled=!busy,onClick={onDelete();confirm=false}){Text("Excluir")}},dismissButton={TextButton(onClick={confirm=false}){Text("Cancelar")}})
}
@Composable fun AccountForm(original:Account,state:FinanceState,busy:Boolean,onDismiss:()->Unit,onSave:(Account)->Unit,onDelete:()->Unit) {
    var name by remember {mutableStateOf(original.name)}
    var bank by remember {mutableStateOf(original.institution)}
    var kind by remember {mutableStateOf(original.kind)}
    var opening by remember {mutableStateOf(Money.edit(original.opening))}
    var error by remember {mutableStateOf("")}
    FormDialog(if(original.id==0L) "Nova conta" else "Editar conta",busy,error,onDismiss,{try {val amount=Money.parse(opening,true);require(name.isNotBlank()) {"Dê um nome para esta conta."};onSave(original.copy(name=name,institution=bank,kind=kind,opening=amount))} catch(e:Exception){error=e.message ?: "Confira os dados."}}) {
        Input("Nome da conta",name,{name=it})
        Input("Banco / instituição (opcional)",bank,{bank=it})
        Choice("Onde está o dinheiro?",AccountKind.entries.filter {it!=AccountKind.INVESTMENT}.map {it.label},kind.label,{label->kind=AccountKind.entries.first {it.label==label}})
        Input("Saldo inicial (R$)",opening,{opening=it})
        Text("Use vírgula para centavos e nenhum ponto. Para saldo negativo: -150,00. O saldo inicial é o ponto de partida; os lançamentos serão somados a ele.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        if(original.id>0) {
            Text("Saldo atual: ${Money.format(Ledger.balance(original,state.entries))}",fontWeight=FontWeight.SemiBold)
            Text("Alterar o saldo inicial ajusta também os saldos calculados desta conta.",style=MaterialTheme.typography.bodySmall)
            DeleteButton("Excluir conta sem histórico",busy,onDelete)
        }
    }
}
@Composable fun EntryForm(original:Entry,state:FinanceState,busy:Boolean,onDismiss:()->Unit,onSave:(Entry,Boolean)->Unit) {
    val choices=if(original.id>0) state.accounts else state.financeAccounts
    var title by remember {mutableStateOf(original.title)}
    var amount by remember {mutableStateOf(if(original.amount==0L) "" else Money.edit(original.amount))}
    var kind by remember {mutableStateOf(original.kind)}
    var account by remember {mutableLongStateOf(original.accountId)}
    var destination by remember {mutableLongStateOf(original.toAccountId ?: choices.firstOrNull {it.id!=original.accountId}?.id ?: 0)}
    var date by remember {mutableStateOf(original.date.br())}
    var posted by remember {mutableStateOf(original.posted)}
    var category by remember {mutableStateOf(original.category)}
    var note by remember {mutableStateOf(original.note)}
    var recurring by remember {mutableStateOf(false)}
    var error by remember {mutableStateOf("")}
    FormDialog(if(original.id==0L) "Novo lançamento" else "Editar lançamento",busy,error,onDismiss,{
        try {
            require(title.isNotBlank()) {"Informe uma descrição."}
            val d=parseDate(date);val cents=Money.parse(amount)
            require(!posted || d<=LocalDate.now()) {"Para uma data futura, escolha Agendado."}
            if(kind==EntryKind.TRANSFER) require(destination!=account && destination>0) {"Escolha uma conta de destino diferente."}
            require(category.isNotBlank()) {"Informe uma categoria."}
            onSave(original.copy(title=title,amount=cents,kind=kind,accountId=account,toAccountId=if(kind==EntryKind.TRANSFER) destination else null,date=d,posted=posted && !recurring,category=if(kind==EntryKind.TRANSFER) "Transferência" else category,note=note,autoPost=false),recurring)
        } catch(e:Exception){error=e.message ?: "Confira os dados."}
    },saveEnabled=choices.isNotEmpty()) {
        if(original.debtId==null) Choice("Tipo de movimento",EntryKind.entries.map {it.label},kind.label,{label->kind=EntryKind.entries.first {it.label==label};if(kind==EntryKind.TRANSFER) recurring=false})
        else Text("Acerto vinculado a uma dívida. O valor restante será recalculado.",style=MaterialTheme.typography.bodySmall)
        Input("Descrição",title,{title=it})
        Input("Valor (R$)",amount,{amount=it},money=true)
        Text("Exemplo: 1250,50 · Sem separador de milhar",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        AccountChoice(if(kind==EntryKind.TRANSFER) "Conta de origem" else "Conta",choices,account,{account=it})
        if(kind==EntryKind.TRANSFER) {
            AccountChoice("Conta de destino",choices.filter {it.id!=account},destination,{destination=it})
            Text("Transferências entre contas não são receitas nem despesas. Para investir, use Aportar na carteira de investimentos.",style=MaterialTheme.typography.bodySmall)
        } else Input("Categoria",category,{category=it})
        DateInput(if(recurring) "Primeira ocorrência" else "Data",date,{date=it})
        if(original.debtId==null && !recurring) Choice("Situação",listOf("Confirmado","Agendado"),if(posted) "Confirmado" else "Agendado",{posted=it=="Confirmado"})
        if(original.id==0L && kind!=EntryKind.TRANSFER && original.debtId==null) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {Text("Repetir todo mês",Modifier.weight(1f));Switch(checked=recurring,onCheckedChange={recurring=it;if(it) posted=false},modifier=Modifier.semantics {contentDescription="Repetir todo mês"})}
            if(recurring) Text("Cria agendamentos mensais, inclusive o primeiro. Confirme cada ocorrência quando o dinheiro entrar ou sair. Em meses curtos, o vencimento vai para o último dia.",style=MaterialTheme.typography.bodySmall)
        }
        if(original.seriesId!=null) Text("Esta edição altera apenas esta ocorrência. Para encerrar as próximas, abra Agenda → Gerenciar recorrências.",style=MaterialTheme.typography.bodySmall)
        if(original.autoPost) Text("Ao editar este lançamento, a confirmação desta ocorrência passa a ser manual. Os outros recebimentos continuam automáticos.",style=MaterialTheme.typography.bodySmall)
        Input("Observação (opcional)",note,{note=it},multiline=true)
    }
}
@Composable fun DebtForm(original:Debt,state:FinanceState,busy:Boolean,onDismiss:()->Unit,onSave:(Debt)->Unit,onDelete:()->Unit) {
    var person by remember {mutableStateOf(original.person)};var title by remember {mutableStateOf(original.title)}
    var amount by remember {mutableStateOf(if(original.amount==0L) "" else Money.edit(original.amount))}
    var kind by remember {mutableStateOf(original.kind)};var due by remember {mutableStateOf(original.due.br())}
    var note by remember {mutableStateOf(original.note)};var error by remember {mutableStateOf("")}
    FormDialog(if(original.id==0L) "Novo compromisso" else "Editar compromisso",busy,error,onDismiss,{
        try {require(person.isNotBlank() && title.isNotBlank()) {"Preencha a pessoa e a descrição."};onSave(original.copy(person=person,title=title,amount=Money.parse(amount),kind=kind,due=parseDate(due),note=note))} catch(e:Exception){error=e.message ?: "Confira os dados."}
    }) {
        Choice("Quem deve?",DebtKind.entries.map {it.label},kind.label,{label->kind=DebtKind.entries.first {it.label==label}})
        Input("Pessoa",person,{person=it});Input("Descrição do combinado",title,{title=it});Input("Valor total (R$)",amount,{amount=it},money=true)
        DateInput("Vencimento combinado",due,{due=it})
        Input("Observação (opcional)",note,{note=it},multiline=true)
        Text("Este cadastro acompanha o compromisso e não movimenta uma conta. Se você emprestar dinheiro agora, registre também a saída correspondente em Movimentos.",style=MaterialTheme.typography.bodySmall)
        if(original.id>0) {
            Text("Já quitado: ${Money.format(original.amount-Ledger.outstanding(original,state.entries))}",fontWeight=FontWeight.Bold)
            val settlements=state.entries.filter {it.debtId==original.id}.sortedBy {it.date}
            settlements.forEach {Text("${it.date.br()} · ${Money.format(it.amount)} · ${state.accounts.firstOrNull {a->a.id==it.accountId}?.name.orEmpty()}",style=MaterialTheme.typography.bodySmall)}
            Text("Acertos podem ser editados ou excluídos em Movimentos.",style=MaterialTheme.typography.bodySmall)
            DeleteButton(busy=busy,onDelete=onDelete)
        }
    }
}
@Composable fun SettleForm(debt:Debt,state:FinanceState,busy:Boolean,onDismiss:()->Unit,onSave:(Long,Long,LocalDate)->Unit) {
    val remaining=Ledger.outstanding(debt,state.entries)
    var amount by remember {mutableStateOf(Money.edit(remaining))};var account by remember {mutableLongStateOf(state.financeAccounts.firstOrNull()?.id ?: 0)}
    var date by remember {mutableStateOf(LocalDate.now().br())};var error by remember {mutableStateOf("")}
    FormDialog("Registrar acerto",busy,error,onDismiss,{
        try {val cents=Money.parse(amount);require(cents<=remaining){"O acerto não pode superar o valor restante."};val d=parseDate(date);require(d<=LocalDate.now()) {"O acerto deve ter acontecido até hoje."};onSave(account,cents,d)} catch(e:Exception){error=e.message ?: "Confira os dados."}
    },saveLabel=if(debt.kind==DebtKind.RECEIVABLE) "Confirmar recebimento" else "Confirmar pagamento",saveEnabled=state.financeAccounts.isNotEmpty()) {
        Text("${debt.person} · ${debt.title}",fontWeight=FontWeight.Bold)
        Text("${debt.kind.label}: ${Money.format(remaining)}")
        if(state.financeAccounts.isEmpty()) Text("Cadastre uma conta na aba Contas antes de registrar o acerto.",color=MaterialTheme.colorScheme.error)
        else AccountChoice(if(debt.kind==DebtKind.RECEIVABLE) "Receber na conta" else "Pagar com a conta",state.financeAccounts,account,{account=it})
        Input("Valor deste acerto (R$)",amount,{amount=it},money=true)
        DateInput("Data do acerto",date,{date=it})
        Text("Você pode registrar um valor parcial. O saldo da conta e o restante do compromisso serão atualizados juntos.",style=MaterialTheme.typography.bodySmall)
    }
}
@Composable fun EntryDetail(entry:Entry,state:FinanceState,busy:Boolean,onDismiss:()->Unit,onEdit:()->Unit,onDelete:()->Unit,onConfirm:()->Unit) {
    AlertDialog(onDismissRequest=onDismiss,title={Text(entry.title)},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(Money.format(entry.amount),fontSize=30.sp,fontWeight=FontWeight.Bold)
        Text("${entry.kind.label} · ${entry.date.br()} · ${if(entry.posted) "Confirmado" else "Agendado"}")
        Text("Conta: ${state.accounts.firstOrNull {it.id==entry.accountId}?.name.orEmpty()}")
        entry.toAccountId?.let {id->Text("Destino: ${state.accounts.firstOrNull {it.id==id}?.name.orEmpty()}")}
        Text("Categoria: ${entry.category}")
        if(entry.note.isNotBlank()) Text(entry.note)
        if(!entry.posted) {
            if(entry.date<=LocalDate.now()) Button(onClick=onConfirm,enabled=!busy,modifier=Modifier.fillMaxWidth()){Text("Confirmar ${if(entry.kind==EntryKind.INCOME) "recebimento" else if(entry.kind==EntryKind.EXPENSE) "pagamento" else "transferência"}")}
            else Text("Na data agendada você poderá confirmar. Se já aconteceu, edite a data para o dia real.",style=MaterialTheme.typography.bodySmall)
        }
        OutlinedButton(onClick=onEdit,enabled=!busy,modifier=Modifier.fillMaxWidth()){Text("Editar lançamento")}
        DeleteButton(busy=busy,onDelete=onDelete)
    }},confirmButton={TextButton(onClick=onDismiss){Text("Fechar")}})
}
@Composable fun SeriesDialog(state:FinanceState,busy:Boolean,onDismiss:()->Unit,onStop:(Long)->Unit,onEnableAutomatic:(Long)->Unit) {
    var selected by remember {mutableStateOf<Series?>(null)}
    var automatic by remember {mutableStateOf<Series?>(null)}
    AlertDialog(onDismissRequest=onDismiss,title={Text("Recorrências mensais")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text("Salários automáticos entram no saldo na data configurada. As outras recorrências aguardam confirmação. Para mudar valor ou dia, encerre a recorrência e crie outra.",style=MaterialTheme.typography.bodySmall)
        if(state.series.isEmpty()) Text("Crie um lançamento e ative Repetir todo mês.")
        state.series.sortedByDescending {it.active}.forEach {s -> Column {
            Text(s.title,fontWeight=FontWeight.Bold); Text("${Money.format(s.amount)} · ${if(s.rule==PayRule.FIXED_DAY) "Dia ${s.day}" else "5º dia útil"} · ${if(s.active) "Ativa" else "Encerrada"}",style=MaterialTheme.typography.bodySmall)
            Text(if(s.autoPost) "Recebimento automático" else "Confirmação manual",style=MaterialTheme.typography.bodySmall)
            if(s.active && s.kind==EntryKind.INCOME && !s.autoPost) TextButton(onClick={automatic=s},enabled=!busy) {Text("Ativar recebimento automático")}
            if(s.active) TextButton(onClick={selected=s},enabled=!busy) {Text("Encerrar próximas ocorrências")}
        } }
    }},confirmButton={TextButton(onClick=onDismiss){Text("Concluir")}})
    selected?.let {s->AlertDialog(onDismissRequest={selected=null},title={Text("Encerrar ${s.title}?")},text={Text("Os agendamentos futuros desta recorrência serão removidos. O histórico confirmado e as pendências até hoje serão mantidos.")},confirmButton={TextButton(enabled=!busy,onClick={onStop(s.id);selected=null}){Text("Encerrar")}},dismissButton={TextButton(onClick={selected=null}){Text("Cancelar")}})}
    automatic?.let {s->AlertDialog(onDismissRequest={automatic=null},title={Text("Ativar recebimento automático?")},text={Text("${s.title} será registrado automaticamente na conta a partir de hoje, usando a mesma regra mensal. Não cria outra recorrência nem altera os lançamentos anteriores a hoje. Isso registra o saldo no Talumi e não consulta seu banco.")},confirmButton={TextButton(enabled=!busy,onClick={onEnableAutomatic(s.id);automatic=null}){Text("Ativar")}},dismissButton={TextButton(onClick={automatic=null}){Text("Cancelar")}})}
}

@Composable fun SalaryForm(state: FinanceState,busy: Boolean,onDismiss: ()->Unit,onSave: (Series)->Unit) {
    var title by remember {mutableStateOf("Salário")}
    var amount by remember {mutableStateOf("")}
    var account by remember {mutableLongStateOf(state.financeAccounts.firstOrNull()?.id ?: 0)}
    var rule by remember {mutableStateOf(PayRule.FIXED_DAY)}
    var day by remember {mutableStateOf("5")}
    var start by remember {mutableStateOf(LocalDate.now().br())}
    var saturday by remember {mutableStateOf(false)}
    var holidays by remember {mutableStateOf("")}
    var error by remember {mutableStateOf("")}
    fun candidate(): Series {
        val holidayDates=if(rule==PayRule.FIFTH_BUSINESS_DAY) holidays.lineSequence().filter {it.isNotBlank()}.map {parseDate(it.trim())}.toSet() else emptySet()
        val fixedDay=if(rule==PayRule.FIXED_DAY) day.toIntOrNull() ?: 0 else 5
        require(fixedDay in 1..31) { "Escolha um dia de 1 a 31." }
        require(title.isNotBlank()) { "Informe o nome do recebimento." }
        return Series(title=title,amount=Money.parse(amount),kind=EntryKind.INCOME,accountId=account,start=parseDate(start),category="Salário",rule=rule,day=fixedDay,autoPost=true,includeSaturday=saturday,holidays=holidayDates)
    }
    FormDialog("Salário recorrente",busy,error,onDismiss,{
        try { val salary=candidate(); Ledger.nextOccurrence(salary,salary.start);onSave(salary) } catch(e:Exception) {error=e.message ?: "Confira os dados."}
    },saveLabel="Cadastrar salário automático",saveEnabled=state.financeAccounts.isNotEmpty()) {
        Text("Configure uma vez. O Talumi registra os recebimentos no saldo local nas datas combinadas.",style=MaterialTheme.typography.bodyMedium)
        if(state.financeAccounts.isEmpty()) Text("Adicione uma conta na aba Contas antes de cadastrar seu salário.",color=MaterialTheme.colorScheme.error)
        Input("Nome / emprego",title,{title=it})
        Input("Salário líquido (R$)",amount,{amount=it},money=true)
        AccountChoice("Receber na conta",state.financeAccounts,account,{account=it})
        Choice("Quando você recebe?",PayRule.entries.map {it.label},rule.label,{label->rule=PayRule.entries.first {it.label==label}})
        if(rule==PayRule.FIXED_DAY) {
            Input("Dia do pagamento (1 a 31)",day,{day=it.filter(Char::isDigit).take(2)},money=true)
            Text("Se o mês não tiver esse dia, será usado o último dia do mês. Fins de semana não alteram uma data fixa.",style=MaterialTheme.typography.bodySmall)
        } else {
            Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {Text("Contar sábados como dia útil",Modifier.weight(1f));Switch(checked=saturday,onCheckedChange={saturday=it},modifier=Modifier.semantics {contentDescription="Contar sábados como dia útil"})}
            Text("Domingos não contam. Por padrão, são contados os dias de segunda a sexta. Informe abaixo os feriados e folgas que seu emprego não considera; não há calendário de feriados automático.",style=MaterialTheme.typography.bodySmall)
            Input("Feriados / folgas (dd/MM/aaaa, um por linha)",holidays,{holidays=it},multiline=true)
        }
        DateInput("Começar a partir de",start,{start=it})
        val first=runCatching {candidate().let {Ledger.nextOccurrence(it,it.start)}}.getOrNull()
        if(first!=null) Text("Primeiro recebimento: ${first.br()}",fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
        Text("Escolha hoje ou uma data futura para não somar salários já incluídos no saldo inicial. Se escolher uma data passada, os recebimentos vencidos serão registrados agora.",style=MaterialTheme.typography.bodySmall)
        Text("O saldo é atualizado ao abrir o app e também em segundo plano quando o Android permitir. Não é uma confirmação bancária. Se houver atraso ou diferença no salário, edite a ocorrência em Movimentos.",style=MaterialTheme.typography.bodySmall)
    }
}
