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
import tech.igrant.jizhang.main.detail.CreateDetailActivity
import tech.igrant.jizhang.main.detail.DetailService
import tech.igrant.jizhang.state.EnvManager
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (EnvManager.offline()) {
            supportActionBar?.setTitle(R.string.offline_mode_tips)
        } else {
            supportActionBar?.setTitle(R.string.online_mode_tips)
        }
        binding.createDetail.setOnClickListener {
            CreateDetailActivity.startAsCreateMode(this)
        }
        loadData()
    }

    private fun loadData() {
        DetailService.load()
                .subscribe { list ->
                    binding.details.layoutManager = LinearLayoutManager(this)
                    binding.details.adapter =
                            toAdapter(
                                    list,
                                    onClickDetail = { CreateDetailActivity.startAsEditMode(this, it.toTransferObject()) }
                            )
                }
        if (EnvManager.online()) {
            checkOfflineData()
        }
    }

    private fun checkOfflineData() {
        DetailService.loadFromLocal()
                .filter { list -> list.isNotEmpty() }
                .subscribe { list ->
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
                                            loadData()
                                        }
                            }
                            .show()
                }
    }

    private fun toAdapter(
            list: List<DetailService.DetailViewObject.Local>,
            onClickDetail: (detailVo: DetailService.DetailViewObject.Local) -> Unit
    ): DetailAdapter {
        val renderLines: ArrayList<RenderLine> = ArrayList()
        val grouped = list.groupBy { detail ->
            detail.createdAt.format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
            )
        }
        for (entry in grouped) {
            renderLines.add(RenderLine(entry.key, RenderLine.BANNER))
            for (detailVo in entry.value) {
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

    abstract class AbsDetailViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        abstract fun bind(renderLine: RenderLine, onClickDetail: (detailVo: DetailService.DetailViewObject.Local) -> Unit)

        companion object {
            val colors = listOf(R.color.bg_1, R.color.bg_2, R.color.bg_3)
        }
    }

    class DetailViewHolder(val v: View, private val itemDetailBinding: ItemDetailBinding) : AbsDetailViewHolder(v) {

        @SuppressLint("SetTextI18n")
        override fun bind(renderLine: RenderLine, onClickDetail: (detailVo: DetailService.DetailViewObject.Local) -> Unit) {
            val detailVo = renderLine.t as DetailService.DetailViewObject.Local
            itemDetailBinding.detailCard.setCardBackgroundColor(v.context.getColor(colors.random()))
            itemDetailBinding.detailCard.setOnClickListener { onClickDetail(detailVo) }
            itemDetailBinding.subject.text = "#${detailVo.subjectName}"
            itemDetailBinding.user.text = "@${detailVo.username}"
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

    class DetailViewDateHolder(val v: View, private val itemDetailDateBinding: ItemDetailDateBinding) : AbsDetailViewHolder(v) {
        override fun bind(renderLine: RenderLine, onClickDetail: (detailVo: DetailService.DetailViewObject.Local) -> Unit) {
            val dateString = renderLine.t as String
            itemDetailDateBinding.detailListDate.text = dateString
        }
    }

    class DetailAdapter(
            private val renderLine: List<RenderLine>,
            private val onClickDetail: (detailVo: DetailService.DetailViewObject.Local) -> Unit
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
            holder.bind(renderLine[position], onClickDetail)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CreateDetailActivity.COMMON_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        loadData()
                    }
                    else -> {
                    }
                }
            }
            else -> {
            }
        }

    }

}