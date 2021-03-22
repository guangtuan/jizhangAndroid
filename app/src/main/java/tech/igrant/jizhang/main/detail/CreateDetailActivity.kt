package tech.igrant.jizhang.main.detail

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import tech.igrant.jizhang.databinding.ActivityCreateBinding
import tech.igrant.jizhang.databinding.ItemSelectBinding
import tech.igrant.jizhang.framework.ext.toDate
import tech.igrant.jizhang.framework.ext.toLocalDateTime
import tech.igrant.jizhang.login.TokenManager
import tech.igrant.jizhang.main.account.AccountService
import tech.igrant.jizhang.main.subject.SubjectService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CreateDetailActivity : AppCompatActivity() {

    companion object {
        val REQUEST_CODE = 1

        fun startForResult(activity: Activity) {
            activity.startActivityForResult(
                Intent(activity, CreateDetailActivity::class.java),
                REQUEST_CODE
            )
        }
    }

    private lateinit var binding: ActivityCreateBinding

    private var detailTo = DetailService.DetailTo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        TokenManager.get()?.let {
            detailTo.userId = it.userId
        }
        setContentView(binding.root)
        SubjectService.loadSubject()
            .map {
                it.map { sub ->
                    sub.children.map { child ->
                        IdName(
                            child.id,
                            child.name
                        )
                    }
                }.flatten()
            }
            .subscribe {
                val idNameAdapter = IdNameAdapter(it)
                binding.createDetailSubjectInput.adapter = idNameAdapter
                binding.createDetailSubjectInput.onItemSelectedListener =
                    getOnItemSelectedListener { id -> detailTo.subjectId = id }
            }
        AccountService.loadAccount()
            .map {
                it.map { acc -> IdName(acc.id, acc.name) }
            }
            .subscribe {
                IdNameAdapter(it).apply {
                    binding.createDetailSourceAccountInput.adapter = this
                    binding.createDetailSourceAccountInput.onItemSelectedListener =
                        getOnItemSelectedListener { id -> detailTo.sourceAccountId = id }
                }
                IdNameAdapter(it).apply {
                    binding.createDetailDestAccountInput.adapter = this
                    binding.createDetailDestAccountInput.onItemSelectedListener =
                        getOnItemSelectedListener { id -> detailTo.destAccountId = id }
                }
            }
        bindDate()
        binding.createDetailDateInput.setOnClickListener {
            detailTo.createdAt.toLocalDateTime().apply {
                DatePickerDialog(
                    this@CreateDetailActivity,
                    DatePickerDialog.OnDateSetListener { _, year, month, day ->
                        detailTo.createdAt =
                            LocalDateTime.of(year, month + 1, day, 0, 0, 1).toDate()
                        bindDate()
                    },
                    this.year,
                    this.monthValue - 1,
                    this.dayOfMonth
                ).show()
            }
        }
        binding.createDetailSaveButton.setOnClickListener {
            detailTo.amount = binding.createDetailAmountInput.text.toString().toInt() * 100
            detailTo.remark = binding.createDetailRemarkInput.text.toString()
            Log.i("create", detailTo.toString())
            DetailService.create(detailTo)
                .subscribe {
                    Toast.makeText(this, "create success", Toast.LENGTH_LONG).show()
                    this.setResult(Activity.RESULT_OK)
                    this.finish()
                }
        }
    }

    private fun bindDate() {
        binding.createDetailDateInput.text =
            detailTo.createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    private fun getOnItemSelectedListener(after: (id: Long) -> Unit): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val adapter = parent?.adapter
                if (adapter is IdNameAdapter) {
                    val item = adapter.getItem(position)
                    after(item.id)
                }
            }
        }
    }

    data class IdName(val id: Long, val name: String)

    class IdNameViewHolder(val view: View, val itemSelectBinding: ItemSelectBinding)

    class IdNameAdapter(private val idNames: List<IdName>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val idNameViewHolder: IdNameViewHolder
            if (convertView == null) {
                val binding = ItemSelectBinding.inflate(
                    LayoutInflater.from(parent?.context),
                    parent,
                    false
                )
                idNameViewHolder = IdNameViewHolder(binding.root, binding)
                binding.root.tag = idNameViewHolder
            } else {
                idNameViewHolder = convertView.tag as IdNameViewHolder
            }
            idNameViewHolder.itemSelectBinding.itemSelectText.text = getItem(position).name
            return idNameViewHolder.view
        }

        override fun getItem(position: Int): IdName = idNames[position]

        override fun getItemId(position: Int): Long = idNames[position].id

        override fun getCount() = idNames.size
    }

}