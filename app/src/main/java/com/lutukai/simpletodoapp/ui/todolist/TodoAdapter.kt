package com.lutukai.simpletodoapp.ui.todolist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lutukai.simpletodoapp.R
import com.lutukai.simpletodoapp.domain.models.Todo
import com.lutukai.simpletodoapp.databinding.ItemTodoBinding
import com.lutukai.simpletodoapp.util.setDebouncedClickListener

class TodoAdapter(
    private val listener: TodoItemListener
) : ListAdapter<Todo, TodoAdapter.TodoViewHolder>(TodoComp) {

    interface TodoItemListener {
        fun onToggleComplete(todo: Todo)
        fun onDelete(todo: Todo)
        fun onItemClick(todo: Todo)
    }

    private object TodoComp : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            ItemTodoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bindTodoItem(getItem(position))
    }

    class TodoViewHolder(
        private val binding: ItemTodoBinding,
        private val listener: TodoItemListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindTodoItem(item: Todo) {
            with(binding) {
                tvTitle.text = item.title
                cbComplete.isChecked = item.isCompleted

                // Apply strikethrough and color for completed items
                if (item.isCompleted) {
                    tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvTitle.setTextColor(ContextCompat.getColor(root.context, R.color.slate_500))
                } else {
                    tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    tvTitle.setTextColor(ContextCompat.getColor(root.context, R.color.slate_800))
                }

                // Click listeners
                cbComplete.setDebouncedClickListener {
                    listener.onToggleComplete(item)
                }

                btnDelete.setDebouncedClickListener {
                    listener.onDelete(item)
                }

                root.setDebouncedClickListener {
                    listener.onItemClick(item)
                }
            }
        }
    }
}