package com.talumi.financas

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import org.junit.Assert.assertEquals
import java.time.LocalDate
import java.time.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28])
class AppFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun reset() {
        // The application is empty when this separate test class starts.
        compose.waitUntil(20000) {compose.onAllNodesWithText("Seu dinheiro, com clareza.").fetchSemanticsNodes().isNotEmpty()}
    }
    @Test fun createAccountAndIncomeThenNavigateAndChangeTheme() {
        compose.onNode(hasText("Contas") and hasClickAction()).performClick()
        compose.onNodeWithContentDescription("Nova conta").performClick()
        compose.onNodeWithText("Nome da conta").performTextInput("Banco teste")
        compose.onNodeWithText("Saldo inicial (R$)").performScrollTo().performTextReplacement("100,00")
        compose.onNodeWithText("Salvar").performClick()
        compose.waitUntil(20000) {compose.onAllNodesWithText("Nova conta").fetchSemanticsNodes().isEmpty()}
        compose.waitUntil(20000) {compose.onAllNodesWithText("Banco teste").fetchSemanticsNodes().isNotEmpty()}
        compose.waitUntil(10000) {compose.onAllNodesWithText("Conta salva").fetchSemanticsNodes().isEmpty()}
        compose.onNode(hasText("Início") and hasClickAction()).performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Entrada"))
        compose.onNodeWithText("Entrada").performClick()
        try {
            compose.waitUntil(20000) {compose.onAllNodesWithText("Descrição").fetchSemanticsNodes().isNotEmpty()}
        } catch(e:Exception) {
            println(compose.onRoot().printToString())
            throw e
        }
        compose.onNodeWithText("Descrição").performTextInput("Freelance teste")
        compose.onNodeWithText("Valor (R$)").performTextInput("25,50")
        compose.onNodeWithText("Salvar").performClick()
        compose.waitUntil(20000) {compose.onAllNodesWithText(Money.format(12550)).fetchSemanticsNodes().isNotEmpty()}
        compose.onNode(hasText("Movimentos") and hasClickAction()).performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Freelance teste"))
        compose.onNodeWithText("Freelance teste").assertExists()
        compose.onNode(hasText("Agenda") and hasClickAction()).performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Gerenciar recorrências mensais"))
        compose.onNodeWithText("Gerenciar recorrências mensais").assertExists()
        compose.onNode(hasText("Pessoas") and hasClickAction()).performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Adicionar compromisso"))
        compose.onNodeWithText("Adicionar compromisso").assertExists()
        compose.onNodeWithContentDescription("Configurações e backup").performClick()
        compose.onNodeWithText("Sistema").performClick()
        compose.onNodeWithText("Escuro").performClick()
        compose.onNodeWithText("Criado por Carlos Eduardo").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Projeto autoral de portfólio").assertExists()
        compose.onNodeWithText("Concluir").performClick()
        compose.onNode(hasText("Agenda") and hasClickAction()).performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Cadastrar salário recorrente"))
        compose.onNodeWithText("Cadastrar salário recorrente").performClick()
        compose.onNodeWithText("Salário líquido (R$)").performScrollTo().performTextInput("3000,00")
        compose.onNodeWithText("Dia fixo do mês").performScrollTo().performClick()
        compose.onNodeWithText("Quinto dia útil").performClick()
        compose.onNodeWithText("Cadastrar salário automático").performClick()
        compose.waitUntil(20000) {compose.onAllNodesWithText("Salário recorrente").fetchSemanticsNodes().isEmpty()}
        compose.waitUntil(10000) {compose.onAllNodesWithText("Salário recorrente cadastrado").fetchSemanticsNodes().isEmpty()}
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Gerenciar recorrências mensais"))
        compose.onNodeWithText("Gerenciar recorrências mensais").performClick()
        compose.onNodeWithText("Recebimento automático").assertExists()
        compose.onNodeWithText("5º dia útil",substring=true).assertExists()
        compose.onNodeWithText("Concluir").performClick()
        compose.onNode(hasText("Contas") and hasClickAction()).performClick()
        compose.onNodeWithText("Banco teste").assertExists()
        verifyInvestmentWallet()
        verifyReportsAndAbout()
        verifyPrivacyControls()
    }

    private fun verifyReportsAndAbout() {
        compose.onNode(hasText("Relatórios") and hasClickAction()).performClick()
        compose.onNodeWithText("Relatório do mês").assertIsDisplayed()
        compose.onNodeWithText("Resultado do mês").assertExists()
        compose.onNodeWithContentDescription("Gráfico do mês.",substring=true).assertExists()
        compose.onNodeWithContentDescription("Novo lançamento").assertDoesNotExist()
        compose.onNodeWithContentDescription("Mês anterior").performClick()
        compose.onNodeWithText("Sem movimentos").assertExists()
        compose.onNodeWithContentDescription("Próximo mês").performClick()
        compose.onNodeWithText("Resultado positivo").assertExists()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Ver movimentos do mês"))
        compose.onNodeWithText("Ver movimentos do mês").performScrollTo().performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Freelance teste"))
        compose.onNodeWithText("Freelance teste").assertExists()
        compose.onNodeWithContentDescription("Informações do aplicativo").performClick()
        compose.onNodeWithText("Sobre o Talumi").assertExists()
        compose.onNodeWithText("Talumi 1.4.0 · Android").assertExists()
        compose.onNodeWithText("Tecnologias").assertExists()
        compose.onNodeWithText("GitHub de Carlos Eduardo").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("LinkedIn de Carlos Eduardo").performScrollTo().assertIsDisplayed()
        assertEquals("https://github.com/C4rlossz",compose.activity.getString(R.string.creator_profile_github_url))
        assertEquals("https://www.linkedin.com/in/carlos-eduardo-a8bb50268/",compose.activity.getString(R.string.creator_linkedin_url))
        compose.onNodeWithText("Concluir").performClick()
        compose.onNodeWithContentDescription("Configurações e backup").performClick()
        compose.onNodeWithText("Escuro").performClick()
        compose.onNodeWithText("Claro").performClick()
        compose.onNodeWithText("Concluir").performClick()
        compose.onNode(hasText("Relatórios") and hasClickAction()).performClick()
        compose.onNodeWithText("Resultado positivo").assertExists()
    }

    private fun verifyInvestmentWallet() {
        val db=(compose.activity.application as TalumiApplication).financeDb
        lateinit var vm:FinanceViewModel
        compose.runOnIdle {
            listOf("CDI","SELIC").forEach {db.saveBenchmark(Benchmark(it,"13.65",LocalDate.now(),Instant.now().toString()))}
            vm=ViewModelProvider(compose.activity)[FinanceViewModel::class.java];vm.refresh()
        }
        compose.waitUntil(20000) {compose.onAllNodes(isRoot()).fetchSemanticsNodes();vm.state.value.benchmarks.size==2 && vm.state.value.accounts.isNotEmpty()}
        val initialCash=Ledger.available(vm.state.value)
        val initialEntries=vm.state.value.entries.size
        compose.onNodeWithText("Investimentos").performClick()
        compose.onNodeWithText("Sua carteira de investimentos").assertExists()
        compose.onNodeWithContentDescription("Novo investimento").performClick()
        compose.onNodeWithText("Nome da aplicação").performTextInput("CDB da reserva")
        compose.onNodeWithText("Banco / instituição").performTextInput("Banco da carteira")
        compose.onNodeWithText("Outro / a classificar").performScrollTo().performClick()
        compose.onNodeWithText("CDB",useUnmergedTree=true).performClick()
        compose.onNodeWithText("Valor já investido (R$)").performScrollTo().performTextReplacement("500,00")
        compose.onNodeWithText("Salvar").performClick()
        compose.waitUntil(20000) {compose.onAllNodes(isRoot()).fetchSemanticsNodes();vm.state.value.investments.size==1}
        compose.waitUntil(10000) {compose.onAllNodesWithText("Novo investimento").fetchSemanticsNodes().isEmpty()}
        compose.waitUntil(10000) {compose.onAllNodesWithText("Investimento salvo").fetchSemanticsNodes().isEmpty()}
        val id=vm.state.value.investments.single().accountId
        assertEquals(initialCash,Ledger.available(vm.state.value))
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Distribuição por banco"))
        compose.onNodeWithText("Distribuição por banco").assertIsDisplayed()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Aportar"))
        compose.onNodeWithText("Aportar").performScrollTo().performClick()
        try {compose.waitUntil(10000) {compose.onAllNodesWithText("Valor (R$)").fetchSemanticsNodes().isNotEmpty()}} catch(e:Throwable) {println(compose.onAllNodes(isRoot()).printToString());throw e}
        compose.onNodeWithText("Valor (R$)").performTextInput("100,00")
        compose.onNodeWithText("Registrar movimento").performClick()
        compose.waitUntil(20000) {compose.onAllNodes(isRoot()).fetchSemanticsNodes();Ledger.available(vm.state.value)==initialCash-10000L}
        compose.waitUntil(10000) {compose.onAllNodesWithText("Movimentar investimento").fetchSemanticsNodes().isEmpty()}
        compose.waitUntil(10000) {compose.onAllNodesWithText("Movimento da carteira registrado").fetchSemanticsNodes().isEmpty()}
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Registrar rendimento ou perda"))
        compose.onNodeWithText("Registrar rendimento ou perda").performScrollTo().performClick()
        try {compose.waitUntil(10000) {compose.onAllNodesWithText("Valor (R$)").fetchSemanticsNodes().isNotEmpty()}} catch(e:Throwable) {println(compose.onAllNodes(isRoot()).printToString());throw e}
        compose.onNodeWithText("Valor (R$)").performTextInput("10,00")
        compose.onNodeWithText("Registrar movimento").performClick()
        compose.waitUntil(20000) {compose.onAllNodes(isRoot()).fetchSemanticsNodes();vm.state.value.entries.size==initialEntries+2}
        compose.waitUntil(10000) {compose.onAllNodesWithText("Movimentar investimento").fetchSemanticsNodes().isEmpty()}
        assertEquals(initialCash-10000L,Ledger.available(vm.state.value))
        compose.waitUntil(10000) {compose.onAllNodesWithText("Movimento da carteira registrado").fetchSemanticsNodes().isEmpty()}
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Resgatar"))
        compose.onNodeWithText("Resgatar").performScrollTo().performClick()
        try {compose.waitUntil(10000) {compose.onAllNodesWithText("Valor (R$)").fetchSemanticsNodes().isNotEmpty()}} catch(e:Throwable) {println(compose.onAllNodes(isRoot()).printToString());throw e}
        compose.onNodeWithText("Valor (R$)").performTextInput("50,00")
        compose.onNodeWithText("Registrar movimento").performClick()
        compose.waitUntil(20000) {compose.onAllNodes(isRoot()).fetchSemanticsNodes();Ledger.available(vm.state.value)==initialCash-5000L}
        compose.waitUntil(10000) {compose.onAllNodesWithText("Movimentar investimento").fetchSemanticsNodes().isEmpty()}
        assertEquals(56000L,Ledger.balance(vm.state.value.accounts.first {it.id==id},vm.state.value.entries))
        compose.onNodeWithText("Finanças").performClick()
        compose.onNode(hasText("Início") and hasClickAction()).performClick()
        compose.onNode(hasScrollAction()).performScrollToIndex(0)
        try {compose.waitUntil(10000) {compose.onAllNodesWithText("Saldo disponível hoje").fetchSemanticsNodes().isNotEmpty()}} catch(e:Throwable) {println(compose.onAllNodes(isRoot()).printToString());throw e}
        compose.onNodeWithText("Saldo disponível hoje").assertExists()
        compose.onNode(hasText("Contas") and hasClickAction()).performClick()
        compose.onNodeWithText("Banco teste").assertExists()
        compose.onNodeWithText("CDB da reserva").assertDoesNotExist()
    }
    private fun verifyPrivacyControls() {
        val vm=ViewModelProvider(compose.activity)[FinanceViewModel::class.java]
        compose.onNodeWithContentDescription("Configurações e backup").performClick()
        compose.onNodeWithText("Privacidade, acessibilidade e licenças").performScrollTo().performClick()
        compose.waitUntil(10000) {compose.onAllNodesWithText("Privacidade e acessibilidade").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithText("Carlosz.eduardo1402@gmail.com").assertExists()
        compose.onNodeWithText("Política de privacidade").performScrollTo().performClick()
        compose.onNodeWithText("Versão documental 1.1",substring=true).assertExists()
        compose.onNodeWithText("Voltar").performClick()
        compose.onNodeWithText("Formulário de acessibilidade").performScrollTo().performClick()
        compose.onNodeWithText("Nome (opcional)").assertExists()
        compose.onNodeWithContentDescription("Fechar").performClick()
        compose.onNodeWithText("Consultar CDI e Selic online").performScrollTo()
        compose.onNode(isToggleable()).performClick()
        compose.runOnIdle {assertEquals(false,vm.onlineRates.value);vm.refreshRates(true);assertEquals(false,vm.ratesBusy.value)}
        compose.onNodeWithText("Excluir todos os registros locais").performScrollTo().performClick()
        compose.onNodeWithText("Apagar registros").assertIsNotEnabled()
        compose.onNodeWithText("Digite APAGAR para confirmar").performScrollTo().performTextInput("APAGAR")
        compose.onNodeWithText("Apagar registros").assertIsEnabled().performClick()
        compose.waitUntil(20000) {compose.onAllNodes(isRoot()).fetchSemanticsNodes();vm.state.value.accounts.isEmpty() && vm.state.value.series.isEmpty()}
        compose.waitUntil(10000) {compose.onAllNodesWithText("Excluir seus registros?").fetchSemanticsNodes().isEmpty()}
        compose.onNodeWithText("Concluir").performClick()
        compose.waitUntil(10000) {compose.onAllNodesWithText("Privacidade e acessibilidade").fetchSemanticsNodes().isEmpty()}
        compose.onNode(hasText("Contas") and hasClickAction()).performClick()
        compose.onNodeWithText("Onde está seu dinheiro?").assertExists()
        compose.onNode(hasText("Relatórios") and hasClickAction()).performClick()
        compose.onNodeWithText("Sem movimentos").assertExists()
    }
}
