package com.lutukai.simpletodoapp.ui.tododetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.databinding.DialogTodoDetailBinding
import com.lutukai.simpletodoapp.ui.addedittodo.AddEditTodoDialog
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class TodoDetailDialog : BottomSheetDialogFragment(), TodoDetailContract.View {

    private var _binding: DialogTodoDetailBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var presenter: TodoDetailPresenter

    private var todoId: Long = NO_TODO_ID
    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    override fun getTheme(): Int = R.style.ThemeOverlay_App_BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTodoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parseArguments()
        setupClickListeners()

        presenter.attach(this)

        if (todoId != NO_TODO_ID) {
            presenter.loadTodo(todoId)
        } else {
            showError("Todo not found")
            dismiss()
        }
    }

    private fun parseArguments() {
        arguments?.let { args ->
            todoId = args.getLong(ARG_TODO_ID, NO_TODO_ID)
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnEdit.setOnClickListener {
            presenter.onEditClicked()
        }

        binding.switchCompleted.setOnCheckedChangeListener { _, isChecked ->
            presenter.toggleComplete(isChecked)
        }
    }

    override fun showTodo(todo: TodoEntity) {
        binding.tvTitle.text = todo.title
        binding.tvDescription.text = todo.description.ifEmpty { "-" }

        // Temporarily remove listener to avoid triggering update
        binding.switchCompleted.setOnCheckedChangeListener(null)
        binding.switchCompleted.isChecked = todo.isCompleted
        binding.switchCompleted.setOnCheckedChangeListener { _, isChecked ->
            presenter.toggleComplete(isChecked)
        }

        // Format and display dates
        binding.tvCreatedAt.text = formatDate(todo.createdAt)

        // Show completed date only if completed
        binding.completedOnContainer.isVisible = todo.isCompleted && todo.completedAt != null
        todo.completedAt?.let { completedAt ->
            binding.tvCompletedAt.text = formatDate(completedAt)
        }
    }

    private fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    override fun navigateToEdit(todoId: Long) {
        dismiss()
        findNavController().navigate(
            R.id.action_todoDetail_to_addEditTodo,
            bundleOf(AddEditTodoDialog.ARG_TODO_ID to todoId)
        )
    }

    override fun closeDialog() {
        dismiss()
    }

    override fun showLoading() {
        // Optional: show loading state
    }

    override fun hideLoading() {
        // Optional: hide loading state
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
        const val TAG = "TodoDetailDialog"
        const val ARG_TODO_ID = "arg_todo_id"
        const val NO_TODO_ID = -1L
    }
}
