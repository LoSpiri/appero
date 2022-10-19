package com.example.apper.screens.add_edit_todo

sealed class AddEditTodoEvent{
    data class OnTitleChange(val title:String):AddEditTodoEvent()
    data class OnDescriptionChange(val description:String):AddEditTodoEvent()
    data class OnDateChange(val date:String):AddEditTodoEvent()
    data class OnTimeChange(val time: String):AddEditTodoEvent()
    data class OnAlarmChange(val alarm: Boolean):AddEditTodoEvent()
    data class OnCalendarSwitchChange(val calendar: Boolean):AddEditTodoEvent()
    object OnSaveTodoClick: AddEditTodoEvent()
}