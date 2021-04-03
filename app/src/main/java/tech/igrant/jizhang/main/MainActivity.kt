package tech.igrant.jizhang.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import tech.igrant.jizhang.R
import tech.igrant.jizhang.databinding.ActivityMainBinding
import tech.igrant.jizhang.framework.PageQuery
import tech.igrant.jizhang.framework.RetrofitFacade
import tech.igrant.jizhang.framework.ext.format
import tech.igrant.jizhang.framework.ext.inflate
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
            CreateDetailActivity.startForResult(this)
        }
        loadData()
    }

    private fun loadData() {
        if (EnvManager.offline()) {
            DetailService.loadFromLocal()
                .subscribe { list ->
                    binding.details.layoutManager = LinearLayoutManager(this)
                    binding.details.adapter =
                        toAdapter(list.map { detailTo -> DetailService.DetailVo.fromTo(detailTo) })
                }
            return
        } else {
            loadFormServer()
        }
    }

    private fun loadFormServer() {
        RetrofitFacade.get().create(DetailService::class.java).list(
            PageQuery(
                queryParam = DetailService.DetailQuery.first(),
                page = 0,
                size = 10
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.content }
            .subscribe { list ->
                binding.details.layoutManager = LinearLayoutManager(this)
                val detailAdapter = toAdapter(list)
                binding.details.adapter = detailAdapter
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
                    .setNegativeButton(getText(R.string.cancel)) { dialog, which -> dialog.dismiss() }
                    .setPositiveButton(getText(R.string.ok)) { dialog, which ->
                        DetailService.createBatch(
                            list
                        ).subscribe {
                            dialog.dismiss()
                            DetailService.clearLocal()
                            loadData()
                        }
                    }
                    .show()
            }
    }

    private fun toAdapter(list: List<DetailService.DetailVo>): DetailAdapter {
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
        return DetailAdapter(renderLines)
    }

    data class RenderLine(val t: Any, val type: Int) {

        companion object {
            const val BANNER = 1
            const val CONTENT = 2
        }
    }

    class DetailViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    }

    class DetailAdapter(private val renderLine: List<RenderLine>) :
        RecyclerView.Adapter<DetailViewHolder>() {

        private val colors = listOf<Int>(R.color.bg_1, R.color.bg_2, R.color.bg_3)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
            return when (viewType) {
                RenderLine.BANNER -> {
                    val inflatedView = parent.inflate(R.layout.item_detail_date, false)
                    DetailViewHolder(inflatedView)
                }
                else -> {
                    val inflatedView = parent.inflate(R.layout.item_detail, false)
                    DetailViewHolder(inflatedView)
                }
            }
        }

        override fun getItemCount(): Int = renderLine.size

        override fun getItemViewType(position: Int): Int = renderLine[position].type

        override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
            val itemViewType = getItemViewType(position)
            when (itemViewType) {
                RenderLine.CONTENT -> {
                    val detailVo = renderLine[position].t as DetailService.DetailVo
                    val (
                        id,
                        userId,
                        username,
                        sourceAccountId,
                        sourceAccountName,
                        destAccountId,
                        destAccountName,
                        subjectId,
                        subjectName,
                        remark,
                        createdAt,
                        updatedAt,
                        amount,
                        splited,
                        parentId
                    ) = detailVo
                    holder.itemView.findViewById<CardView>(R.id.detail_card).setCardBackgroundColor(holder.itemView.context.getColor(colors.random()))
                    holder.itemView.findViewById<TextView>(R.id.subject).text = "#$subjectName"
                    holder.itemView.findViewById<TextView>(R.id.user).text = "@$username"
                    val amountTv = holder.itemView.findViewById<TextView>(R.id.amount)
                    if (detailVo.extern()) {
                        amountTv.text = "-¥${(amount.toDouble() / 100).format(2)}"
                        amountTv.setTextColor(holder.itemView.context.getColor(R.color.amount_out))
                    } else {
                        amountTv.text = "+¥${(amount.toDouble() / 100).format(2)}"
                        amountTv.setTextColor(holder.itemView.context.getColor(R.color.amount_in))
                    }
                    holder.itemView.findViewById<TextView>(R.id.account).text =
                        "@" + (sourceAccountName ?: destAccountName)
                }
                else -> {
                    val dateString = renderLine[position].t as String
                    holder.itemView.findViewById<TextView>(R.id.detail_list_date).text = dateString
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CreateDetailActivity.REQUEST_CODE -> {
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