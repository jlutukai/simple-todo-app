package com.lutukai.simpletodoapp.ui.util

/**
 * Centralized test tags for Compose UI testing.
 * Using an object ensures consistency between production code and tests.
 */
object TestTags {
    // TodoListScreen
    const val FAB_ADD_TODO = "fab_add_todo"
    const val SEARCH_BAR = "search_bar"
    const val TAB_ALL = "tab_all"
    const val TAB_COMPLETED = "tab_completed"
    const val TODO_LIST = "todo_list"
    const val LOADING_INDICATOR = "loading_indicator"
    const val EMPTY_STATE = "empty_state"

    // TodoItem - dynamic tags
    fun todoItem(id: Long) = "todo_item_$id"
    fun todoCheckbox(id: Long) = "todo_checkbox_$id"
    fun todoDeleteButton(id: Long) = "todo_delete_$id"

    // AddEditTodoScreen
    const val ADD_EDIT_TITLE_FIELD = "add_edit_title"
    const val ADD_EDIT_DESCRIPTION_FIELD = "add_edit_description"
    const val ADD_EDIT_COMPLETED_SWITCH = "add_edit_completed_switch"
    const val ADD_EDIT_SAVE_BUTTON = "add_edit_save"
    const val ADD_EDIT_CANCEL_BUTTON = "add_edit_cancel"
    const val ADD_EDIT_LOADING = "add_edit_loading"

    // TodoDetailScreen
    const val DETAIL_EDIT_BUTTON = "detail_edit"
    const val DETAIL_CLOSE_BUTTON = "detail_close"
    const val DETAIL_TITLE = "detail_title"
    const val DETAIL_COMPLETED_SWITCH = "detail_completed_switch"
    const val DETAIL_NOTES = "detail_notes"
    const val DETAIL_CREATED_DATE = "detail_created_date"
    const val DETAIL_COMPLETED_DATE = "detail_completed_date"
    const val DETAIL_LOADING = "detail_loading"
}
