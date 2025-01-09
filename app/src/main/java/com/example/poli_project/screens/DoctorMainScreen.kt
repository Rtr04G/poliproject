package com.example.poli_project.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorMainScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToAddTemplate: () -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                TextButton(onClick = onNavigateToProfile) {
                    Text("Справка", color = MaterialTheme.colorScheme.primary)
                }
            },
            actions = {
                TextButton(onClick = onLogout) {
                    Text("Выход", color = MaterialTheme.colorScheme.primary)
                }
            }
        )

        val items = listOf("Мое расписание")
        val pagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2)

        VerticalPager(
            state = pagerState,
            count = Int.MAX_VALUE,
            contentPadding = PaddingValues(vertical = 32.dp),
            modifier = Modifier.weight(1f)
        ) { page ->
            val index = ((page % items.size) + items.size) % items.size
            val action: () -> Unit = when (index) {
                0 -> onNavigateToSchedule
                else -> { {} }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = action,
                    shape = CircleShape,
                    modifier = Modifier.size(250.dp)
                ) {
                    Text(
                        text = items[index],
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
