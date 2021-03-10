package tech.igrant.jizhang.main.detail

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
import tech.igrant.jizhang.framework.PageQuery
import tech.igrant.jizhang.framework.RetrofitFacade
import tech.igrant.jizhang.framework.ext.inflate
import java.time.format.DateTimeFormatter

class DetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_detail, container, false)
        val detailService = RetrofitFacade.get().create(DetailService::class.java)
        root.findViewById<FloatingActionButton>(R.id.create_detail).apply {
            this.setOnClickListener {
                CreateDetailActivity.start(this@DetailFragment.requireContext())
            }
        }
        root.findViewById<RecyclerView>(R.id.details).apply {
            detailService.list(
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
                    this.layoutManager = LinearLayoutManager(this.context)
                    val groupedDetails = list.groupBy { detail ->
                        detail.createdAt.format(
                            DateTimeFormatter.ofPattern("YYYY-MM-dd")
                        )
                    }
                    this.adapter = DetailAdapter(groupedDetails.keys.flatMap { it ->
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
        return root
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
                    holder.itemView.findViewById<Chip>(R.id.subject).text = subjectName
                    val amountTv = holder.itemView.findViewById<TextView>(R.id.amount)
                    if (detailVo.extern()) {
                        amountTv.text = "-¥${amount / 100}"
                        amountTv.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
                    } else {
                        amountTv.text = "+¥${amount / 100}"
                        amountTv.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
                    }
                    holder.itemView.findViewById<Chip>(R.id.account).text =
                        sourceAccountName ?: destAccountName
                }
                else -> {
                    val dateString = renderLine[position].t as String
                    holder.itemView.findViewById<TextView>(R.id.detail_list_date).text = dateString
                }
            }
        }

    }

}