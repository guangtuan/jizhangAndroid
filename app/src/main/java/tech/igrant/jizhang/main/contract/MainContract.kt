package tech.igrant.jizhang.main.contract

import android.app.Activity
import tech.igrant.jizhang.R
import tech.igrant.jizhang.framework.ext.toDate
import tech.igrant.jizhang.main.detail.CreateDetailActivity
import tech.igrant.jizhang.main.detail.DetailService
import tech.igrant.jizhang.state.EnvManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface MainContract {

    interface View {
        fun renderTitle(dateStr: String, offline: Boolean)
        fun renderList(list: List<DetailService.DetailViewObject.Local>)
        fun renderOfflineData(list: List<DetailService.DetailViewObject.Local>)
        fun highLight(weekIndex: Int)
        fun renderWeekButtons(tvs: List<Int>)
    }

    interface Model {
        fun dateStr(): String
        fun offline(): Boolean
        fun dateSelected(): LocalDateTime
        fun setIndex(index: Int)
        fun getIndex(): Int
        fun dates(): List<LocalDateTime>
        fun weekDateStrResource(): List<Int>

        class Impl(today: LocalDateTime) : Model {
            private var index = 3
            private var dates: List<LocalDateTime> = listOf(
                today.minusDays(3),
                today.minusDays(2),
                today.minusDays(1),
                today,
                today.plusDays(1),
                today.plusDays(2),
                today.plusDays(3),
            )

            override fun dates(): List<LocalDateTime> {
                return dates
            }

            override fun getIndex(): Int {
                return index
            }

            override fun setIndex(index: Int) {
                this.index = index
            }

            override fun dateSelected(): LocalDateTime {
                return dates[index]
            }

            override fun dateStr(): String {
                return dateSelected().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }

            override fun offline(): Boolean {
                return EnvManager.offline()
            }

            override fun weekDateStrResource(): List<Int> {
                val lookup = mapOf(
                    Pair(1, R.string.mon),
                    Pair(2, R.string.tue),
                    Pair(3, R.string.wed),
                    Pair(4, R.string.thur),
                    Pair(5, R.string.fri),
                    Pair(6, R.string.sat),
                    Pair(7, R.string.sun),
                )
                return dates.map { it.dayOfWeek.value }
                    .mapNotNull { dayOfWeek -> lookup[dayOfWeek] }
            }
        }

    }

    interface Presenter {
        fun startup()
        fun loadData()
        fun onClick(index: Int)
        fun navigateToCreate(activity: Activity)

        class Impl(private val view: View, private val model: Model) : Presenter {
            override fun startup() {
                view.renderTitle(
                    dateStr = model.dateStr(),
                    offline = model.offline()
                )
                view.renderWeekButtons(model.weekDateStrResource())
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
                        view.highLight(model.getIndex())
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
                if (model.getIndex() == index) {
                    return
                }
                model.setIndex(index)
                view.renderTitle(
                    dateStr = model.dateStr(),
                    offline = model.offline()
                )
                loadData()
            }

            override fun navigateToCreate(activity: Activity) {
                CreateDetailActivity.startAsCreateMode(activity, model.dateSelected().toDate().time)
            }

        }

    }

}