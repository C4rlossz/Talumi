package com.talumi.financas

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

class LocalRequestTest {
    @Test fun rightsRequestNeedsNoIdentityDocumentOrJustification() {
        val result=LocalRequest.text(RequestKind.PRIVACY,"Exclusão","","","Excluir dados","1.3.0",LocalDate.of(2026,10,22))
        assertTrue(result.contains("Excluir dados"));assertTrue(result.contains("não foi enviado"))
        assertFalse(result.contains("CPF:"));assertFalse(result.contains("account_id"))
    }
    @Test fun accessibilityReportOnlyContainsSuppliedInformation() {
        val result=LocalRequest.text(RequestKind.ACCESSIBILITY,"Contraste","Teste","teste@example.com","Não consigo ler o texto do saldo.","1.3.0")
        assertTrue(result.contains("teste@example.com"));assertTrue(result.contains("Não consigo ler"));assertFalse(result.contains("INVESTMENT"))
    }
    @Test fun invalidOrExcessiveFieldsAreRejected() {
        assertThrows(Exception::class.java) {LocalRequest.text(RequestKind.PRIVACY,"Outro","",""," ","1.3.0")}
        assertThrows(Exception::class.java) {LocalRequest.text(RequestKind.PRIVACY,"Outro","","inválido","Excluir","1.3.0")}
        assertThrows(Exception::class.java) {LocalRequest.text(RequestKind.PRIVACY,"Outro","","","x".repeat(2001),"1.3.0")}
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=android.app.Application::class)
class PrivacyDbTest {
    @Test fun eraseRemovesAllFinancialTablesAndDoesNotResurrectSalary() {
        val context=ApplicationProvider.getApplicationContext<Context>();context.deleteDatabase("privacy-test.db")
        FinanceDb(context,"privacy-test.db").use {db->
            val today=LocalDate.now();val account=db.saveAccount(Account(name="Conta",opening=100000))
            db.addSeries(Series(title="Salário",amount=1000,kind=EntryKind.INCOME,accountId=account,start=today,category="Salário",autoPost=true))
            db.saveDebt(Debt(person="Pessoa fictícia",title="Teste",amount=500,kind=DebtKind.PAYABLE,due=today))
            db.saveInvestment(Account(name="CDB",institution="Banco fictício",kind=AccountKind.INVESTMENT,opening=1000),Investment(0,valuationDate=today))
            db.saveBenchmark(Benchmark("CDI","13.65",today,java.time.Instant.now().toString()))
            assertTrue(db.snapshot().entries.isNotEmpty())
            db.eraseLocalRecords();db.materialize();val state=db.snapshot()
            assertTrue(state.accounts.isEmpty() && state.entries.isEmpty() && state.debts.isEmpty() && state.series.isEmpty() && state.investments.isEmpty() && state.benchmarks.isEmpty())
            db.readableDatabase.rawQuery("PRAGMA secure_delete",null).use {it.moveToFirst();assertEquals(1,it.getInt(0))}
        }
        FinanceDb(context,"privacy-test.db").use {db->assertTrue(db.snapshot().accounts.isEmpty());assertTrue(db.snapshot().entries.isEmpty())}
        context.deleteDatabase("privacy-test.db")
    }
    @Test fun documentsContactAndLicensesAreAvailableOffline() {
        val context=ApplicationProvider.getApplicationContext<Context>()
        for(file in listOf("PRIVACIDADE.md","TERMOS.md","COOKIES.md","ACESSIBILIDADE.md","AUTORIA.md","THIRD_PARTY_NOTICES.md","LICENSE")) {
            val text=context.assets.open("legal/$file").bufferedReader().use {it.readText()};assertTrue(file,text.length>500)
        }
        assertEquals("Carlosz.eduardo1402@gmail.com",context.getString(R.string.author_contact_email))
    }
}
