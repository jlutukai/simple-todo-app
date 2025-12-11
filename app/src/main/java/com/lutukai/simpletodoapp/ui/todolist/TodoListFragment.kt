package com.lutukai.simpletodoapp.ui.todolist

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.data.local.entity.TodoEntity
import com.lutukai.simpletodoapp.databinding.FragmentTodoListBinding
import com.lutukai.simpletodoapp.ui.addedittodo.AddEditTodoDialog
import com.lutukai.simpletodoapp.ui.tododetail.TodoDetailDialog
import com.lutukai.simpletodoapp.util.setEmptyState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TodoListFragment : Fragment(R.layout.fragment_todo_list), TodoListContract.View {

    private var _binding: FragmentTodoListBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var presenter: TodoListPresenter

    private lateinit var adapter: TodoAdapter
    private var allTodos: List<TodoEntity> = emptyList()
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

        presenter.attach(this)
        presenter.loadTodos()
    }

    private fun setupAdapter() {
        adapter = TodoAdapter(object : TodoAdapter.TodoItemListener {
            override fun onToggleComplete(todo: TodoEntity) {
                presenter.toggleComplete(todo)
            }

            override fun onDelete(todo: TodoEntity) {
                presenter.deleteTodo(todo)
            }

            override fun onItemClick(todo: TodoEntity) {
                presenter.openTodoDetail(todo)
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
            presenter.addNewTodo()
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

    private fun updateList(todos: List<TodoEntity>) {
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
        presenter.detach()
        super.onDestroyView()
        _binding = null
    }

    override fun showTodos(todos: List<TodoEntity>) {
        allTodos = todos
        applyFilters()
    }

    override fun showEmpty() {
        allTodos = emptyList()
        updateList(emptyList())
    }

    override fun showTodoDeleted(todo: TodoEntity) {
        Snackbar.make(binding.root, "Task deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                presenter.undoDelete(todo)
            }
            .show()
    }

    override fun navigateToAddTodo() {
        findNavController().navigate(R.id.action_todoList_to_addEditTodo)
    }

    override fun navigateToTodoDetail(todoId: Long) {
        findNavController().navigate(
            R.id.action_todoList_to_todoDetail,
            bundleOf(TodoDetailDialog.ARG_TODO_ID to todoId)
        )
    }

    override fun showLoading() {
        binding.progressBar.isVisible = true
    }

    override fun hideLoading() {
        binding.progressBar.isVisible = false
    }

    override fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}