package com.example.apper.screens.add_edit_todo

import android.R
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.provider.CalendarContract
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    var recordingPath by mutableStateOf("")
        private set

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // TODO Uno é deprecato, l'altro vuole API 31 xd
    private val mediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(application)
        } else {
            MediaRecorder()
        }

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
                    recordingPath = it.recordingPath ?: ""
                    this@AddEditTodoViewModel.todo = it
                }
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag", "RestrictedApi")
    fun onEvent(event:AddEditTodoEvent){
        when(event){
            is AddEditTodoEvent.OnTitleChange ->{
                title = event.title
            }
            is AddEditTodoEvent.OnDescriptionChange ->{
                description = event.description
            }
            /*
            is AddEditTodoEvent.OnDateChange ->{
                date = event.date
            }
            */
            is AddEditTodoEvent.OnDateClick ->{
                date = "${event.year}-${event.month}-${event.day}"
            }
            is AddEditTodoEvent.OnTimeClick ->{
                time = "${event.hour}-${event.minute}"
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
            is AddEditTodoEvent.OnStartRecording ->{
                Log.d("START","Entered")
                if(recordingPath == ""){
                    val filePath = application.filesDir.absolutePath
                    val fileName = "$filePath/${"abcdefghilmnopqrstuvz".toMutableList().shuffled().joinToString("")}.3gp"
                    recordingPath = fileName
                }
                Log.d("START", recordingPath)
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                mediaRecorder.setOutputFile(recordingPath)
                mediaRecorder.prepare()
                mediaRecorder.start()
            }
            is AddEditTodoEvent.OnStopRecording ->{
                Log.d("STOP","Entered")
                mediaRecorder.stop()
                mediaRecorder.reset()
                //mediaRecorder = null
                Log.d("STOP",recordingPath)
            }
            is AddEditTodoEvent.OnPlayRecording ->{
                Log.d("PLAY","Entered")
                val mediaPlayer = MediaPlayer()
                try {
                    mediaPlayer.setDataSource(recordingPath)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                }
                catch (e:Exception) {
                    Log.d("AHIA","Problem finding audioFile")
                }
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
                            recordingPath = recordingPath,
                            id = todo?.id
                        )
                    )
                    sendUiEvent(UiEvent.PopBackStack)
                }

                val calendarInstance = Calendar.getInstance()
                var dateArray: List<Int> = mutableListOf()
                var timeArray: List<Int> = mutableListOf()
                if(date != "" && time != ""){
                    // create calendar instance
                    // TODO check input, parseInt() exception handling
                    dateArray = date.split("-").map{it.toInt()}
                    timeArray = time.split("-").map{it.toInt()}
                    calendarInstance.set(Calendar.YEAR, dateArray[0])
                    calendarInstance.set(Calendar.MONTH, dateArray[1])
                    calendarInstance.set(Calendar.DAY_OF_MONTH, dateArray[2])
                    calendarInstance.set(Calendar.HOUR_OF_DAY, timeArray[0])
                    calendarInstance.set(Calendar.MINUTE, timeArray[1])
                    calendarInstance.set(Calendar.SECOND, 0)
                }

                viewModelScope.launch {
                    if(alarm) {
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
                        Log.d("ALARM","Esco dalla coroutine alarm")
                    }
                }

                if(calendar && date != "" && time != "") {
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
            }
        }
    }

    private fun sendUiEvent(event: UiEvent){
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}