package tech.igrant.jizhang

import org.junit.Assert
import org.junit.Test
import tech.igrant.jizhang.main.contract.MainContract
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainContractTest {

    @Test
    fun testModel() {
        val model = MainContract.Model.Impl(
            LocalDate.parse(
                "2021-05-07",
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            ).atStartOfDay()
        )
        val dates = model.dates()
        val datesExpected = arrayListOf(
            "2021-05-02",
            "2021-05-03",
            "2021-05-04",
            "2021-05-05",
            "2021-05-06",
            "2021-05-07",
            "2021-05-08",
        )
        dates.forEachIndexed { index, localDateTime ->
            Assert.assertEquals(
                datesExpected[index],
                localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )
        }
        for (i in 0..6) {
            model.setIndex(i)
            Assert.assertEquals(
                datesExpected[i],
                model.dateStr()
            )
        }
    }

}