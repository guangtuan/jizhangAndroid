package tech.igrant.jizhang.main.contract

import tech.igrant.jizhang.main.detail.DetailService
import tech.igrant.jizhang.state.EnvManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface MainContract {

    interface View {
        fun renderTitle(dateStr: String, offline: Boolean)
        fun renderList(list: List<DetailService.DetailViewObject.Local>)
        fun renderOfflineData(list: List<DetailService.DetailViewObject.Local>)
        fun renderWeekButton(weekIndex: Int)
        fun renderWeekButtons(dateSelectable: List<Boolean>)
    }

    interface Model {
        fun dateStr(): String
        fun offline(): Boolean
        fun dateSelected(): LocalDateTime
        fun changeWeekIndex(index: Int)
        fun getWeekIndex(): Int
        fun daySelectable(): List<Boolean>

        class Impl(private val today: LocalDateTime) : Model {
            // weekIndex 1 -> 7 其他的 index 0 -> 6
            private var weekIndex: Int

            init {
                weekIndex = today.dayOfWeek.value
            }

            override fun daySelectable(): List<Boolean> {
                return IntArray(7).mapIndexed { index: Int, _: Int -> index + 1 <= today.dayOfWeek.value }
            }

            override fun getWeekIndex(): Int {
                return weekIndex
            }

            override fun changeWeekIndex(index: Int) {
                weekIndex = index
            }

            override fun dateSelected(): LocalDateTime {
                if (today.dayOfWeek.value == weekIndex) {
                    return today
                }
                return today.minusDays((today.dayOfWeek.value - weekIndex).toLong())
            }

            override fun dateStr(): String {
                return dateSelected().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }

            override fun offline(): Boolean {
                return EnvManager.offline()
            }
        }
    }

    interface Presenter {
        fun startup()
        fun loadData()
        fun onClick(index: Int)

        class Impl(private val view: View, private val model: Model) : Presenter {
            override fun startup() {
                view.renderTitle(
                    dateStr = model.dateStr(),
                    offline = model.offline()
                )
                view.renderWeekButtons(model.daySelectable())
            }

            override fun loadData() {
                DetailService
                    .load(
                        model.dateSelected().toLocalDate().atStartOfDay(),
                        model.dateSelected().plusDays(1).toLocalDate().atStartOfDay()
                            .minusSeconds(1),
                    )
                    .subscribe { list ->
                        view.renderList(list)
                        view.renderWeekButton(model.getWeekIndex())
                    }
                if (!model.offline()) {
                    DetailService.loadAllFromLocal()
                        .filter { list -> list.isNotEmpty() }
                        .subscribe { list ->
                            view.renderOfflineData(list)
                        }
                }
            }

            override fun onClick(index: Int) {
                if (model.getWeekIndex() == index + 1) {
                    return
                }
                model.changeWeekIndex(index + 1)
                view.renderTitle(
                    dateStr = model.dateStr(),
                    offline = model.offline()
                )
                loadData()
            }
        }

    }

}