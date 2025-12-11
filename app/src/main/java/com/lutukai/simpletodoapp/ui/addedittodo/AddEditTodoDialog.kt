package com.lutukai.simpletodoapp.ui.addedittodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.databinding.DialogAddEditTodoBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddEditTodoDialog : BottomSheetDialogFragment(), AddEditTodoContract.View {

    enum class Mode { ADD, EDIT }

    private var _binding: DialogAddEditTodoBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var presenter: AddEditTodoPresenter

    private var mode: Mode = Mode.ADD
    private var todoId: Long? = null

    override fun getTheme(): Int = R.style.ThemeOverlay_App_BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEditTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parseArguments()
        setupUI()
        setupClickListeners()

        presenter.attach(this)

        if (mode == Mode.EDIT && todoId != null) {
            presenter.loadTodo(todoId!!)
        }
    }

    private fun parseArguments() {
        arguments?.let { args ->
            val argTodoId = args.getLong(ARG_TODO_ID, NO_TODO_ID)
            if (argTodoId != NO_TODO_ID) {
                todoId = argTodoId
                mode = Mode.EDIT
            }
        }
    }

    private fun setupUI() {
        binding.tvDialogTitle.text = getString(
            if (mode == Mode.ADD) R.string.add_todo_title else R.string.edit_todo_title
        )
        binding.completedToggleContainer.isVisible = mode == Mode.EDIT
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                saveTodo()
            }
        }
    }

    private fun validateInput(): Boolean {
        val title = binding.etTitle.text?.toString().orEmpty().trim()
        if (title.isEmpty()) {
            Snackbar.make(binding.root, R.string.error_title_required, Snackbar.LENGTH_SHORT).show()
            binding.etTitle.requestFocus()
            return false
        }
        return true
    }

    private fun saveTodo() {
        val title = binding.etTitle.text?.toString().orEmpty().trim()
        val description = binding.etDescription.text?.toString().orEmpty().trim()
        val isCompleted = binding.switchCompleted.isChecked

        presenter.saveTodo(title, description, isCompleted, todoId)
    }

    // AddEditTodoContract.View implementation

    override fun showTodo(todo: TodoEntity) {
        binding.etTitle.setText(todo.title)
        binding.etDescription.setText(todo.description)
        binding.switchCompleted.isChecked = todo.isCompleted
    }

    override fun onSaveSuccess() {
        dismiss()
    }

    override fun showLoading() {
        binding.btnSave.isEnabled = false
    }

    override fun hideLoading() {
        binding.btnSave.isEnabled = true
    }

    override fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        presenter.detach()
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddEditTodoDialog"
        const val ARG_TODO_ID = "arg_todo_id"
        const val NO_TODO_ID = -1L
    }
}