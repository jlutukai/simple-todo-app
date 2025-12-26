package com.lutukai.simpletodoapp.ui.todolist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lutukai.simpletodoapp.ui.util.TestTags
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.ui.mvi.ObserveAsEvents
import com.lutukai.simpletodoapp.ui.mvi.collectState
import com.lutukai.simpletodoapp.ui.mvi.rememberOnIntent
import com.lutukai.simpletodoapp.ui.preview.DevicePreviews
import com.lutukai.simpletodoapp.ui.theme.SimpleTodoAppTheme

@Composable
fun TodoListScreen(
    onNavigateToAddTodo: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: TodoListMviViewModel = hiltViewModel()
) {
    val state = viewModel.collectState()
    val onIntent = viewModel.rememberOnIntent()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(flow = viewModel.effect) { effect ->
        when (effect) {
            is TodoListSideEffect.NavigateToAddTodo -> onNavigateToAddTodo()
            is TodoListSideEffect.NavigateToDetail -> onNavigateToDetail(effect.todoId)
            is TodoListSideEffect.ShowSnackbar -> {
                val result = snackbarHostState.showSnackbar(
                    message = effect.message,
                    actionLabel = effect.actionLabel,
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed && effect.todo != null) {
                    viewModel.onIntent(TodoListIntent.UndoDelete(effect.todo))
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(TodoListIntent.AddNewTodo) },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag(TestTags.FAB_ADD_TODO)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_task),
                    tint = Color.White
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        TodoListContent(
            state = state,
            onIntent = onIntent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
internal fun TodoListContent(
    state: TodoListState,
    onIntent: (TodoListIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.todo_list_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { /* More options */ }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.cd_more_options)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { onIntent(TodoListIntent.UpdateSearchQuery(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Tabs
        FilterTabs(
            selectedFilter = state.filter,
            onFilterSelected = { onIntent(TodoListIntent.UpdateFilter(it)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(TestTags.LOADING_INDICATOR),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.filteredTodos.isEmpty() -> {
                EmptyState(modifier = Modifier.testTag(TestTags.EMPTY_STATE))
            }
            else -> {
                TodoList(
                    todos = state.filteredTodos,
                    onToggleComplete = { todo -> onIntent(TodoListIntent.ToggleComplete(todo)) },
                    onDelete = { todo -> onIntent(TodoListIntent.DeleteTodo(todo)) },
                    onItemClick = { todo ->
                        todo.id?.let { onIntent(TodoListIntent.OpenTodoDetail(it)) }
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .testTag(TestTags.SEARCH_BAR),
        placeholder = {
            Text(
                stringResource(R.string.search_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = stringResource(R.string.cd_search),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun FilterTabs(
    selectedFilter: TodoListState.TodoFilter,
    onFilterSelected: (TodoListState.TodoFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        TodoListState.TodoFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surface
                        else Color.Transparent
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(vertical = 10.dp)
                    .testTag(
                        when (filter) {
                            TodoListState.TodoFilter.ALL -> TestTags.TAB_ALL
                            TodoListState.TodoFilter.COMPLETED -> TestTags.TAB_COMPLETED
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (filter) {
                        TodoListState.TodoFilter.ALL -> stringResource(R.string.tab_all)
                        TodoListState.TodoFilter.COMPLETED -> stringResource(R.string.tab_completed)
                    },
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TodoList(
    todos: List<Todo>,
    onToggleComplete: (Todo) -> Unit,
    onDelete: (Todo) -> Unit,
    onItemClick: (Todo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.testTag(TestTags.TODO_LIST),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(
            items = todos,
            key = { it.id ?: it.hashCode() }
        ) { todo ->
            TodoItem(
                todo = todo,
                onToggleComplete = onToggleComplete,
                onDelete = onDelete,
                onClick = onItemClick
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.empty_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.empty_subtitle),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@DevicePreviews
@Composable
private fun TodoListContentPreview() {
    val todos = listOf(
        Todo(
            id = 1,
            title = "Buy groceries",
            description = "Milk, eggs, bread",
            isCompleted = false,
            completedAt = null,
            createdAt = System.currentTimeMillis()
        ),
        Todo(
            id = 2,
            title = "Finish project report",
            description = "Complete the quarterly report",
            isCompleted = true,
            completedAt = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis() - 86400000
        ),
        Todo(
            id = 3,
            title = "Call mom",
            description = "",
            isCompleted = false,
            completedAt = null,
            createdAt = System.currentTimeMillis() - 3600000
        )
    )
    SimpleTodoAppTheme(dynamicColor = false) {
        TodoListContent(
            state = TodoListState(
                todos = todos,
                filteredTodos = todos,
                isLoading = false
            ),
            onIntent = {}
        )
    }
}

@DevicePreviews
@Composable
private fun TodoListContentEmptyPreview() {
    SimpleTodoAppTheme(dynamicColor = false) {
        TodoListContent(
            state = TodoListState(
                todos = emptyList(),
                isLoading = false
            ),
            onIntent = {}
        )
    }
}

@DevicePreviews
@Composable
private fun TodoListContentLoadingPreview() {
    SimpleTodoAppTheme(dynamicColor = false) {
        TodoListContent(
            state = TodoListState(
                isLoading = true
            ),
            onIntent = {}
        )
    }
}
