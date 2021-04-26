package tech.igrant.jizhang

import org.junit.Assert
import org.junit.Test
import tech.igrant.jizhang.main.contract.MainContract
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainContractTest {

    @Test
    fun testModel() {
        monday_case()
        wednesday_case()
        sunday_case()
    }

    private fun wednesday_case() {
        val model = MainContract.Model.Impl(
            LocalDate.parse(
                "2021-04-21",
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            ).atStartOfDay()
        )
        model.daySelectable().forEachIndexed { index, b ->
            when (index) {
                0 -> {
                    Assert.assertTrue(b)
                }
                1 -> {
                    Assert.assertTrue(b)
                }
                2 -> {
                    Assert.assertTrue(b)
                }
                else -> {
                    Assert.assertFalse(b)
                }
            }
        }
    }

    private fun monday_case() {
        val model = MainContract.Model.Impl(
            LocalDate.parse(
                "2021-04-19",
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            ).atStartOfDay()
        )
        model.daySelectable().forEachIndexed { index, b ->
            if (index == 0) {
                Assert.assertTrue(b)
            } else {
                Assert.assertFalse(b)
            }
        }
        Assert.assertEquals("2021-04-19", model.dateStr())

        // set to Monday
        model.changeWeekIndex(7)
        Assert.assertEquals("2021-04-25", model.dateStr())
    }

    private fun sunday_case() {
        val model = MainContract.Model.Impl(
            LocalDate.parse(
                "2021-04-25",
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            ).atStartOfDay()
        )

        model.daySelectable().forEachIndexed { _, b ->
            Assert.assertTrue(b)
        }

        Assert.assertSame(7, model.getWeekIndex())
        Assert.assertEquals("2021-04-25", model.dateStr())

        // set to Monday
        model.changeWeekIndex(1)
        Assert.assertSame(1, model.getWeekIndex())
        Assert.assertEquals("2021-04-19", model.dateStr())

        // set to Tuesday
        model.changeWeekIndex(2)
        Assert.assertSame(2, model.getWeekIndex())
        Assert.assertEquals("2021-04-20", model.dateStr())
    }

}