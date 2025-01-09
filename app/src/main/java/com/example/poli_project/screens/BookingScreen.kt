package com.example.poli_project.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.poli_project.logic.makeBooking
import com.example.poli_project.logic.sendBookingRequest
import com.example.poli_project.model.BookingDateTime
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingScreen(
    patientId: Int,
    doctorId: Int,
    onBack: () -> Unit,
    onBookingSuccess: () -> Unit
) {
    val bookingDates = remember { mutableStateOf<List<BookingDateTime>?>(null) }
    val selectedDate = remember { mutableStateOf<BookingDateTime?>(null) }
    val selectedTime = remember { mutableStateOf<String?>(null) }
    val isBookingButtonEnabled = selectedDate.value != null && selectedTime.value != null
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    val currentMonth = remember { mutableStateOf<YearMonth?>(null) }

    val maxMonthsAhead = 2

    LaunchedEffect(doctorId) {
        isLoading.value = true
        sendBookingRequest(
            doctorId = doctorId,
            onSuccess = { dates ->
                bookingDates.value = dates
                currentMonth.value = dates.firstOrNull()?.date?.let { parseToYearMonth(it) }
                isLoading.value = false
            },
            onError = { error ->
                errorMessage.value = error
                isLoading.value = false
            }
        )
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onBack) {
                    Text(text = "Отмена")
                }
                Button(
                    onClick = {
                        selectedDate.value?.let { date ->
                            selectedTime.value?.let { time ->
                                makeBooking(
                                    patientId = patientId,
                                    doctorId = doctorId,
                                    date = date.date,
                                    time = time,
                                    onSuccess = {
                                        onBookingSuccess()
                                        onBack()
                                    },
                                    onError = { errorMessage.value = it }
                                )
                            }
                        }
                    },
                    enabled = isBookingButtonEnabled
                ) {
                    Text(text = "Записаться!")
                }
            }
        }
    ) { paddingValues ->
        if (isLoading.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage.value != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage.value ?: "Unknown error")
            }
        } else if (bookingDates.value != null) {
            val availableDates = bookingDates.value!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Выберите дату и время",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                currentMonth.value?.let { month ->
                    CalendarView(
                        dates = availableDates,
                        currentMonth = month,
                        maxMonthsAhead = maxMonthsAhead,
                        onMonthChange = { newMonth -> currentMonth.value = newMonth },
                        onDateSelected = { date ->
                            selectedDate.value = date
                            selectedTime.value = null
                        },
                        selectedDate = selectedDate.value
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                selectedDate.value?.let { date ->
                    Text(
                        text = "Выберите время:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TimeMatrix(
                        times = date.availableTimes,
                        bookedTimes = date.bookedTimes,
                        selectedTime = selectedTime.value,
                        onTimeSelected = { time -> selectedTime.value = time }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    dates: List<BookingDateTime>,
    currentMonth: YearMonth,
    maxMonthsAhead: Int,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelected: (BookingDateTime) -> Unit,
    selectedDate: BookingDateTime?
) {
    val monthDates = dates.filter {
        val dateMonth = parseToYearMonth(it.date)
        dateMonth == currentMonth
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    val previousMonth = currentMonth.minusMonths(1)
                    if (!previousMonth.isBefore(YearMonth.now())) {
                        onMonthChange(previousMonth)
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Previous Month")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(
                onClick = {
                    val nextMonth = currentMonth.plusMonths(1)
                    if (!nextMonth.isAfter(YearMonth.now().plusMonths(maxMonthsAhead.toLong()))) {
                        onMonthChange(nextMonth)
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(monthDates) { date ->
                val isSelected = date == selectedDate
                val isFullyBooked = date.isFullyBooked

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(4.dp)
                        .background(
                            color = when {
                                isFullyBooked -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.background
                            },
                            shape = CircleShape
                        )
                        .clickable(enabled = !isFullyBooked) { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.date.split("-").last(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFullyBooked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun parseToYearMonth(date: String): YearMonth {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.parse(date, formatter).let { YearMonth.of(it.year, it.month) }
}

@Composable
fun TimeMatrix(
    times: List<String>,
    bookedTimes: List<String>,
    selectedTime: String?,
    onTimeSelected: (String) -> Unit
) {
    val matrixSize = 4

    LazyVerticalGrid(
        columns = GridCells.Fixed(matrixSize),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(times) { time ->
            val isBooked = time in bookedTimes
            val isSelected = time == selectedTime

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .background(
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            isBooked -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = !isBooked) { onTimeSelected(time) }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isBooked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

