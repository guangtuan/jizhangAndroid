package tech.igrant.jizhang.main.subject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tech.igrant.jizhang.R
import tech.igrant.jizhang.databinding.FragmentSubjectBinding
import tech.igrant.jizhang.framework.ext.inflate
import tech.igrant.jizhang.state.EnvManager

class SubjectFragment : Fragment() {

    private lateinit var binding: FragmentSubjectBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_subject, container, false)
        binding = FragmentSubjectBinding.bind(root)
        loadData()
        return root
    }

    private fun loadData() {
        if (EnvManager.offline()) {
            return
        }
        SubjectService.loadSubject()
            .map {
                it.flatMap { l -> mutableListOf(l).also { single -> single.addAll(l.children) } }
            }
            .subscribe { list ->
                binding.subjects.layoutManager = GridLayoutManager(this.context, 3)
                binding.subjects.adapter = SubjectAdapter(list)
            }
    }

    open class AbsSubjectViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    }

    class BigSubjectViewHolder(v: View) : AbsSubjectViewHolder(v) {

    }

    class SmallSubjectViewHolder(v: View) : AbsSubjectViewHolder(v) {

    }

    class SubjectAdapter(private val subjects: List<SubjectService.SubjectVo>) :
        RecyclerView.Adapter<AbsSubjectViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsSubjectViewHolder {
            return if (viewType == SubjectService.LEVEL_BIG) {
                val inflatedView = parent.inflate(R.layout.item_big_subject, false)
                BigSubjectViewHolder(inflatedView)
            } else {
                val inflatedView = parent.inflate(R.layout.item_small_subject, false)
                SmallSubjectViewHolder(inflatedView)
            }
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            val layoutManager = recyclerView.layoutManager
            if (layoutManager is GridLayoutManager) {
                layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (getItemViewType(position) == SubjectService.LEVEL_BIG) {
                            layoutManager.spanCount
                        } else {
                            1
                        }
                    }
                }
            }
        }

        override fun getItemCount(): Int = subjects.size

        override fun getItemViewType(position: Int): Int = subjects[position].level

        override fun onBindViewHolder(holder: AbsSubjectViewHolder, position: Int) {
            val subjectVo = subjects[position]
            Log.i("subjectVo", subjectVo.toString())
            if (subjectVo.level == SubjectService.LEVEL_BIG) {
                val name: TextView = holder.itemView.findViewById(R.id.subject_name)
                name.text = subjectVo.name
            } else {
                val name: TextView = holder.itemView.findViewById(R.id.subject_name)
                name.text = subjectVo.name
                val desc: TextView = holder.itemView.findViewById(R.id.subject_desc)
                desc.text = subjectVo.description
            }
        }
    }

}