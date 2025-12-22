package com.lutukai.simpletodoapp.ui.addedittodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.databinding.DialogAddEditTodoBinding
import com.lutukai.simpletodoapp.util.setDebouncedClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditTodoDialog : BottomSheetDialogFragment() {

    enum class Mode { ADD, EDIT }

    private var _binding: DialogAddEditTodoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditTodoViewModel by viewModels()

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
        observeViewModel()

        if (mode == Mode.EDIT && todoId != null) {
            viewModel.loadTodo(todoId!!)
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
        binding.btnCancel.setDebouncedClickListener {
            dismiss()
        }

        binding.btnSave.setDebouncedClickListener {
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

        viewModel.saveTodo(title, description, isCompleted, todoId)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.btnSave.isEnabled = !state.isLoading

                        state.todo?.let { todo ->
                            binding.etTitle.setText(todo.title)
                            binding.etDescription.setText(todo.description)
                            binding.switchCompleted.isChecked = todo.isCompleted
                        }
                    }
                }

                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is AddEditTodoEvent.SaveSuccess -> dismiss()
                            is AddEditTodoEvent.ShowError -> {
                                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddEditTodoDialog"
        const val ARG_TODO_ID = "arg_todo_id"
        const val NO_TODO_ID = -1L
    }
}