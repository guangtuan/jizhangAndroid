package tech.igrant.jizhang.main.detail

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tech.igrant.jizhang.R
import tech.igrant.jizhang.databinding.ActivityCreateBinding
import tech.igrant.jizhang.framework.Serialization
import tech.igrant.jizhang.framework.ext.format
import tech.igrant.jizhang.framework.ext.toDate
import tech.igrant.jizhang.framework.ext.toLocalDateTime
import tech.igrant.jizhang.framework.ext.uuid
import tech.igrant.jizhang.login.TokenManager
import tech.igrant.jizhang.main.account.AccountService
import tech.igrant.jizhang.main.subject.SubjectService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class CreateDetailActivity : AppCompatActivity() {

    companion object {
        const val MODE_EDIT = 1
        const val MODE_CREATE = 2

        const val KEY_EDITING = "editing"
        const val KEY_MODE = "keyMode"
        const val KEY_DATE = "keyDate"

        const val COMMON_REQUEST_CODE = 1

        fun startAsCreateMode(activity: Activity, date: Long) {
            Intent(activity, CreateDetailActivity::class.java).also {
                it.putExtra(KEY_MODE, MODE_CREATE)
                it.putExtra(KEY_DATE, date)
                activity.startActivityForResult(
                    it,
                    COMMON_REQUEST_CODE
                )
            }
        }

        fun startAsCreateMode(
            activity: Activity,
            detailDisplay: DetailService.DetailTransferObject.Local
        ) {
            Intent(activity, CreateDetailActivity::class.java).also {
                it.putExtra(KEY_MODE, MODE_CREATE)
                it.putExtra(KEY_EDITING, Serialization.toJson(detailDisplay))
                activity.startActivityForResult(
                    it,
                    COMMON_REQUEST_CODE
                )
            }
        }

        fun startAsEditMode(
            activity: Activity,
            detailDisplay: DetailService.DetailTransferObject.Local
        ) {
            Intent(activity, CreateDetailActivity::class.java).also {
                it.putExtra(KEY_MODE, MODE_EDIT)
                it.putExtra(KEY_EDITING, Serialization.toJson(detailDisplay))
                activity.startActivityForResult(
                    it,
                    COMMON_REQUEST_CODE
                )
            }
        }

    }

    private lateinit var binding: ActivityCreateBinding

    private var detail = DetailService.DetailTransferObject.Local(
        localId = uuid(), remoteId = -1
    )

    private var mode: Int = MODE_CREATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        TokenManager.get()?.let {
            detail.userId = it.userId
        }
        intent?.let { intent ->
            intent.extras?.getInt(KEY_MODE)?.let {
                mode = it
            }
            intent.extras?.getString(KEY_EDITING)?.let { detailToStr ->
                detail = Serialization.fromJson(
                    detailToStr,
                    DetailService.DetailTransferObject.Local::class.java
                )
            }
            intent.extras?.getLong(KEY_DATE)?.let {
                if (0L != it) {
                    detail.createdAt = Date(it)
                }
            }
        }
        when (mode) {
            MODE_CREATE -> {
                binding.titleBar.leftTextView.setText(R.string.titleCreate)
            }
            MODE_EDIT -> {
                binding.titleBar.leftTextView.setText(R.string.titleEdit)
            }
        }
        setContentView(binding.root)
        if (detail.amount != 0) {
            binding.createDetailAmountInput.setText((detail.amount.toDouble() / 100).format(2))
        }
        detail.remark?.let {
            binding.createDetailRemarkInput.setText(it)
        }
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
                if (detail.subjectId != -1L) {
                    binding.createDetailSubjectInput.text =
                        idNames.find { it.id == detail.subjectId }?.name
                }
                binding.createDetailSubjectInput.setOnClickListener {
                    SelectorDialog.show(
                        activity = this,
                        idNames = idNames,
                        onItemSelect = { idName ->
                            binding.createDetailSubjectInput.text = idName.name
                            detail.subjectId = idName.id
                        }
                    )
                }
            }
        AccountService.loadAccount()
            .map {
                it.map { acc -> SelectorDialog.IdName(acc.id, acc.name) }
            }
            .subscribe { idNames ->
                detail.sourceAccountId?.let {
                    binding.createDetailSourceAccountInput.text =
                        idNames.find { idName -> idName.id == it }?.name
                }
                binding.createDetailSourceAccountInput.setOnClickListener {
                    SelectorDialog.show(
                        activity = this,
                        idNames = idNames,
                        onItemSelect = { idName ->
                            binding.createDetailSourceAccountInput.text = idName.name
                            detail.sourceAccountId = idName.id
                        }
                    )
                }
                detail.destAccountId?.let {
                    binding.createDetailDestAccountInput.text =
                        idNames.find { idName -> idName.id == it }?.name
                }
                binding.createDetailDestAccountInput.setOnClickListener {
                    SelectorDialog.show(
                        activity = this,
                        idNames = idNames,
                        onItemSelect = { idName ->
                            binding.createDetailDestAccountInput.text = idName.name
                            detail.destAccountId = idName.id
                        }
                    )
                }
            }
        bindDate()
        binding.createDetailDateInput.setOnClickListener {
            detail.createdAt.toLocalDateTime().apply {
                DatePickerDialog(
                    this@CreateDetailActivity,
                    { _, year, month, day ->
                        detail.createdAt =
                            LocalDateTime.of(year, month + 1, day, 0, 0, 1).toDate()
                        bindDate()
                    },
                    this.year,
                    this.monthValue - 1,
                    this.dayOfMonth
                ).show()
            }
        }
        binding.titleBar.rightTextView.setOnClickListener {
            val amountText = binding.createDetailAmountInput.text.toString()
            if (amountText.isEmpty()) {
                Toast.makeText(this, "请输入金额", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            detail.amount = (amountText.toDouble() * 100).toInt()
            detail.remark = binding.createDetailRemarkInput.text.toString()
            Log.i("create", detail.toString())

            if (detail.amount == 0) {
                Toast.makeText(this, "请输入金额", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (detail.destAccountId == null && detail.sourceAccountId == null) {
                Toast.makeText(this, "请选择来源账户或者目标账户", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (detail.subjectId == -1L) {
                Toast.makeText(this, "请选择科目", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (mode == MODE_CREATE) {
                DetailService.create(detail)
                    .subscribe {
                        Toast.makeText(this, "create success", Toast.LENGTH_LONG).show()
                        this.setResult(Activity.RESULT_OK)
                        this.finish()
                    }
            } else {
                DetailService.update(detail)
                    .subscribe {
                        Toast.makeText(this, "update success", Toast.LENGTH_LONG).show()
                        this.setResult(Activity.RESULT_OK)
                        this.finish()
                    }
            }
        }
    }

    private fun bindDate() {
        binding.createDetailDateInput.text =
            detail.createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

}