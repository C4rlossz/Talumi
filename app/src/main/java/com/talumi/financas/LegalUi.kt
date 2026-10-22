package com.talumi.financas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

enum class RequestKind(val label:String) { PRIVACY("Solicitação LGPD"), ACCESSIBILITY("Relatar barreira de acessibilidade") }

object LocalRequest {
    fun text(kind:RequestKind,topic:String,name:String,reply:String,details:String,version:String,date:LocalDate=LocalDate.now()):String {
        require(topic.isNotBlank() && topic.length<=120)
        require(name.length<=120 && reply.length<=120 && details.trim().length in 1..2000) {"Descreva seu pedido em até 2000 caracteres."}
        require(reply.isBlank() || reply.matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) {"Confira o e-mail de resposta ou deixe o campo vazio."}
        return """${kind.label} — Talumi
Data: ${date.br()}
Versão informada: $version
Assunto: $topic
Nome (opcional): ${name.trim().ifBlank {"Não informado"}}
Contato para resposta (opcional): ${reply.trim().ifBlank {"Não informado"}}

Pedido ou barreira relatada:
${details.trim()}

Este texto foi gerado localmente e não foi enviado. Não inclui registros financeiros, anexos ou identificadores do aparelho. Revise o texto antes de encaminhar ao contato público do autor.
"""
    }
}

@Composable internal fun LegalCenter(vm:FinanceViewModel,busy:Boolean,onDismiss:()->Unit) {
    var document by remember {mutableStateOf<Pair<String,String>?>(null)}
    var request by remember {mutableStateOf<RequestKind?>(null)}
    var status by remember {mutableStateOf("")}
    var erase by remember {mutableStateOf(false)}
    val online by vm.onlineRates.collectAsState()
    val contact=stringResource(R.string.author_contact_email)
    AlertDialog(onDismissRequest=onDismiss,title={Text("Privacidade e acessibilidade")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Responsável pelo projeto: Carlos Eduardo",fontWeight=FontWeight.Bold)
        SelectionContainer {Text(contact)}
        Text("Projeto pessoal de portfólio. Não há empresa ou CNPJ informado.",style=MaterialTheme.typography.bodySmall)
        listOf("Política de privacidade" to "PRIVACIDADE.md","Termos de uso" to "TERMOS.md","Cookies e armazenamento local" to "COOKIES.md","Declaração de acessibilidade" to "ACESSIBILIDADE.md","Autoria e créditos" to "AUTORIA.md","Licenças de terceiros" to "THIRD_PARTY_NOTICES.md","Licença MIT do projeto" to "LICENSE").forEach {(title,path)->
            OutlinedButton(onClick={document=title to path},modifier=Modifier.fillMaxWidth()) {Text(title)}
        }
        if(status.isNotBlank()) Text(status)
        HorizontalDivider()
        Row {Text("Consultar CDI e Selic online",Modifier.weight(1f));Switch(checked=online,onCheckedChange=vm::onlineRates,modifier=Modifier.semantics {contentDescription="Consultar CDI e Selic online"})}
        Text("O Banco Central recebe os dados técnicos usuais da conexão, como IP, mas o Talumi não envia sua carteira. Desligar afeta as próximas consultas; uma consulta em andamento pode terminar.",style=MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick={request=RequestKind.PRIVACY},modifier=Modifier.fillMaxWidth()) {Text("Gerar solicitação LGPD")}
        OutlinedButton(onClick={request=RequestKind.ACCESSIBILITY},modifier=Modifier.fillMaxWidth()) {Text("Formulário de acessibilidade")}
        Text("Os formulários geram um arquivo de texto local. Você decide se e como enviá-lo. Nenhum registro financeiro é anexado.",style=MaterialTheme.typography.bodySmall)
        TextButton(onClick={erase=true},enabled=!busy,colors=ButtonDefaults.textButtonColors(contentColor=MaterialTheme.colorScheme.error)) {Text("Excluir todos os registros locais")}
    }},confirmButton={TextButton(onClick=onDismiss) {Text("Concluir")}})
    document?.let {(title,path)->LegalDocument(title,path) {document=null}}
    request?.let {kind->LocalRequestForm(kind,busy,{request=null}) {uri,text->vm.saveRequest(uri,text) {request=null;status="Solicitação salva. Nenhuma mensagem foi enviada."}}}
    if(erase) {
        var confirmation by remember {mutableStateOf("")}
        AlertDialog(onDismissRequest={if(!busy) erase=false},title={Text("Excluir seus registros?")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("Esta ação apaga contas, lançamentos, dívidas, salários, investimentos e taxas salvas deste aplicativo. As preferências de tema e consultas online são mantidas. Exporte um backup antes se quiser conservar uma cópia.")
            Text("Arquivos de backup e solicitações já salvos fora do aplicativo continuam no local escolhido. A exclusão lógica não garante remoção forense da memória do celular.")
            OutlinedTextField(value=confirmation,onValueChange={confirmation=it},label={Text("Digite APAGAR para confirmar")},singleLine=true,modifier=Modifier.fillMaxWidth())
        }},confirmButton={TextButton(onClick={vm.eraseLocalRecords {erase=false}},enabled=!busy && confirmation=="APAGAR") {Text("Apagar registros")}},dismissButton={TextButton(onClick={erase=false},enabled=!busy) {Text("Cancelar")}})
    }
}

@Composable internal fun LegalDocument(title:String,path:String,onDismiss:()->Unit) {
    val context=LocalContext.current
    val text=remember(path) {runCatching {context.assets.open("legal/$path").bufferedReader(Charsets.UTF_8).use {it.readText()}}.getOrDefault("Documento indisponível nesta instalação. Contate o autor.")}
    FormDialog(title,false,"",onDismiss,onDismiss,saveLabel="Voltar") {
        SelectionContainer {Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            text.split("\n\n").filter {it.isNotBlank()}.forEach {paragraph->
                val isHeading=paragraph.startsWith("#")
                Text(paragraph.trim().trimStart('#',' '),modifier=if(isHeading) Modifier.semantics {heading()} else Modifier,fontWeight=if(isHeading) FontWeight.Bold else FontWeight.Normal)
            }
        }}
    }
}

@Composable private fun LocalRequestForm(kind:RequestKind,busy:Boolean,onDismiss:()->Unit,onSave:(android.net.Uri,String)->Unit) {
    var topic by remember {mutableStateOf(if(kind==RequestKind.PRIVACY) "Acesso e informações" else "Navegação / leitor de tela")}
    var name by remember {mutableStateOf("")};var reply by remember {mutableStateOf("")};var details by remember {mutableStateOf("")};var error by remember {mutableStateOf("")}
    var draft by remember {mutableStateOf("")}
    val version=stringResource(R.string.app_version)
    val contact=stringResource(R.string.author_contact_email)
    val exporter=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {uri->if(uri!=null) onSave(uri,draft)}
    FormDialog(kind.label,busy,error,onDismiss,{
        try {draft=LocalRequest.text(kind,topic,name,reply,details,version);exporter.launch("solicitacao-talumi-${LocalDate.now()}.txt")} catch(e:Exception) {error=e.message ?: "Confira os campos."}
    },saveLabel="Salvar arquivo de solicitação") {
        Text("Contato do autor: $contact")
        Text("Não informe CPF, dados bancários, diagnóstico médico, senhas ou dados de outras pessoas. Preencher não envia nada ao autor. Depois de salvar, revise o arquivo e envie apenas se desejar.",style=MaterialTheme.typography.bodySmall)
        Choice("Assunto",if(kind==RequestKind.PRIVACY) listOf("Acesso e informações","Correção","Exclusão","Portabilidade / cópia","Oposição ou revisão","Outro direito / dúvida") else listOf("Navegação / leitor de tela","Contraste ou tamanho do texto","Área de toque / interação","Outra barreira"),topic,{topic=it})
        Input("Nome (opcional)",name,{name=it});Input("E-mail para resposta (opcional)",reply,{reply=it})
        OutlinedTextField(value=details,onValueChange={details=it.take(2000)},label={Text(if(kind==RequestKind.PRIVACY) "Descreva o pedido" else "Descreva a barreira e a tela")},minLines=4,modifier=Modifier.fillMaxWidth())
        Text("Para dados que só estão no seu celular: use Exportar backup para obter cópia, edite os registros para corrigir ou use Excluir todos os registros locais. O autor não consegue consultar ou apagar remotamente o seu aparelho.",style=MaterialTheme.typography.bodySmall)
    }
}
