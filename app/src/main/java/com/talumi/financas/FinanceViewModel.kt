package com.talumi.financas

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class FinanceViewModel(app: Application) : AndroidViewModel(app) {
    private val db=(app as? TalumiApplication)?.financeDb ?: FinanceDb(app)
    private val prefs=app.getSharedPreferences("talumi_preferences",0)
    private val lock=Mutex()
    private val _state=MutableStateFlow(FinanceState())
    val state=_state.asStateFlow()
    private val _busy=MutableStateFlow(false)
    val busy=_busy.asStateFlow()
    private val _theme=MutableStateFlow(prefs.getString("theme","Sistema") ?: "Sistema")
    val theme=_theme.asStateFlow()
    private val ratesGeneration=java.util.concurrent.atomic.AtomicLong()
    private val _onlineRates=MutableStateFlow(prefs.getBoolean("online_rates",true))
    val onlineRates=_onlineRates.asStateFlow()
    fun onlineRates(enabled:Boolean) {if(!enabled) ratesGeneration.incrementAndGet();_onlineRates.value=enabled;prefs.edit {putBoolean("online_rates",enabled)}}
    fun eraseLocalRecords(done:()->Unit) {ratesGeneration.incrementAndGet();_ratesMessage.value="";action("Registros locais excluídos",done) {db.eraseLocalRecords()}}
    fun saveRequest(uri:Uri,text:String,done:()->Unit={})=action("Solicitação salva no arquivo escolhido",done) {
        require(text.length<=10000)
        getApplication<Application>().contentResolver.openOutputStream(uri,"wt")?.bufferedWriter(Charsets.UTF_8)?.use {it.write(text)} ?: error("Arquivo indisponível")
    }
    private val _ratesBusy=MutableStateFlow(false)
    val ratesBusy=_ratesBusy.asStateFlow()
    private val _ratesMessage=MutableStateFlow("")
    val ratesMessage=_ratesMessage.asStateFlow()
    private val messages=Channel<String>(Channel.BUFFERED)
    val events=messages.receiveAsFlow()
    init { action { db.materialize() } }
    fun theme(value: String) { _theme.value=value; prefs.edit { putString("theme",value) } }
    fun refresh() { if(_state.value.loaded) action(showProgress=false) { db.materialize() } }
    private fun action(success: String?=null, done: () -> Unit={}, showProgress: Boolean=true, block: () -> Unit) {
        viewModelScope.launch {
            lock.withLock {
                if(showProgress) _busy.value=true
                try {
                    val snapshot=withContext(Dispatchers.IO) { block(); db.snapshot() }
                    _state.value=snapshot; done()
                    success?.let { messages.send(it) }
                } catch(e: Exception) {
                    val message=when(e) { is IllegalArgumentException -> e.message ?: "Confira os dados informados."; else -> "Não foi possível concluir. Seus dados anteriores foram preservados. Confira o arquivo e tente novamente." }
                    messages.send(message)
                } finally { if(showProgress) _busy.value=false }
            }
        }
    }
    fun account(value: Account, done: () -> Unit) = action("Conta salva",done) { db.saveAccount(value) }
    fun investment(account:Account,value:Investment,done:()->Unit)=action("Investimento salvo",done) {db.saveInvestment(account,value)}
    fun investmentMovement(id:Long,type:InvestmentAction,amount:Long,cash:Long?,date:java.time.LocalDate,note:String,done:()->Unit)=action("Movimento da carteira registrado",done) {db.investmentMovement(id,type,amount,cash,date,note)}
    fun refreshRates(force:Boolean=false) {
        if(!_onlineRates.value) {_ratesMessage.value="Consultas online desativadas nas configurações. Usando apenas as taxas salvas.";return}
        if(_ratesBusy.value) return
        val today=java.time.LocalDate.now()
        if(!force && _state.value.benchmarks.size==2 && _state.value.benchmarks.all {runCatching {java.time.Instant.parse(it.fetchedAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate()==today}.getOrDefault(false)}) return
        val generation=ratesGeneration.get()
        _ratesBusy.value=true
        viewModelScope.launch {
            try {
                val results=withContext(Dispatchers.IO) {MarketRates.series.keys.map {code->runCatching {MarketRates.fetch(code)}}}
                lock.withLock {
                    if(generation!=ratesGeneration.get()) return@withLock
                    _state.value=withContext(Dispatchers.IO) {results.forEach {it.getOrNull()?.let(db::saveBenchmark)};db.snapshot()}
                }
                _ratesMessage.value=if(generation!=ratesGeneration.get()) "Consulta descartada após alteração dos dados ou da preferência de rede." else if(results.all {it.isSuccess}) "Taxas consultadas no Banco Central." else "Consulta indisponível. As últimas taxas salvas foram mantidas; tente atualizar depois."
            } catch(e:Exception) {_ratesMessage.value="Não foi possível atualizar. As taxas salvas foram mantidas."} finally {_ratesBusy.value=false}
        }
    }
    fun entry(value: Entry, recurring: Boolean, done: () -> Unit) = action("Lançamento salvo",done) {
        if(recurring && value.id==0L) db.addSeries(Series(title=value.title,amount=value.amount,kind=value.kind,accountId=value.accountId,start=value.date,category=value.category)) else db.saveEntry(value)
    }
    fun debt(value: Debt, done: () -> Unit) = action("Compromisso salvo",done) { db.saveDebt(value) }
    fun salary(value: Series, done: () -> Unit) = action("Salário recorrente cadastrado",done) { require(value.autoPost && value.kind==EntryKind.INCOME); db.addSeries(value) }
    fun enableAutomaticIncome(id: Long) = action("Recebimento automático ativado a partir de hoje") { db.enableAutomaticIncome(id) }
    fun settle(debt: Debt, account: Long, amount: Long, date: java.time.LocalDate, done: () -> Unit) = action("Acerto registrado",done) { db.settle(debt.id,account,amount,date) }
    fun confirm(entry: Entry) = action("Confirmado no saldo da conta") { db.confirm(entry.id) }
    fun deleteEntry(id: Long, done: () -> Unit) = action("Lançamento excluído",done) { db.deleteEntry(id) }
    fun deleteAccount(id: Long, done: () -> Unit) = action("Conta excluída",done) { db.deleteAccount(id) }
    fun deleteDebt(id: Long, done: () -> Unit) = action("Compromisso excluído",done) { db.deleteDebt(id) }
    fun stopSeries(id: Long, done: () -> Unit) = action("Recorrência encerrada",done) { db.stopSeries(id) }
    fun export(uri: Uri) = action("Backup salvo. Guarde o arquivo em um local seguro.") {
        val text=db.exportJson()
        getApplication<Application>().contentResolver.openOutputStream(uri,"wt")?.bufferedWriter(Charsets.UTF_8)?.use { it.write(text) } ?: error("Arquivo indisponível")
    }
    fun restore(uri: Uri, done: () -> Unit) = action("Backup restaurado",done) {
        val text=getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
            val output=java.io.ByteArrayOutputStream()
            val buffer=ByteArray(8192)
            while(true) {
                val count=input.read(buffer)
                if(count<0) break
                require(output.size()+count<=10_000_000) { "Backup maior que 10 MB." }
                output.write(buffer,0,count)
            }
            output.toString("UTF-8")
        } ?: error("Arquivo indisponível")
        db.importJson(text)
    }
    override fun onCleared() { super.onCleared(); if(getApplication<Application>() !is TalumiApplication) db.close() }
}
