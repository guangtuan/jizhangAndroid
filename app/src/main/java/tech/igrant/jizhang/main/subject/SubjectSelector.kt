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


class SubjectSelector {

    companion object {
        fun create(ctx: Context, map: SelectOptions) {
            val binding = SubjectSelectorBinding.inflate(LayoutInflater.from(ctx))
            binding.bigCat.adapter = LeftAdapter(map.keys)
            binding.bigCat.layoutManager = LinearLayoutManager(ctx)
            binding.bigCat.addItemDecoration(BigCatDe(ctx))
            binding.smallCat.adapter = RightAdapter(map.first())
            binding.smallCat.layoutManager = LinearLayoutManager(ctx)
            val alertDialog = FullScreenDialog(ctx)
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
        fun bind(s: String) {
            binding.subjectName.text = s
            binding.subjectName.setOnClickListener { v -> v }
        }
    }

    class RightViewHolder(v: View, private val binding: ItemSmallSubjectBinding) :
        RecyclerView.ViewHolder(v) {
        fun bind(s: String) {
            binding.subjectName.text = s
            binding.subjectName.setOnClickListener { v -> v }
        }
    }

    class LeftAdapter(
        private val data: List<String>
    ) : RecyclerView.Adapter<LeftViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeftViewHolder {
            val binding =
                ItemBigSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return LeftViewHolder(binding.root, binding)
        }

        override fun onBindViewHolder(holder: LeftViewHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int = data.size

    }

    class RightAdapter(
        private val data: List<String>
    ) : RecyclerView.Adapter<RightViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RightViewHolder {
            val binding =
                ItemSmallSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return RightViewHolder(binding.root, binding)
        }

        override fun onBindViewHolder(holder: RightViewHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int = data.size

    }

}