package com.lutukai.simpletodoapp.ui.tododetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.databinding.DialogTodoDetailBinding
import com.lutukai.simpletodoapp.ui.addedittodo.AddEditTodoDialog
import com.lutukai.simpletodoapp.util.setDebouncedClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class TodoDetailDialog : BottomSheetDialogFragment() {

    private var _binding: DialogTodoDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TodoDetailViewModel by viewModels()

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
        observeViewModel()

        if (todoId != NO_TODO_ID) {
            viewModel.loadTodo(todoId)
        } else {
            Snackbar.make(binding.root, "Todo not found", Snackbar.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun parseArguments() {
        arguments?.let { args ->
            todoId = args.getLong(ARG_TODO_ID, NO_TODO_ID)
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setDebouncedClickListener {
            dismiss()
        }

        binding.btnEdit.setDebouncedClickListener {
            viewModel.onEditClicked()
        }

        binding.switchCompleted.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleComplete(isChecked)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        state.todo?.let { todo ->
                            showTodo(todo)
                        }
                    }
                }

                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is TodoDetailEvent.NavigateToEdit -> {
                                findNavController().navigate(
                                    R.id.action_todoDetail_to_addEditTodo,
                                    bundleOf(AddEditTodoDialog.ARG_TODO_ID to event.todoId)
                                )
//                                dismiss()
                            }
                            is TodoDetailEvent.ShowError -> {
                                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showTodo(todo: Todo) {
        binding.tvTitle.text = todo.title
        binding.tvDescription.text = todo.description.ifEmpty { "-" }

        // Temporarily remove listener to avoid triggering update
        binding.switchCompleted.setOnCheckedChangeListener(null)
        binding.switchCompleted.isChecked = todo.isCompleted
        binding.switchCompleted.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleComplete(isChecked)
        }

        // Format and display dates
        binding.tvCreatedAt.text = todo.createdAt?.let { formatDate(it) } ?: "-"

        // Show completed date only if completed
        binding.completedOnContainer.isVisible = todo.isCompleted && todo.completedAt != null
        todo.completedAt?.let { completedAt ->
            binding.tvCompletedAt.text = formatDate(completedAt)
        }
    }

    private fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TodoDetailDialog"
        const val ARG_TODO_ID = "arg_todo_id"
        const val NO_TODO_ID = -1L
    }
}
