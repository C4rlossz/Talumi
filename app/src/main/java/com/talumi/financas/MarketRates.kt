package com.talumi.financas

import org.json.JSONArray
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection

object MarketRates {
    val series=linkedMapOf("CDI" to 4389,"SELIC" to 1178)
    fun parse(code:String,body:String,now:LocalDate=LocalDate.now()):Benchmark {
        require(code in series)
        val array=JSONArray(body)
        require(array.length() in 1..10) {"Resposta de taxas inválida."}
        val row=(0 until array.length()).map {array.getJSONObject(it)}.maxBy {LocalDate.parse(it.getString("data"),DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT))}
        val annual=InvestmentMath.parseRate(row.getString("valor"))
        require(annual.toBigDecimal()<=100.toBigDecimal()) {"Taxa de referência fora do intervalo esperado."}
        val date=LocalDate.parse(row.getString("data"),DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT))
        require(date<=now && date>=now.minusDays(90)) {"A fonte não retornou uma taxa recente."}
        return Benchmark(code,annual,date,Instant.now().toString())
    }
    fun fetch(code:String):Benchmark {
        var failure:Exception?=null
        for(count in listOf(1,5)) {
            val connection=URL("https://api.bcb.gov.br/dados/serie/bcdata.sgs.${series.getValue(code)}/dados/ultimos/$count?formato=json").openConnection() as HttpsURLConnection
            connection.connectTimeout=8000;connection.readTimeout=8000;connection.instanceFollowRedirects=false
            try {
                require(connection.responseCode==200) {"Fonte de taxas indisponível."}
                val body=connection.inputStream.use {stream->val buffer=java.io.ByteArrayOutputStream();val block=ByteArray(2048);while(true) {val n=stream.read(block);if(n<0) break;require(buffer.size()+n<=16384);buffer.write(block,0,n)};buffer.toString("UTF-8")}
                return parse(code,body)
            } catch(e:Exception) {failure=e} finally {connection.disconnect()}
        }
        throw failure ?: IllegalStateException("Fonte de taxas indisponível.")
    }
}
