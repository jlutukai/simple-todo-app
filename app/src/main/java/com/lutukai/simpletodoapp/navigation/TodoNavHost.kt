package com.lutukai.simpletodoapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.lutukai.simpletodoapp.ui.features.addedittodo.AddEditTodoScreen
import com.lutukai.simpletodoapp.ui.features.tododetail.TodoDetailScreen
import com.lutukai.simpletodoapp.ui.features.todolist.TodoListScreen

@Composable
fun TodoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.TodoList,
        modifier = modifier
    ) {
        composable<NavRoute.TodoList> {
            TodoListScreen(
                onNavigateToAddTodo = {
                    navController.navigate(NavRoute.AddEditTodo())
                },
                onNavigateToDetail = { todoId ->
                    navController.navigate(NavRoute.TodoDetail(todoId))
                }
            )
        }

        dialog<NavRoute.TodoDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.TodoDetail>()
            TodoDetailScreen(
                todoId = route.todoId,
                onNavigateToEdit = { todoId ->
                    navController.navigate(NavRoute.AddEditTodo(todoId)) {
                        popUpTo<NavRoute.TodoDetail> { inclusive = true }
                    }
                },
                onDismiss = { navController.popBackStack() }
            )
        }

        dialog<NavRoute.AddEditTodo> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.AddEditTodo>()
            AddEditTodoScreen(
                todoId = route.todoId,
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
