package tech.igrant.jizhang.main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tech.igrant.jizhang.R
import tech.igrant.jizhang.databinding.ActivityMainBinding
import tech.igrant.jizhang.databinding.ItemDetailBinding
import tech.igrant.jizhang.databinding.ItemDetailDateBinding
import tech.igrant.jizhang.framework.ext.format
import tech.igrant.jizhang.main.contract.MainContract
import tech.igrant.jizhang.main.detail.CreateDetailActivity
import tech.igrant.jizhang.main.detail.DetailAction
import tech.igrant.jizhang.main.detail.DetailService
import tech.igrant.jizhang.main.subject.SubjectService
import java.time.LocalDateTime

class MainActivity : AppCompatActivity(), MainContract.View {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: MainContract.Presenter
    private lateinit var model: MainContract.Model
    private lateinit var subjectIdToNameLookup: Map<Long, String>
    private lateinit var childToParent: Map<Long, Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLookup()
        binding = ActivityMainBinding.inflate(layoutInflater)
        model = MainContract.Model.Impl(LocalDateTime.now())
        presenter = MainContract.Presenter.Impl(this, model)
        binding.titleBar.rightTextView.setOnClickListener {
            presenter.navigateToCreate(this)
        }
        setContentView(binding.root)
        presenter.startup()
        presenter.loadData()
    }

    private fun initLookup() {
        val subjects = SubjectService.loadSubjectSync()
        subjectIdToNameLookup = subjects.fold(
            mutableMapOf(),
            { acc, subject ->
                acc[subject.id] = subject.name
                acc
            }
        )
        childToParent = subjects.fold(
            mutableMapOf(),
            { acc, subject ->
                subject.parentId?.let {
                    acc[subject.id] = it
                }
                acc
            }
        )
    }

    private fun toAdapter(
        list: List<DetailService.DetailViewObject.Local>,
        onClickDetail: (detailVo: DetailService.DetailViewObject.Local, detailAction: DetailAction) -> Unit
    ): DetailAdapter {
        val renderLines: ArrayList<RenderLine> = ArrayList()
        val grouped = list.groupBy { detail ->
            childToParent[detail.subjectId]
        }
        for ((pid, details) in grouped) {
            subjectIdToNameLookup[pid]?.let {
                renderLines.add(RenderLine(it, RenderLine.BANNER))
            }
            for (detailVo in details) {
                renderLines.add(RenderLine(detailVo, RenderLine.CONTENT))
            }
        }
        return DetailAdapter(renderLines, onClickDetail)
    }

    data class RenderLine(val t: Any, val type: Int) {

        companion object {
            const val BANNER = 1
            const val CONTENT = 2
        }
    }

    abstract class AbsDetailViewHolder(private val v: View) : RecyclerView.ViewHolder(v) {
        abstract fun bind(
            renderLine: RenderLine,
            renderLines: List<RenderLine>,
            onClickDetail: (detailVo: DetailService.DetailViewObject.Local, detailAction: DetailAction) -> Unit
        )

        fun getColors(userId: Long): Int {
            val colors = listOf(R.color.bg_1, R.color.bg_2, R.color.bg_3)
            return v.context.getColor(colors[userId.toInt() % 3])
        }
    }

    class DetailViewHolder(private val v: View, private val itemDetailBinding: ItemDetailBinding) :
        AbsDetailViewHolder(v) {

        @SuppressLint("SetTextI18n")
        override fun bind(
            renderLine: RenderLine,
            renderLines: List<RenderLine>,
            onClickDetail: (detailVo: DetailService.DetailViewObject.Local, detailAction: DetailAction) -> Unit
        ) {
            val detailVo = renderLine.t as DetailService.DetailViewObject.Local
            itemDetailBinding.detailCard.setCardBackgroundColor(
                getColors(
                    detailVo.userId
                )
            )
            itemDetailBinding.optCopy.setOnClickListener {
                onClickDetail(
                    detailVo,
                    DetailAction.COPY
                )
            }
            itemDetailBinding.optEdit.setOnClickListener {
                onClickDetail(
                    detailVo,
                    DetailAction.EDIT
                )
            }
            itemDetailBinding.optDelete.setOnClickListener {
                onClickDetail(
                    detailVo,
                    DetailAction.DELETE
                )
            }
            itemDetailBinding.subject.text = "#${detailVo.subjectName}"
            itemDetailBinding.user.text = "@${detailVo.username}"
            detailVo.remark?.let {
                if (it.isEmpty()) {
                    itemDetailBinding.remark.text = "什么都没写哦"
                } else {
                    itemDetailBinding.remark.text = it
                }
            }
            if (detailVo.extern()) {
                itemDetailBinding.amount.text = "-¥${(detailVo.amount.toDouble() / 100).format(2)}"
                itemDetailBinding.amount.setTextColor(v.context.getColor(R.color.amount_out))
            } else {
                itemDetailBinding.amount.text = "+¥${(detailVo.amount.toDouble() / 100).format(2)}"
                itemDetailBinding.amount.setTextColor(v.context.getColor(R.color.amount_in))
            }
            itemDetailBinding.account.text = "@" + (detailVo.sourceAccountName
                ?: detailVo.destAccountName)
        }
    }

    class DetailViewDateHolder(
        v: View,
        private val itemDetailDateBinding: ItemDetailDateBinding
    ) : AbsDetailViewHolder(v) {
        override fun bind(
            renderLine: RenderLine,
            renderLines: List<RenderLine>,
            onClickDetail: (detailVo: DetailService.DetailViewObject.Local, detailAction: DetailAction) -> Unit
        ) {
            val dateString = renderLine.t as String
            itemDetailDateBinding.detailListDate.text = dateString
        }
    }

    class DetailAdapter(
        private val renderLine: List<RenderLine>,
        private val onClickDetail: (detailVo: DetailService.DetailViewObject.Local, detailAction: DetailAction) -> Unit
    ) : RecyclerView.Adapter<AbsDetailViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsDetailViewHolder {
            return when (viewType) {
                RenderLine.BANNER -> {
                    val bind = ItemDetailDateBinding.inflate(LayoutInflater.from(parent.context))
                    DetailViewDateHolder(bind.root, bind)
                }
                else -> {
                    val bind = ItemDetailBinding.inflate(LayoutInflater.from(parent.context))
                    DetailViewHolder(bind.root, bind)
                }
            }
        }

        override fun getItemCount(): Int = renderLine.size

        override fun getItemViewType(position: Int): Int = renderLine[position].type

        override fun onBindViewHolder(holder: AbsDetailViewHolder, position: Int) {
            holder.bind(renderLine[position], renderLine, onClickDetail)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CreateDetailActivity.COMMON_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        presenter.loadData()
                    }
                    else -> {
                    }
                }
            }
            else -> {
            }
        }

    }

    override fun renderTitle(dateStr: String, offline: Boolean) {
        if (offline) {
            binding.titleBar.leftTextView.text =
                getString(R.string.offline_mode_tips)
        } else {
            binding.titleBar.leftTextView.text =
                getString(R.string.online_mode_tips)
        }
        binding.titleBar.centerTextView.text = dateStr
    }

    override fun renderList(list: List<DetailService.DetailViewObject.Local>) {
        binding.details.layoutManager = LinearLayoutManager(this)
        binding.details.adapter = toAdapter(
            list,
            onClickDetail = { detailVo, detailAction ->
                when (detailAction) {
                    DetailAction.EDIT -> {
                        CreateDetailActivity.startAsEditMode(
                            this,
                            detailVo.toTransferObject()
                        )
                    }
                    DetailAction.COPY -> {
                        CreateDetailActivity.startAsCreateMode(
                            this,
                            detailVo.toTransferObject()
                        )
                    }
                    DetailAction.DELETE -> {
                        AlertDialog.Builder(this)
                            .setMessage(R.string.delete_tips)
                            .setPositiveButton(R.string.ok) { dialog, _ ->
                                DetailService.delete(detailVo)
                                    .subscribe {
                                        dialog.dismiss()
                                        presenter.loadData()
                                    }
                            }
                            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                }
            }
        )
    }

    override fun highLight(weekIndex: Int) {
        val tvs = listOf(
            binding.mon,
            binding.tue,
            binding.wed,
            binding.thur,
            binding.fri,
            binding.sat,
            binding.sun
        )
        tvs.forEach { textView ->
            textView.setBackgroundColor(getColor(android.R.color.white))
        }
        tvs[weekIndex].setBackgroundColor(getColor(R.color.bg_1))
    }

    override fun renderWeekButtons(tvs: List<Int>) {
        listOf(
            binding.mon,
            binding.tue,
            binding.wed,
            binding.thur,
            binding.fri,
            binding.sat,
            binding.sun
        ).forEachIndexed { index, textView ->
            textView.text = getString(tvs[index])
            textView.setOnClickListener {
                presenter.onClick(index)
            }
        }
    }

    override fun renderOfflineData(list: List<DetailService.DetailViewObject.Local>) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.offline_data_title))
            .setMessage(String.format(getString(R.string.offline_data_tips), list.size))
            .setNegativeButton(getText(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(getText(R.string.ok)) { dialog, _ ->
                DetailService
                    .createBatch(list.map { it.toTransferObject() })
                    .subscribe {
                        dialog.dismiss()
                        DetailService.clearLocal()
                        presenter.loadData()
                    }
            }
            .show()
    }

}