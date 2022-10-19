package com.example.apper.screens.todo_list

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apper.data.Todo
import com.example.apper.data.TodoRepo
import com.example.apper.util.Routes
import com.example.apper.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoLIstViewModel @Inject constructor(private val repo: TodoRepo) : ViewModel() {

    val todos = repo.getTodos()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var deletedTodo: Todo? = null

    fun onEvent(event: TodoListEvent){
        when(event){
            is TodoListEvent.OnTodoClick ->{
                sendUiEvent(UiEvent.Navigate(Routes.ADD_EDIT_TODO + "?todoId=${event.todo.id}"))
            }
            is TodoListEvent.OnAddTodoClick ->{
                sendUiEvent(UiEvent.Navigate(Routes.ADD_EDIT_TODO))
            }
            is TodoListEvent.OnDeleteTodoClick ->{
                viewModelScope.launch {
                    deletedTodo = event.todo
                    repo.deleteTodo(event.todo)
                    sendUiEvent(UiEvent.ShowSnackBar(
                        message = "Todo deleted",
                        action = "Undo"
                    ))
                }
            }
            is TodoListEvent.OnUndoDeleteClick ->{
                deletedTodo?.let {todo ->
                    viewModelScope.launch {
                        repo.insertTodo(todo)
                    }
                }
            }
            is TodoListEvent.OnDoneChange ->{
                viewModelScope.launch {
                    repo.insertTodo(event.todo.copy(
                        isDone = event.isDone
                    ))
                }
            }
            is TodoListEvent.OnPlayRecording ->{
                Log.d("TODOLIST","PLAY entered")
                val mediaPlayer = MediaPlayer()
                try {
                    mediaPlayer.setDataSource(event.todo.recordingPath)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                }
                catch (e:Exception) {
                    Log.d("TODOLIST","Problem finding audioFile")
                }
            }
        }
    }

    private fun sendUiEvent(event: UiEvent){
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}