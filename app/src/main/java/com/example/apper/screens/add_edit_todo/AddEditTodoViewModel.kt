package com.example.apper.screens.add_edit_todo

import android.R
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import com.example.apper.data.Todo
import com.example.apper.data.TodoRepo
import com.example.apper.notifications.NotificationReceiver
import com.example.apper.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

const val EXACT_ALARM_INTENT_REQUEST_CODE = 1001

@HiltViewModel
class AddEditTodoViewModel @Inject constructor(
    @SuppressLint("StaticFieldLeak") @ApplicationContext val application: Context,
    private val repo: TodoRepo,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var todo by mutableStateOf<Todo?>(null)
        private set

    var title by mutableStateOf("")
        private set

    var description by mutableStateOf("")
        private set

    var date by mutableStateOf("")
        private set

    var time by mutableStateOf("")
        private set

    var alarm by mutableStateOf(false)
        private set

    var calendar by mutableStateOf(false)
        private set

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        val todoId = savedStateHandle.get<Int>("todoId")!!
        if(todoId != -1){
            viewModelScope.launch {
                repo.getTodoById(todoId)?.let {
                    title = it.title
                    description = it.description ?: ""
                    date = it.date ?: ""
                    time = it.time ?: ""
                    alarm = it.alarm
                    calendar = it.calendar
                    this@AddEditTodoViewModel.todo = it
                }
            }
        }
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    fun onEvent(event:AddEditTodoEvent){
        when(event){
            is AddEditTodoEvent.OnTitleChange ->{
                title = event.title
            }
            is AddEditTodoEvent.OnDescriptionChange ->{
                description = event.description
            }
            is AddEditTodoEvent.OnDateChange ->{
                date = event.date
            }
            is AddEditTodoEvent.OnTimeChange ->{
                time = event.time
            }
            is AddEditTodoEvent.OnAlarmChange ->{
                alarm = event.alarm
            }
            is AddEditTodoEvent.OnCalendarSwitchChange ->{
                calendar = event.calendar
            }
            is AddEditTodoEvent.OnSaveTodoClick ->{

                viewModelScope.launch {
                    if (title.isBlank()) {
                        sendUiEvent(
                            UiEvent.ShowSnackBar(
                                message = "Title can't be empty"
                            )
                        )
                        return@launch
                    }
                    Log.d("ME", "STO SALVANDO ADESSO")
                    repo.insertTodo(
                        Todo(
                            title = title,
                            description = description,
                            isDone = todo?.isDone ?: false,
                            date = date,
                            time = time,
                            alarm = alarm,
                            calendar = calendar,
                            id = todo?.id
                        )
                    )
                }

                // create calendar instance
                // TODO check input, parseInt() exception handling
                val dateArray = date.split("-").map{it -> it.toInt()}
                val timeArray = time.split("-").map{it -> it.toInt()}
                val calendarInstance = Calendar.getInstance()
                calendarInstance.set(Calendar.YEAR, dateArray[0])
                calendarInstance.set(Calendar.MONTH, dateArray[1])
                calendarInstance.set(Calendar.DAY_OF_MONTH, dateArray[2])
                calendarInstance.set(Calendar.HOUR_OF_DAY, timeArray[0])
                calendarInstance.set(Calendar.MINUTE, timeArray[1])
                calendarInstance.set(Calendar.SECOND, 0)

                viewModelScope.launch {
                    if(alarm) {

                        /*
                        if (!canScheduleExactAlarm(alarmManager!!)) {
                            ContextCompat.startActivity(
                                application,
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM),
                                null
                            )
                        }
                        val intent = Intent(application, MyAlarm::class.java)
                        val pendingIntent = PendingIntent.getBroadcast(
                            application,
                            EXACT_ALARM_INTENT_REQUEST_CODE,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                        }
                        else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                        }

                        */
                        //////////////////////////////////////////////////

                        //if (calendar.time < Date()) calendar.add(Calendar.DAY_OF_MONTH, 1)

                        val alarmManager =
                            ContextCompat.getSystemService(application, AlarmManager::class.java)

                        val intent = Intent(
                            application,
                            NotificationReceiver::class.java
                        )
                        val pendingIntent = PendingIntent.getBroadcast(
                            application,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager!!.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendarInstance.timeInMillis,
                                pendingIntent
                            )
                        }
                        else {
                            alarmManager!!.setExact(
                                AlarmManager.RTC_WAKEUP,
                                calendarInstance.timeInMillis,
                                pendingIntent
                            )
                        }
                    }
                }

                if(calendar) {
                    val startMillis: Long = Calendar.getInstance().run { set(dateArray[0], dateArray[1]-1, dateArray[2], timeArray[0], timeArray[1])
                        timeInMillis
                    }
                    val endMillis: Long = Calendar.getInstance().run {
                        set(dateArray[0], dateArray[1]-1, dateArray[2], timeArray[0]+1, timeArray[1])
                        timeInMillis
                    }
                    val intentCalendar = Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                        .putExtra(CalendarContract.Events.TITLE, title)
                        .putExtra(CalendarContract.Events.DESCRIPTION, description)
                    intentCalendar.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    ContextCompat.startActivity(application, intentCalendar, null)

                    Log.d("ME", "Dovrei aver aggiunto l'evento!");
                    Log.d("DATA", dateArray.toString())
                    Log.d("ORA", timeArray.toString())
                }
                sendUiEvent(UiEvent.PopBackStack)
            }
        }
    }

    private fun sendUiEvent(event: UiEvent){
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

    private fun canScheduleExactAlarm(alarmManager: AlarmManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    class MyAlarm : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("ME", "SONO NELLA ONRECEIVE")

            val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.sym_def_app_icon)
                .setContentTitle("My notification")
                .setContentText("Hello World!")

            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(1, mBuilder.build())
        }
    }
}