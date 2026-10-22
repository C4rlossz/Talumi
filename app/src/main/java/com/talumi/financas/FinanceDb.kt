package com.talumi.financas

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth

class FinanceDb(context: Context, name: String = "talumi.db", private val todayProvider: () -> LocalDate = { LocalDate.now() }) : SQLiteOpenHelper(context, name, null, 3), java.io.Closeable {
    override fun onConfigure(db: SQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true); db.rawQuery("PRAGMA secure_delete=ON",null).use {it.moveToFirst()} }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE accounts (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, institution TEXT NOT NULL, kind TEXT NOT NULL, opening INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE series (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, amount INTEGER NOT NULL CHECK(amount>0), kind TEXT NOT NULL, account_id INTEGER NOT NULL REFERENCES accounts(id), start TEXT NOT NULL, category TEXT NOT NULL, active INTEGER NOT NULL, generated_until TEXT NOT NULL)")
        db.execSQL("CREATE TABLE debts (id INTEGER PRIMARY KEY AUTOINCREMENT, person TEXT NOT NULL, title TEXT NOT NULL, amount INTEGER NOT NULL CHECK(amount>0), kind TEXT NOT NULL, due TEXT NOT NULL, note TEXT NOT NULL)")
        db.execSQL("CREATE TABLE entries (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, amount INTEGER NOT NULL CHECK(amount>0), kind TEXT NOT NULL, account_id INTEGER NOT NULL REFERENCES accounts(id), to_account_id INTEGER REFERENCES accounts(id), date TEXT NOT NULL, posted INTEGER NOT NULL, category TEXT NOT NULL, note TEXT NOT NULL, series_id INTEGER REFERENCES series(id), debt_id INTEGER REFERENCES debts(id))")
        db.execSQL("CREATE INDEX entries_date ON entries(date)")
        db.execSQL("CREATE INDEX entries_account ON entries(account_id)")
        db.execSQL("CREATE INDEX entries_debt ON entries(debt_id)")
        addSalaryColumns(db)
        addInvestmentTables(db)
    }
    private fun addSalaryColumns(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE series ADD COLUMN pay_rule TEXT NOT NULL DEFAULT 'FIXED_DAY'")
        db.execSQL("ALTER TABLE series ADD COLUMN pay_day INTEGER NOT NULL DEFAULT 1")
        db.execSQL("UPDATE series SET pay_day=CAST(substr(start,9,2) AS INTEGER)")
        db.execSQL("ALTER TABLE series ADD COLUMN auto_post INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE series ADD COLUMN include_saturday INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE series ADD COLUMN holidays TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE entries ADD COLUMN auto_post INTEGER NOT NULL DEFAULT 0")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if(oldVersion<2) addSalaryColumns(db)
        if(oldVersion<3) addInvestmentTables(db)
    }
    private fun addInvestmentTables(db:SQLiteDatabase) {
        db.execSQL("CREATE TABLE investments (id INTEGER PRIMARY KEY REFERENCES accounts(id) ON DELETE CASCADE,product TEXT NOT NULL,mode TEXT NOT NULL,rate TEXT NOT NULL,maturity TEXT,liquidity TEXT NOT NULL,valuation_date TEXT NOT NULL,holidays TEXT NOT NULL)")
        db.execSQL("CREATE TABLE benchmarks (id TEXT PRIMARY KEY,annual TEXT NOT NULL,observed TEXT NOT NULL,fetched_at TEXT NOT NULL)")
        db.execSQL("INSERT INTO investments SELECT id,'OTHER','MONTHLY','0',NULL,'DAILY',?,'' FROM accounts WHERE kind='INVESTMENT'",arrayOf(todayProvider().toString()))
    }
    private fun <T> transaction(block: (SQLiteDatabase) -> T): T {
        val db = writableDatabase
        db.beginTransaction()
        try { val result = block(db); db.setTransactionSuccessful(); return result } finally { db.endTransaction() }
    }
    private fun values(vararg pairs: Pair<String, Any?>) = ContentValues().apply { pairs.forEach { (key, value) -> when(value) { null -> putNull(key); is Long -> put(key, value); is Int -> put(key, value); is Boolean -> put(key, if(value) 1 else 0); else -> put(key, value.toString()) } } }
    private fun save(table: String, id: Long, values: ContentValues): Long = if(id == 0L) writableDatabase.insertOrThrow(table, null, values) else {
        require(writableDatabase.update(table, values, "id=?", arrayOf(id.toString())) == 1) { "Registro não encontrado." }; id
    }
    private fun <T> rows(table: String, map: (android.database.Cursor) -> T): List<T> = readableDatabase.rawQuery("SELECT * FROM $table ORDER BY id", null).use { c -> buildList { while(c.moveToNext()) add(map(c)) } }
    private fun android.database.Cursor.s(key: String) = getString(getColumnIndexOrThrow(key))
    private fun android.database.Cursor.l(key: String) = getLong(getColumnIndexOrThrow(key))
    private fun android.database.Cursor.optional(key: String): Long? = if(isNull(getColumnIndexOrThrow(key))) null else l(key)
    fun snapshot(): FinanceState = FinanceState(
        accounts = rows("accounts") { Account(it.l("id"),it.s("name"),it.s("institution"),AccountKind.valueOf(it.s("kind")),it.l("opening")) },
        entries = rows("entries") { Entry(it.l("id"),it.s("title"),it.l("amount"),EntryKind.valueOf(it.s("kind")),it.l("account_id"),it.optional("to_account_id"),LocalDate.parse(it.s("date")),it.l("posted")==1L,it.s("category"),it.s("note"),it.optional("series_id"),it.optional("debt_id"),it.l("auto_post")==1L) },
        debts = rows("debts") { Debt(it.l("id"),it.s("person"),it.s("title"),it.l("amount"),DebtKind.valueOf(it.s("kind")),LocalDate.parse(it.s("due")),it.s("note")) },
        series = rows("series") { Series(it.l("id"),it.s("title"),it.l("amount"),EntryKind.valueOf(it.s("kind")),it.l("account_id"),LocalDate.parse(it.s("start")),it.s("category"),it.l("active")==1L,PayRule.valueOf(it.s("pay_rule")),it.l("pay_day").toInt(),it.l("auto_post")==1L,it.l("include_saturday")==1L,it.s("holidays").lineSequence().filter {d->d.isNotBlank()}.map(LocalDate::parse).toSet()) }, loaded = true,
        investments=rows("investments") {Investment(it.l("id"),InvestmentProduct.valueOf(it.s("product")),YieldMode.valueOf(it.s("mode")),it.s("rate"),if(it.isNull(it.getColumnIndexOrThrow("maturity"))) null else LocalDate.parse(it.s("maturity")),Liquidity.valueOf(it.s("liquidity")),LocalDate.parse(it.s("valuation_date")),it.s("holidays").lineSequence().filter {d->d.isNotBlank()}.map(LocalDate::parse).toSet())},
        benchmarks=rows("benchmarks") {Benchmark(it.s("id"),it.s("annual"),LocalDate.parse(it.s("observed")),it.s("fetched_at"))}
    )
    private fun validText(value: String) { require(value.isNotBlank() && value.length <= 120) { "Preencha o nome com até 120 caracteres." } }
    private fun validDate(date: LocalDate) { require(date.year in 2000..2100) { "A data deve estar entre 2000 e 2100." } }
    private fun validAmount(amount: Long) { require(amount in 1..Money.MAX_CENTS) { "Valor inválido." } }
    fun saveAccount(account: Account): Long {
        validText(account.name); require(account.institution.length<=120); require(account.opening in -Money.MAX_CENTS..Money.MAX_CENTS)
        if(account.id>0 && account.kind!=AccountKind.INVESTMENT) require(snapshot().accounts.none {it.id==account.id && it.kind==AccountKind.INVESTMENT}) {"Use o resgate para movimentar um investimento para Finanças."}
        val id=save("accounts",account.id,values("name" to account.name.trim(),"institution" to account.institution.trim(),"kind" to account.kind.name,"opening" to account.opening))
        if(account.kind==AccountKind.INVESTMENT) writableDatabase.execSQL("INSERT OR IGNORE INTO investments VALUES (?,'OTHER','MONTHLY','0',NULL,'DAILY',?,'')",arrayOf<Any>(id,todayProvider().toString()))
        return id
    }
    fun deleteAccount(id: Long) {
        val state = snapshot()
        require(state.entries.none { it.accountId==id || it.toAccountId==id } && state.series.none { it.accountId==id }) { "Esta conta tem lançamentos ou recorrências. Mantenha-a para preservar o histórico." }
        writableDatabase.delete("accounts","id=?",arrayOf(id.toString()))
    }
    fun saveEntry(entry: Entry,investmentOperation:Boolean=false,revalue:Boolean=true): Long = transaction {
        validText(entry.title); validAmount(entry.amount); validDate(entry.date); require(entry.note.length<=1000); require(entry.category.length in 1..120)
        val state = snapshot()
        val previous=state.entries.firstOrNull {it.id==entry.id}
        require(state.accounts.any { it.id==entry.accountId }) { "Escolha uma conta." }
        if(entry.id==0L && state.accounts.any {it.id==entry.accountId && it.kind==AccountKind.INVESTMENT} && entry.kind!=EntryKind.TRANSFER) require(investmentOperation || entry.seriesId!=null) {"Registre rendimentos e perdas na carteira de investimentos."}
        require(!entry.posted || entry.date<=todayProvider()) { "Uma data futura deve ficar como agendamento." }
        if(entry.kind==EntryKind.TRANSFER) require(entry.toAccountId!=entry.accountId && state.accounts.any { it.id==entry.toAccountId }) { "Escolha uma conta de destino diferente da origem." }
        else require(entry.toAccountId==null) { "Destino permitido somente em transferências." }
        if(revalue && entry.kind==EntryKind.TRANSFER && state.accounts.any {it.id==entry.accountId && it.kind==AccountKind.INVESTMENT} && (previous==null || previous.amount!=entry.amount || previous.accountId!=entry.accountId || previous.toAccountId!=entry.toAccountId || previous.date!=entry.date || previous.posted!=entry.posted)) {
            val investment=state.investments.first {it.accountId==entry.accountId}
            require(investment.liquidity!=Liquidity.MATURITY || (investment.maturity!=null && entry.date>=investment.maturity)) {"O investimento está bloqueado até o vencimento."}
            require(Ledger.balance(state.accounts.first {it.id==entry.accountId},state.entries.filter {it.id!=entry.id})>=entry.amount) {"O resgate supera o saldo investido."}
        }
        entry.seriesId?.let { id -> require(state.series.any { it.id==id }) }
        if(entry.autoPost) require(entry.kind==EntryKind.INCOME && entry.seriesId!=null && state.series.any {it.id==entry.seriesId && it.autoPost}) { "Recebimento automático sem salário vinculado." }
        entry.debtId?.let { id ->
            val debt=state.debts.firstOrNull { it.id==id } ?: error("Dívida não encontrada.")
            require(entry.posted && entry.seriesId==null && entry.kind==if(debt.kind==DebtKind.RECEIVABLE) EntryKind.INCOME else EntryKind.EXPENSE)
            require(entry.amount<=Ledger.outstanding(debt,state.entries.filter { it.id!=entry.id })) { "O acerto supera o valor restante." }
        }
        val savedId=save("entries",entry.id,values("title" to entry.title.trim(),"amount" to entry.amount,"kind" to entry.kind.name,"account_id" to entry.accountId,"to_account_id" to entry.toAccountId,"date" to entry.date,"posted" to entry.posted,"category" to entry.category,"note" to entry.note,"series_id" to entry.seriesId,"debt_id" to entry.debtId,"auto_post" to entry.autoPost))
        val affected=listOfNotNull(entry.accountId,entry.toAccountId,previous?.accountId,previous?.toAccountId)
        if(revalue && (entry.posted || previous?.posted==true)) affected.distinct().forEach {id->writableDatabase.update("investments",values("valuation_date" to todayProvider()),"id=?",arrayOf(id.toString()))}
        savedId
    }
    fun deleteEntry(id: Long) = transaction {
        val previous=snapshot().entries.firstOrNull {it.id==id}
        writableDatabase.delete("entries","id=?",arrayOf(id.toString()))
        if(previous?.posted==true) listOfNotNull(previous.accountId,previous.toAccountId).forEach {account->writableDatabase.update("investments",values("valuation_date" to todayProvider()),"id=?",arrayOf(account.toString()))}
    }
    fun confirm(id: Long) { val entry=snapshot().entries.first { it.id==id }; saveEntry(entry.copy(posted=true)) }
    fun saveDebt(debt: Debt): Long {
        validText(debt.person); validText(debt.title); validAmount(debt.amount); validDate(debt.due); require(debt.note.length<=1000)
        val state=snapshot()
        val paid=state.entries.filter { it.debtId==debt.id }.sumOf { it.amount }
        require(debt.amount>=paid) { "O total não pode ser menor que o valor já quitado." }
        if(paid>0) require(state.debts.first { it.id==debt.id }.kind==debt.kind) { "Não é possível trocar o tipo após um acerto." }
        return save("debts",debt.id,values("person" to debt.person.trim(),"title" to debt.title.trim(),"amount" to debt.amount,"kind" to debt.kind.name,"due" to debt.due,"note" to debt.note))
    }
    fun deleteDebt(id: Long) {
        require(snapshot().entries.none { it.debtId==id }) { "Exclua os acertos vinculados em Movimentos antes de excluir esta dívida." }
        writableDatabase.delete("debts","id=?",arrayOf(id.toString()))
    }
    fun settle(debtId: Long, accountId: Long, amount: Long, date: LocalDate) = transaction {
        require(snapshot().financeAccounts.any {it.id==accountId}) {"Escolha uma conta de Finanças. Resgate o investimento antes de usá-lo."}
        val debt=snapshot().debts.first { it.id==debtId }
        saveEntry(Entry(title="Acerto · ${debt.person}",amount=amount,kind=if(debt.kind==DebtKind.RECEIVABLE) EntryKind.INCOME else EntryKind.EXPENSE,accountId=accountId,date=date,posted=true,category="Acerto de dívida",debtId=debtId))
    }
    private fun validSeries(series: Series) {
        validText(series.title); validAmount(series.amount); validDate(series.start)
        require(series.kind!=EntryKind.TRANSFER && snapshot().accounts.any { it.id==series.accountId })
        require(series.day in 1..31 && series.category.length in 1..120)
        require(!series.autoPost || series.kind==EntryKind.INCOME) { "Somente entradas podem ser confirmadas automaticamente." }
        require(series.holidays.size<=366) { "Informe até 366 datas de exceção." }
        series.holidays.forEach(::validDate)
    }
    fun addSeries(series: Series) = transaction {
        validSeries(series)
        require(snapshot().financeAccounts.any {it.id==series.accountId}) {"Escolha uma conta de Finanças para esta recorrência."}
        save("series",0,values("title" to series.title.trim(),"amount" to series.amount,"kind" to series.kind.name,"account_id" to series.accountId,"start" to series.start,"category" to series.category,"active" to true,"generated_until" to YearMonth.from(series.start).minusMonths(1),"pay_rule" to series.rule.name,"pay_day" to series.day,"auto_post" to series.autoPost,"include_saturday" to series.includeSaturday,"holidays" to series.holidays.sorted().joinToString("\n")))
        materialize()
    }
    fun enableAutomaticIncome(id: Long) = transaction { db ->
        val series=snapshot().series.firstOrNull {it.id==id} ?: throw IllegalArgumentException("Recorrência não encontrada.")
        require(series.kind==EntryKind.INCOME && series.active) { "Escolha uma entrada mensal ativa." }
        db.update("series",values("auto_post" to true),"id=?",arrayOf(id.toString()))
        db.update("entries",values("auto_post" to true),"series_id=? AND posted=0 AND date>=?",arrayOf(id.toString(),todayProvider().toString()))
        materialize()
    }
    fun stopSeries(id: Long) = transaction { db ->
        db.update("series",values("active" to false),"id=?",arrayOf(id.toString()))
        db.delete("entries","series_id=? AND posted=0 AND date>?",arrayOf(id.toString(),todayProvider().toString()))
    }
    fun materialize(today: LocalDate = todayProvider()) = transaction { db ->
        val untils=rows("series") { it.l("id") to YearMonth.parse(it.s("generated_until")) }.toMap()
        for(s in snapshot().series.filter { it.active }) {
            var month=untils.getValue(s.id).plusMonths(1)
            val end=maxOf(YearMonth.from(today),YearMonth.from(s.start)).plusMonths(12).coerceAtMost(YearMonth.of(2100,12))
            while(month<=end) {
                val date=Ledger.occurrence(s,month)
                if(date>=s.start) saveEntry(Entry(title=s.title,amount=s.amount,kind=s.kind,accountId=s.accountId,date=date,category=s.category,seriesId=s.id,autoPost=s.autoPost))
                month=month.plusMonths(1)
            }
            if(untils.getValue(s.id)<end) db.update("series",values("generated_until" to end),"id=?",arrayOf(s.id.toString()))
        }
        db.execSQL("UPDATE entries SET posted=1 WHERE auto_post=1 AND posted=0 AND kind='INCOME' AND date<=? AND series_id IN (SELECT id FROM series WHERE active=1 AND auto_post=1)",arrayOf(today.toString()))
    }
    fun exportJson(): String = transaction {
        val root=JSONObject().put("app","Talumi").put("version",3).put("exportedAt",java.time.Instant.now().toString())
        for(table in listOf("accounts","debts","series","entries","investments","benchmarks")) {
            val array=JSONArray()
            readableDatabase.rawQuery("SELECT * FROM $table ORDER BY id",null).use { c -> while(c.moveToNext()) {
                val row=JSONObject()
                for(i in 0 until c.columnCount) row.put(c.getColumnName(i),when(c.getType(i)) { android.database.Cursor.FIELD_TYPE_NULL -> JSONObject.NULL; android.database.Cursor.FIELD_TYPE_INTEGER -> c.getLong(i); else -> c.getString(i) })
                array.put(row)
            } }
            root.put(table,array)
        }
        root.toString(2)
    }
    fun importJson(text: String) = transaction { db ->
        require(text.length<=10_000_000) { "Backup maior que o limite de 10 MB." }
        val root=JSONObject(text)
        require(root.getString("app")=="Talumi" && root.getInt("version") in 1..3) { "Este arquivo não é um backup Talumi compatível." }
        if(root.getInt("version")==1) {
            val oldSeries=root.getJSONArray("series")
            for(i in 0 until oldSeries.length()) oldSeries.getJSONObject(i).apply { put("pay_rule","FIXED_DAY");put("pay_day",LocalDate.parse(getString("start")).dayOfMonth);put("auto_post",0);put("include_saturday",0);put("holidays","") }
            val oldEntries=root.getJSONArray("entries")
            for(i in 0 until oldEntries.length()) oldEntries.getJSONObject(i).put("auto_post",0)
        }
        if(root.getInt("version")<3) {
            val investments=JSONArray();val accounts=root.getJSONArray("accounts")
            for(i in 0 until accounts.length()) {val a=accounts.getJSONObject(i);if(a.getString("kind")=="INVESTMENT") investments.put(JSONObject().put("id",a.getLong("id")).put("product","OTHER").put("mode","MONTHLY").put("rate","0").put("maturity",JSONObject.NULL).put("liquidity","DAILY").put("valuation_date",todayProvider().toString()).put("holidays",""))}
            root.put("investments",investments).put("benchmarks",JSONArray())
        }
        val columns=mapOf("accounts" to setOf("id","name","institution","kind","opening"),"debts" to setOf("id","person","title","amount","kind","due","note"),"series" to setOf("id","title","amount","kind","account_id","start","category","active","generated_until","pay_rule","pay_day","auto_post","include_saturday","holidays"),"entries" to setOf("id","title","amount","kind","account_id","to_account_id","date","posted","category","note","series_id","debt_id","auto_post"),"investments" to setOf("id","product","mode","rate","maturity","liquidity","valuation_date","holidays"),"benchmarks" to setOf("id","annual","observed","fetched_at"))
        for(table in listOf("entries","series","debts","investments","benchmarks","accounts")) db.delete(table,null,null)
        for(table in listOf("accounts","debts","series","entries","investments","benchmarks")) {
            val array=root.getJSONArray(table); require(array.length()<=50000)
            for(i in 0 until array.length()) {
                val row=array.getJSONObject(i)
                require(row.keys().asSequence().toSet()==columns.getValue(table)) { "Estrutura de backup inválida." }
                val cv=ContentValues()
                row.keys().forEach { key -> val v=row.get(key); when { v==JSONObject.NULL -> cv.putNull(key); v is Number -> cv.put(key,java.math.BigDecimal(v.toString()).longValueExact()); v is String -> cv.put(key,v); else -> error("Valor inválido no backup.") } }
                if(table!="benchmarks") require(cv.getAsLong("id")>0) else require(cv.getAsString("id") in listOf("CDI","SELIC"))
                db.insertOrThrow(table,null,cv)
            }
        }
        val s=snapshot()
        s.accounts.forEach { saveAccount(it) }
        s.debts.forEach { saveDebt(it) }
        s.entries.forEach { saveEntry(it,revalue=false) }
        s.series.forEach(::validSeries)
        require(s.accounts.filter {it.kind==AccountKind.INVESTMENT}.map {it.id}.toSet()==s.investments.map {it.accountId}.toSet()) {"Carteira incompleta no backup."}
        s.investments.forEach {validateInvestment(it)}
        s.benchmarks.forEach {require(it.code in MarketRates.series && it.annual.toBigDecimal() in java.math.BigDecimal.ZERO..java.math.BigDecimal("100"));validDate(it.observed);java.time.Instant.parse(it.fetchedAt)}
        rows("series") { YearMonth.parse(it.s("generated_until")) to YearMonth.from(LocalDate.parse(it.s("start"))) }.forEach { (until,start) -> require(until.year in 1999..2100 && until>=start.minusMonths(1)) }
        db.rawQuery("PRAGMA foreign_key_check",null).use { require(!it.moveToFirst()) }
        materialize()
    }
    private fun validateInvestment(value:Investment) {
        require(snapshot().accounts.any {it.id==value.accountId && it.kind==AccountKind.INVESTMENT})
        require(InvestmentMath.parseRate(value.rate)==value.rate) {"Taxa inválida no investimento."}
        validDate(value.valuationDate);require(value.valuationDate<=todayProvider()) {"A data do saldo não pode estar no futuro."}
        value.maturity?.let(::validDate);require(value.liquidity!=Liquidity.MATURITY || value.maturity!=null) {"Informe o vencimento para um investimento bloqueado."}
        require(value.holidays.size<=366);value.holidays.forEach(::validDate)
    }
    fun saveInvestment(account:Account,value:Investment):Long = transaction {db->
        require(account.kind==AccountKind.INVESTMENT)
        if(account.id==0L) require(account.opening>=0) {"O valor investido não pode ser negativo."}
        val id=saveAccount(account)
        val investment=value.copy(accountId=id,rate=InvestmentMath.parseRate(value.rate))
        validateInvestment(investment)
        require(snapshot().entries.none {it.posted && (it.accountId==id || it.toAccountId==id) && it.date>investment.valuationDate}) {"A data do saldo precisa incluir os movimentos já registrados."}
        db.update("investments",values("product" to investment.product.name,"mode" to investment.mode.name,"rate" to investment.rate,"maturity" to investment.maturity,"liquidity" to investment.liquidity.name,"valuation_date" to investment.valuationDate,"holidays" to investment.holidays.sorted().joinToString("\n")),"id=?",arrayOf(id.toString()))
        id
    }
    fun investmentMovement(id:Long,action:InvestmentAction,amount:Long,cashAccount:Long?,date:LocalDate,note:String) = transaction {db->
        val state=snapshot();val account=state.accounts.firstOrNull {it.id==id && it.kind==AccountKind.INVESTMENT} ?: throw IllegalArgumentException("Investimento não encontrado.")
        validAmount(amount);require(date<=todayProvider()) {"Registre apenas movimentos já realizados."}
        if(action==InvestmentAction.DEPOSIT || action==InvestmentAction.WITHDRAW) require(state.financeAccounts.any {it.id==cashAccount}) {"Escolha uma conta de Finanças."}
        if(action==InvestmentAction.DEPOSIT) require(Ledger.balance(state.financeAccounts.first {it.id==cashAccount},state.entries)>=amount) {"Saldo insuficiente na conta de origem."}
        if(action==InvestmentAction.LOSS) require(Ledger.balance(account,state.entries)>=amount) {"A perda supera o saldo registrado."}
        val entry=when(action) {
            InvestmentAction.DEPOSIT -> Entry(title="Aporte · ${account.name}".take(120),amount=amount,kind=EntryKind.TRANSFER,accountId=cashAccount!!,toAccountId=id,date=date,posted=true,category="Aporte",note=note)
            InvestmentAction.WITHDRAW -> Entry(title="Resgate · ${account.name}".take(120),amount=amount,kind=EntryKind.TRANSFER,accountId=id,toAccountId=cashAccount,date=date,posted=true,category="Resgate",note=note)
            InvestmentAction.GAIN -> Entry(title="Rendimento · ${account.name}".take(120),amount=amount,kind=EntryKind.INCOME,accountId=id,date=date,posted=true,category="Rendimento de investimento",note=note)
            InvestmentAction.LOSS -> Entry(title="Perda / custo · ${account.name}".take(120),amount=amount,kind=EntryKind.EXPENSE,accountId=id,date=date,posted=true,category="Perda de investimento",note=note)
        }
        saveEntry(entry,investmentOperation=true)
        db.update("investments",values("valuation_date" to todayProvider()),"id=?",arrayOf(id.toString()))
    }
    fun eraseLocalRecords() = transaction {db->
        for(table in listOf("entries","series","debts","investments","benchmarks","accounts")) db.delete(table,null,null)
    }
    fun saveBenchmark(value:Benchmark) {
        require(value.code in MarketRates.series);require(value.annual.toBigDecimal() in java.math.BigDecimal.ZERO..java.math.BigDecimal("100"));validDate(value.observed);java.time.Instant.parse(value.fetchedAt)
        writableDatabase.insertWithOnConflict("benchmarks",null,values("id" to value.code,"annual" to value.annual,"observed" to value.observed,"fetched_at" to value.fetchedAt),SQLiteDatabase.CONFLICT_REPLACE)
    }
}
