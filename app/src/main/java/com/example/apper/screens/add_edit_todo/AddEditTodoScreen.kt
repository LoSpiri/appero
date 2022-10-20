package com.example.apper.screens.add_edit_todo

import android.Manifest
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.widget.CalendarView
import android.widget.TimePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.apper.R
import com.example.apper.speechToText.SpeechRecognizerContract
import com.example.apper.util.UiEvent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState



@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddEditTodoScreen(
    onPopBackStack : ()->Unit,
    viewModel: AddEditTodoViewModel = hiltViewModel()
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SCHEDULE_EXACT_ALARM,
            Manifest.permission.INTERNET,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    )
    SideEffect {
        permissionsState.launchMultiplePermissionRequest()
    }
    val recordingPermission = permissionsState.permissions.find { it.permission == Manifest.permission.RECORD_AUDIO}

    val titleSpeechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = SpeechRecognizerContract(),
        onResult = {
            viewModel.onEvent(AddEditTodoEvent.OnTitleChange(it.toString().drop(1).dropLast(1)))
        }
    )

    val descriptionSpeechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = SpeechRecognizerContract(),
        onResult = {
            viewModel.onEvent(AddEditTodoEvent.OnDescriptionChange(it.toString().drop(1).dropLast(1)))
        }
    )

    val scaffoldState =  rememberScaffoldState()
    LaunchedEffect(key1 = true){
        viewModel.uiEvent.collect{event ->
            when(event){
                is UiEvent.PopBackStack -> onPopBackStack()
                is UiEvent.ShowSnackBar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.action
                    )
                }
                else -> Unit
            }
        }
    }

    var showCalendarView by remember { mutableStateOf(false) }
    var showTimePickerView by remember { mutableStateOf(false) }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .fillMaxSize()
            .padding(17.dp),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if(showCalendarView){
                    showCalendarView = false
                }
                else if (showTimePickerView){
                    showTimePickerView = false
                }
                else {
                    viewModel.onEvent(AddEditTodoEvent.OnSaveTodoClick)
                }
            }) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
            }
        }
    ) {

        if (showCalendarView) {
            AndroidView(
                { CalendarView(it) },
                modifier = Modifier.wrapContentWidth(),
                update = {
                    it.setOnDateChangeListener {
                            calendarView, year, month, day ->
                                viewModel.onEvent(AddEditTodoEvent.OnDateClick(year,month,day))
                    }
                }
            )
        }
        else if (showTimePickerView){
            AndroidView(
                {
                    TimePicker(it)
                },
                modifier = Modifier.wrapContentWidth(),
                update = {
                    it.setOnTimeChangedListener {
                            view, hourOfDay, minute ->
                                viewModel.onEvent(AddEditTodoEvent.OnTimeClick(hourOfDay,minute))
                    }
                }
            )
        }else {

            Column(modifier = Modifier.fillMaxSize()) {

                OutlinedTextField(
                    value = viewModel.title,
                    onValueChange = {
                        viewModel.onEvent(AddEditTodoEvent.OnTitleChange(it))
                    },
                    placeholder = {
                        Text(text = "Title")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = @Composable {
                        IconButton(
                            onClick =
                            {
                                if (recordingPermission!!.status.isGranted) {
                                    titleSpeechRecognizerLauncher.launch(Unit)
                                } else
                                    permissionsState.launchMultiplePermissionRequest()
                            }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_mic_24),
                                contentDescription = "Mic"
                            )
                        }
                    }
                )
                Spacer(
                    modifier = Modifier.height(9.dp)
                )
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = {
                        viewModel.onEvent(AddEditTodoEvent.OnDescriptionChange(it))
                    },
                    placeholder = {
                        Text(text = "Description")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 4,
                    trailingIcon = @Composable {
                        IconButton(
                            onClick =
                            {

                                if (recordingPermission!!.status.isGranted) {
                                    descriptionSpeechRecognizerLauncher.launch(Unit)
                                } else
                                    permissionsState.launchMultiplePermissionRequest()
                            }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_mic_24),
                                contentDescription = "Mic"
                            )
                        }
                    }
                )
                Spacer(
                    modifier = Modifier.height(9.dp)
                )
                OutlinedTextField(
                    value = if (viewModel.date == ""){
                        viewModel.date
                    }
                    else {
                        val arr = viewModel.date.split("-").map{it.toInt()}.toMutableList()
                        arr[1]++
                        arr.joinToString("-")
                         },
                    onValueChange = {
                        showCalendarView = true
                    },
                    placeholder = {
                        Text(text = "Set date")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showCalendarView = true
                        },
                    singleLine = false,
                    maxLines = 4,
                    trailingIcon = @Composable {
                        IconButton(
                            onClick =
                            {
                                showCalendarView = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Set date"
                            )
                        }
                    }
                )
                Spacer(
                    modifier = Modifier.height(9.dp)
                )
                /*
                OutlinedTextField(
                    value = viewModel.time,
                    onValueChange = {
                        viewModel.onEvent(AddEditTodoEvent.OnTimeChange(it))
                    },
                    placeholder = {
                        Text(text = "hh-mm 24-hour format")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                */
                OutlinedTextField(
                    value = viewModel.time,
                    onValueChange = {
                        showTimePickerView = true
                    },
                    placeholder = {
                        Text(text = "Set time")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showTimePickerView = true
                        },
                    singleLine = false,
                    maxLines = 4,
                    trailingIcon = @Composable {
                        IconButton(
                            onClick =
                            {
                                showTimePickerView = true
                            }
                        ) {
                            Icon(
                                // TODO change icon to clock
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Set time"
                            )
                        }
                    }
                )
                Spacer(
                    modifier = Modifier.height(9.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        enabled = true,
                        onClick = {
                            if (recordingPermission!!.status.isGranted) {
                                viewModel.onEvent(AddEditTodoEvent.OnStartRecording)
                            } else {
                                permissionsState.launchMultiplePermissionRequest()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_record_voice_over_24),
                            contentDescription = "Start"
                        )
                    }
                    IconButton(
                        enabled = true,
                        onClick = {
                            if (recordingPermission!!.status.isGranted) {
                                viewModel.onEvent(AddEditTodoEvent.OnStopRecording)
                            } else {
                                permissionsState.launchMultiplePermissionRequest()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_stop_24),
                            contentDescription = "Stop"
                        )
                    }
                    IconButton(
                        enabled = true,
                        onClick = {
                            if (recordingPermission!!.status.isGranted) {
                                viewModel.onEvent(AddEditTodoEvent.OnPlayRecording)
                            } else {
                                permissionsState.launchMultiplePermissionRequest()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_play_arrow_24),
                            contentDescription = "Play"
                        )
                    }
                }
                Spacer(
                    modifier = Modifier.height(9.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = viewModel.alarm,
                        onCheckedChange = {
                            viewModel.onEvent(AddEditTodoEvent.OnAlarmChange(it))
                        }
                    )
                    Text(
                        text = "To add notification",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .wrapContentHeight()
                    )
                }
                Spacer(
                    modifier = Modifier.height(9.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = viewModel.calendar,
                        onCheckedChange = {
                            viewModel.onEvent(AddEditTodoEvent.OnCalendarSwitchChange(it))
                        }
                    )
                    Text(
                        text = "To add calendar reminder",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .wrapContentHeight()
                    )
                }
            }
        }
    }
}