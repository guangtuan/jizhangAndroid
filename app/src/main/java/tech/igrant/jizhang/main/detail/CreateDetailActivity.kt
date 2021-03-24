package tech.igrant.jizhang.main.detail

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tech.igrant.jizhang.databinding.ActivityCreateBinding
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
                        SelectorDialog.IdName(
                            child.id,
                            child.name
                        )
                    }
                }.flatten()
            }
            .subscribe { idNames ->
                binding.createDetailSubjectInput.setOnClickListener {
                    SelectorDialog.show(
                        activity = this,
                        idNames = idNames,
                        onItemSelect = { idName ->
                            binding.createDetailSubjectInput.text = idName.name
                            detailTo.subjectId = idName.id
                        }
                    )
                }
            }
        AccountService.loadAccount()
            .map {
                it.map { acc -> SelectorDialog.IdName(acc.id, acc.name) }
            }
            .subscribe { idNames ->
                binding.createDetailSourceAccountInput.setOnClickListener {
                    SelectorDialog.show(
                        activity = this,
                        idNames = idNames,
                        onItemSelect = { idName ->
                            binding.createDetailSourceAccountInput.text = idName.name
                            detailTo.sourceAccountId = idName.id
                        }
                    )
                }
                binding.createDetailDestAccountInput.setOnClickListener {
                    SelectorDialog.show(
                        activity = this,
                        idNames = idNames,
                        onItemSelect = { idName ->
                            binding.createDetailDestAccountInput.text = idName.name
                            detailTo.destAccountId = idName.id
                        }
                    )
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
            val amountText = binding.createDetailAmountInput.text.toString()
            if (amountText.isEmpty()) {
                Toast.makeText(this, "请输入金额", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            detailTo.amount = (amountText.toDouble() * 100).toInt()
            detailTo.remark = binding.createDetailRemarkInput.text.toString()
            Log.i("create", detailTo.toString())

            if (detailTo.amount == 0) {
                Toast.makeText(this, "请输入金额", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (detailTo.destAccountId == null && detailTo.sourceAccountId == null) {
                Toast.makeText(this, "请选择来源账户或者目标账户", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (detailTo.subjectId == -1L) {
                Toast.makeText(this, "请选择科目", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

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

}