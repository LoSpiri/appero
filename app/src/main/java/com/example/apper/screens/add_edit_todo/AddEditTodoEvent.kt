package com.example.apper.screens.add_edit_todo

import java.time.Month

sealed class AddEditTodoEvent{
    data class OnTitleChange(val title:String):AddEditTodoEvent()
    data class OnDescriptionChange(val description:String):AddEditTodoEvent()
    data class OnDateChange(val date:String):AddEditTodoEvent()
    data class OnTimeChange(val time: String):AddEditTodoEvent()
    data class OnAlarmChange(val alarm: Boolean):AddEditTodoEvent()
    data class OnCalendarSwitchChange(val calendar: Boolean):AddEditTodoEvent()
    data class OnDateClick(val year: Int, val month: Int, val day: Int ): AddEditTodoEvent()
    data class OnTimeClick(val hour: Int, val minute: Int): AddEditTodoEvent()
    object OnSaveTodoClick: AddEditTodoEvent()
    object OnStartRecording: AddEditTodoEvent()
    object OnStopRecording: AddEditTodoEvent()
    object OnPlayRecording: AddEditTodoEvent()
}