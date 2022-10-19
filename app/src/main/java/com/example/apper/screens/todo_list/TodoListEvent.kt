package com.example.apper.screens.todo_list

import com.example.apper.data.Todo

sealed class TodoListEvent{
    data class OnDeleteTodoClick(val todo: Todo): TodoListEvent()
    data class OnDoneChange(val todo:Todo,val isDone:Boolean):TodoListEvent()
    data class OnTodoClick(val todo: Todo) : TodoListEvent()
    data class OnPlayRecording(val todo: Todo): TodoListEvent()
    object OnUndoDeleteClick : TodoListEvent()
    object OnAddTodoClick : TodoListEvent()
}