package com.example.apper.screens.todo_list

import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apper.data.Todo

@Composable
fun TodoItem(todo: Todo, onEvent:(TodoListEvent) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ){
        Column(
            verticalArrangement = Arrangement.Center,
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ){
                Text(
                    text = todo.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        onEvent(TodoListEvent.OnDeleteTodoClick(todo))
                    }
                ){
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                todo.description?.let {
                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f)
                    )
                }
                Checkbox(
                    checked = todo.isDone,
                    onCheckedChange = { isChecked ->
                        onEvent(TodoListEvent.OnDoneChange(todo,isChecked))
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                todo.date?.let {
                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                todo.time?.let {
                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}