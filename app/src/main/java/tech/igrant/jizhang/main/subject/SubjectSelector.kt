package tech.igrant.jizhang.main.subject

import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tech.igrant.jizhang.R
import tech.igrant.jizhang.comp.SelectOptions
import tech.igrant.jizhang.databinding.ItemBigSubjectBinding
import tech.igrant.jizhang.databinding.ItemSmallSubjectBinding
import tech.igrant.jizhang.databinding.SubjectSelectorBinding
import tech.igrant.jizhang.framework.IdName


class SubjectSelector {

    companion object {
        fun create(ctx: Context, map: SelectOptions, onClickItem: (idName: IdName) -> Unit) {
            val alertDialog = FullScreenDialog(ctx)
            val binding = SubjectSelectorBinding.inflate(LayoutInflater.from(ctx))

            val rightAdapter = RightAdapter(
                data = map.first(),
                onClickItem = { idName ->
                    alertDialog.dismiss()
                    onClickItem(idName)
                }
            )
            binding.smallCat.adapter = rightAdapter
            binding.smallCat.layoutManager = LinearLayoutManager(ctx)
            binding.smallCat.addItemDecoration(BigCatDe(ctx))

            binding.bigCat.adapter = LeftAdapter(
                data = map.keys,
                onClickItem = { item ->
                    rightAdapter.data = map.get(item)
                    rightAdapter.notifyDataSetChanged()
                }
            )
            binding.bigCat.layoutManager = LinearLayoutManager(ctx)
            binding.bigCat.addItemDecoration(BigCatDe(ctx))

            binding.cancelButton.setOnClickListener { alertDialog.dismiss() }

            alertDialog.setContentView(binding.root)
            alertDialog.show()
        }
    }

    class BigCatDe(ctx: Context) : RecyclerView.ItemDecoration() {
        private var dividerPaint: Paint = Paint()
        private var dividerHeight: Int

        init {
            dividerPaint.color = ctx.resources.getColor(R.color.divider)
            dividerHeight = ctx.resources.getDimensionPixelSize(R.dimen.divider)
        }

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.bottom = dividerHeight;
        }

        override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val childCount: Int = parent.childCount
            val left: Int = parent.paddingLeft
            val right: Int = parent.width - parent.paddingRight

            for (i in 0 until childCount) {
                parent.getChildAt(i).let { view ->
                    val top = view.bottom.toFloat()
                    val bottom = (view.bottom + dividerHeight).toFloat()
                    canvas.drawRect(left.toFloat(), top, right.toFloat(), bottom, dividerPaint)
                }
            }
        }
    }

    class FullScreenDialog(ctx: Context) :
        Dialog(ctx, R.style.DialogTheme)

    class LeftViewHolder(v: View, private val binding: ItemBigSubjectBinding) :
        RecyclerView.ViewHolder(v) {
        fun bind(bean: IdName, onClickItem: (idName: IdName) -> Unit) {
            binding.subjectName.text = bean.name
            binding.subjectName.setOnClickListener { onClickItem(bean) }
        }
    }

    class RightViewHolder(v: View, private val binding: ItemSmallSubjectBinding) :
        RecyclerView.ViewHolder(v) {
        fun bind(bean: IdName, onClickItem: (idName: IdName) -> Unit) {
            binding.subjectName.text = bean.name
            binding.subjectName.setOnClickListener { onClickItem(bean) }
        }
    }

    class LeftAdapter(
        private val data: List<IdName>,
        private val onClickItem: (idName: IdName) -> Unit
    ) : RecyclerView.Adapter<LeftViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeftViewHolder {
            val binding =
                ItemBigSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return LeftViewHolder(binding.root, binding)
        }

        override fun onBindViewHolder(holder: LeftViewHolder, position: Int) {
            holder.bind(data[position], onClickItem)
        }

        override fun getItemCount(): Int = data.size

    }

    class RightAdapter(
        var data: List<IdName>,
        private val onClickItem: (idName: IdName) -> Unit
    ) : RecyclerView.Adapter<RightViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RightViewHolder {
            val binding =
                ItemSmallSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return RightViewHolder(binding.root, binding)
        }

        override fun onBindViewHolder(holder: RightViewHolder, position: Int) {
            holder.bind(data[position], onClickItem)
        }

        override fun getItemCount(): Int = data.size

    }

}