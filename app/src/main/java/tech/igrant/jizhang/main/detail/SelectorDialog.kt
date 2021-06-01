package tech.igrant.jizhang.main.detail

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tech.igrant.jizhang.R
import tech.igrant.jizhang.databinding.IdNameSelectListBinding
import tech.igrant.jizhang.databinding.ItemSelectBinding
import tech.igrant.jizhang.framework.IdName

class SelectorDialog {

    class IdNameViewHolder(private val binding: ItemSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            bean: IdName,
            callback: (idName: IdName) -> Unit
        ) {
            binding.itemSelectText.text = bean.name
            binding.itemSelectTextContainer.setOnClickListener { callback(bean) }
        }
    }

    class IdNameAdapter(
        private val idNames: List<IdName>,
        private val callback: (idName: IdName) -> Unit
    ) :
        RecyclerView.Adapter<IdNameViewHolder>() {

        override fun getItemId(position: Int): Long = idNames[position].id

        override fun getItemCount() = idNames.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdNameViewHolder {
            return IdNameViewHolder(
                ItemSelectBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: IdNameViewHolder, position: Int) {
            holder.bind(idNames[position], callback)
        }
    }

    companion object {
        fun show(
            activity: Activity,
            idNames: List<IdName>,
            onItemSelect: (idName: IdName) -> Unit
        ) {
            val binding: IdNameSelectListBinding =
                IdNameSelectListBinding.inflate(activity.layoutInflater)
            val dialog = AlertDialog.Builder(activity)
                .setView(binding.root)
                .setNegativeButton(
                    activity.getText(R.string.cancel)
                ) { dialog, which -> dialog.dismiss() }
                .create()
            binding.mainList.layoutManager = LinearLayoutManager(activity)
            binding.mainList.adapter =
                IdNameAdapter(idNames) { idName -> onItemSelect(idName); dialog.dismiss(); }
            dialog.show()
        }
    }

}