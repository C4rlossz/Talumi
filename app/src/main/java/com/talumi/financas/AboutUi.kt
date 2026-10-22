package com.talumi.financas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable internal fun AboutTalumi(onDismiss: ()->Unit) {
    val uriHandler = LocalUriHandler.current
    val github = stringResource(R.string.creator_profile_github_url)
    val linkedin = stringResource(R.string.creator_linkedin_url)
    val project = stringResource(R.string.creator_github_url)
    val version = stringResource(R.string.app_version)
    var linkError by remember { mutableStateOf(false) }
    fun open(url: String) {
        try { uriHandler.openUri(url); linkError = false }
        catch(_: android.content.ActivityNotFoundException) { linkError = true }
        catch(_: SecurityException) { linkError = true }
        catch(_: IllegalArgumentException) { linkError = true }
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Sobre o Talumi") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ChipMark()
            Text("Suas finanças, com clareza.", fontWeight = FontWeight.Bold)
            Text("Talumi $version · Android")
            Text("Tecnologias", fontWeight = FontWeight.SemiBold)
            Text("Kotlin e Jetpack Compose na interface, SQLite para o banco de dados local e WorkManager para tarefas agendadas.")
            HorizontalDivider()
            Text("Privacidade e proteção", fontWeight = FontWeight.SemiBold)
            Text("Seus registros financeiros ficam no armazenamento privado do aplicativo, isolado pelo Android. O autor não recebe automaticamente seus registros. Não há login, anúncios, rastreadores nem conexão com suas contas bancárias.")
            Text("Você pode usar as finanças sem internet. A consulta opcional de CDI e Selic acessa o Banco Central por HTTPS, sem enviar sua carteira ou seus valores. É possível desativá-la na área de privacidade.")
            Text("Backups exportados não são criptografados: guarde-os em um local protegido. Os links abaixo abrem serviços externos quando você tocar neles.", style = MaterialTheme.typography.bodySmall)
            HorizontalDivider()
            Text("Criado por Carlos Eduardo", fontWeight = FontWeight.Bold)
            Text("Projeto autoral de portfólio. Código sob licença MIT, preservando o aviso de copyright e a licença. Bibliotecas e ícones mantêm suas licenças próprias.")
            OutlinedButton(onClick = {open(github)}, modifier = Modifier.fillMaxWidth()) { Text("GitHub de Carlos Eduardo") }
            OutlinedButton(onClick = {open(linkedin)}, modifier = Modifier.fillMaxWidth()) { Text("LinkedIn de Carlos Eduardo") }
            TextButton(onClick = {open(project)}, modifier = Modifier.fillMaxWidth()) { Text("Código do Talumi no GitHub") }
            if(linkError) Text("Não foi possível abrir o link. Verifique se há um navegador disponível.", color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = {TextButton(onClick = onDismiss) {Text("Concluir")}})
}
