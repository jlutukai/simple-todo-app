package com.lutukai.simpletodoapp.ui.todolist

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.databinding.FragmentTodoListBinding
import com.lutukai.simpletodoapp.ui.tododetail.TodoDetailDialog
import com.lutukai.simpletodoapp.util.setEmptyState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TodoListFragment : Fragment(R.layout.fragment_todo_list) {

    private var _binding: FragmentTodoListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TodoListViewModel by viewModels()

    private lateinit var adapter: TodoAdapter
    private var allTodos: List<Todo> = emptyList()
    private var currentFilter: TodoFilter = TodoFilter.ALL
    private var searchQuery: String = ""

    enum class TodoFilter {
        ALL, COMPLETED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTodoListBinding.bind(view)

        setupAdapter()
        setupTabs()
        setupSearch()
        setupFab()
        observeViewModel()
    }

    private fun setupAdapter() {
        adapter = TodoAdapter(object : TodoAdapter.TodoItemListener {
            override fun onToggleComplete(todo: Todo) {
                viewModel.toggleComplete(todo)
            }

            override fun onDelete(todo: Todo) {
                viewModel.deleteTodo(todo)
            }

            override fun onItemClick(todo: Todo) {
                viewModel.openTodoDetail(todo)
            }
        })
        binding.rvTodos.adapter = adapter
    }

    private fun setupTabs() {
        // Set initial state - "All" tab selected
        selectTab(TodoFilter.ALL, animate = false)

        binding.tabAll.setOnClickListener {
            if (currentFilter != TodoFilter.ALL) {
                selectTab(TodoFilter.ALL, animate = true)
                applyFilters()
            }
        }

        binding.tabCompleted.setOnClickListener {
            if (currentFilter != TodoFilter.COMPLETED) {
                selectTab(TodoFilter.COMPLETED, animate = true)
                applyFilters()
            }
        }
    }

    private fun selectTab(filter: TodoFilter, animate: Boolean) {
        currentFilter = filter
        val targetView = if (filter == TodoFilter.ALL) binding.tabAll else binding.tabCompleted

        if (animate) {
            TransitionManager.beginDelayedTransition(binding.tabsContainer)
        }

        // Update selector position
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.tabsContainer)
        constraintSet.connect(R.id.tabSelector, ConstraintSet.START, targetView.id, ConstraintSet.START)
        constraintSet.connect(R.id.tabSelector, ConstraintSet.END, targetView.id, ConstraintSet.END)
        constraintSet.applyTo(binding.tabsContainer)

        // Update text colors
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.slate_900)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.slate_600)

        binding.tabAll.setTextColor(if (filter == TodoFilter.ALL) selectedColor else unselectedColor)
        binding.tabCompleted.setTextColor(if (filter == TodoFilter.COMPLETED) selectedColor else unselectedColor)
    }

    private fun setupSearch() {
        binding.etSearch.doAfterTextChanged { text ->
            searchQuery = text?.toString().orEmpty()
            applyFilters()
        }
    }

    private fun setupFab() {
        binding.fabAddTodo.setOnClickListener {
            viewModel.addNewTodo()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressBar.isVisible = state.isLoading

                        if (state.error != null) {
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_SHORT).show()
                        }

                        allTodos = state.todos
                        applyFilters()
                    }
                }

                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is TodoListEvent.NavigateToAddTodo -> {
                                findNavController().navigate(R.id.action_todoList_to_addEditTodo)
                            }
                            is TodoListEvent.NavigateToDetail -> {
                                findNavController().navigate(
                                    R.id.action_todoList_to_todoDetail,
                                    bundleOf(TodoDetailDialog.ARG_TODO_ID to event.todoId)
                                )
                            }
                            is TodoListEvent.ShowSnackbar -> {
                                val snackbar = Snackbar.make(
                                    binding.root,
                                    event.message,
                                    Snackbar.LENGTH_LONG
                                )
                                if (event.actionLabel != null && event.action != null) {
                                    snackbar.setAction(event.actionLabel) { event.action.invoke() }
                                }
                                snackbar.show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun applyFilters() {
        var filtered = allTodos

        // Apply tab filter
        if (currentFilter == TodoFilter.COMPLETED) {
            filtered = filtered.filter { it.isCompleted }
        }

        // Apply search filter
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }

        updateList(filtered)
    }

    private fun updateList(todos: List<Todo>) {
        adapter.submitList(todos)

        val isEmpty = todos.isEmpty()
        binding.rvTodos.isVisible = !isEmpty
        binding.emptyStateStub.setEmptyState(
            dataIsEmpty = isEmpty,
            title = getString(R.string.empty_title),
            desc = getString(R.string.empty_subtitle)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}