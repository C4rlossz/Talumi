package com.talumi.financas

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=android.app.Application::class)
class MarketRatesTest {
    private val today=LocalDate.of(2026,10,22)
    @Test fun officialSeriesResponseUsesMostRecentDate() {
        val rate=MarketRates.parse("CDI","""[{"data":"20/10/2026","valor":"13.65"},{"data":"21/10/2026","valor":"13.65"}]""",today)
        assertEquals("CDI",rate.code);assertEquals("13.65",rate.annual);assertEquals(today.minusDays(1),rate.observed)
    }
    @Test fun invalidStaleFutureAndOutOfRangeResponsesAreRejected() {
        listOf("[]","{}","""[{"data":"23/10/2026","valor":"13.65"}]""","""[{"data":"01/01/2026","valor":"13.65"}]""","""[{"data":"21/10/2026","valor":"NaN"}]""","""[{"data":"21/10/2026","valor":"101"}]""").forEach {body->assertThrows(Exception::class.java) {MarketRates.parse("SELIC",body,today)}}
    }
}
