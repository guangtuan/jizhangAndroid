package tech.igrant.jizhang.main.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import tech.igrant.jizhang.R
import tech.igrant.jizhang.databinding.FragmentDetailBinding
import tech.igrant.jizhang.framework.PageQuery
import tech.igrant.jizhang.framework.RetrofitFacade
import tech.igrant.jizhang.framework.ext.inflate
import tech.igrant.jizhang.state.EnvManager
import java.time.format.DateTimeFormatter

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_detail, container, false)
        binding = FragmentDetailBinding.bind(root)
        binding.createDetail.setOnClickListener {
            CreateDetailActivity.startForResult(this)
        }
        loadData()
        return binding.root
    }

    private fun loadData() {
        if (EnvManager.offline()) {
            return
        }
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
                binding.details.layoutManager = LinearLayoutManager(this.context)
                val groupedDetails = list.groupBy { detail ->
                    detail.createdAt.format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    )
                }
                binding.details.adapter = DetailAdapter(groupedDetails.keys.flatMap { it ->
                    mutableListOf<RenderLine>().apply {
                        this.add(RenderLine(it, RenderLine.BANNER))
                        groupedDetails[it]?.map { content ->
                            RenderLine(
                                content,
                                RenderLine.CONTENT
                            )
                        }?.forEach { this.add(it) }
                    }
                })
            }
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
                    holder.itemView.findViewById<TextView>(R.id.subject).text = "#$subjectName"
                    val amountTv = holder.itemView.findViewById<TextView>(R.id.amount)
                    if (detailVo.extern()) {
                        amountTv.text = "-¥${amount / 100}"
                        amountTv.setTextColor(holder.itemView.context.getColor(R.color.amount_out))
                    } else {
                        amountTv.text = "+¥${amount / 100}"
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