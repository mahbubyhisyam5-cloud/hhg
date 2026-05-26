package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.HistoryEntity
import com.example.ui.CalculatorViewModel
import com.example.ui.CalculatorViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CalculatorApp()
      }
    }
  }
}

@Composable
fun CalculatorApp() {
  val context = LocalContext.current
  val application = context.applicationContext as Application
  val viewModel: CalculatorViewModel = viewModel(
    factory = CalculatorViewModelFactory(application)
  )

  val expression by viewModel.expression.collectAsStateWithLifecycle()
  val resultPreview by viewModel.resultPreview.collectAsStateWithLifecycle()
  val showHistory by viewModel.showHistory.collectAsStateWithLifecycle()
  val historyList by viewModel.historyEntries.collectAsStateWithLifecycle()

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF121213)),
    color = Color(0xFF121213)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Kalkulator Sederhana",
          color = Color.White,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold
        )

        IconButton(
          onClick = { viewModel.toggleHistoryPanel() },
          modifier = Modifier.testTag("toggle_history_button")
        ) {
          Icon(
            imageVector = if (showHistory) Icons.Default.Close else Icons.Default.History,
            contentDescription = "Toggle History",
            tint = Color(0xFFFF9F0A),
            modifier = Modifier.size(26.dp)
          )
        }
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
      ) {
        AnimatedContent(
          targetState = showHistory,
          transitionSpec = {
            if (targetState) {
              slideInVertically { height -> -height } togetherWith slideOutVertically { height -> height }
            } else {
              slideInVertically { height -> height } togetherWith slideOutVertically { height -> -height }
            }
          },
          label = "DisplaySwitch"
        ) { isHistoryOpen ->
          if (isHistoryOpen) {
            HistoryPanel(
              historyList = historyList,
              onSelectHistory = { viewModel.selectHistory(it) },
              onClearAll = { viewModel.clearHistory() },
              onDelete = { viewModel.deleteHistoryItem(it) }
            )
          } else {
            ActiveDisplay(
              expression = expression,
              resultPreview = resultPreview,
              onBackspace = { viewModel.onButtonPress("⌫") }
            )
          }
        }
      }

      HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 1.dp)

      KeypadArea(onButtonPress = { viewModel.onButtonPress(it) })
    }
  }
}

@Composable
fun ActiveDisplay(
  expression: String,
  resultPreview: String,
  onBackspace: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(vertical = 16.dp),
    verticalArrangement = Arrangement.Bottom,
    horizontalAlignment = Alignment.End
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End
    ) {
      Text(
        text = expression.ifEmpty { "0" },
        style = MaterialTheme.typography.displayMedium,
        fontSize = if (expression.length > 10) 36.sp else 48.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White,
        textAlign = TextAlign.End,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
          .weight(1f)
          .padding(end = 8.dp)
          .testTag("expression_display")
      )
      
      if (expression.isNotEmpty()) {
        IconButton(
          onClick = onBackspace,
          modifier = Modifier
            .size(44.dp)
            .testTag("backspace_button")
        ) {
          Icon(
            imageVector = Icons.Default.KeyboardBackspace,
            contentDescription = "Backspace",
            tint = Color(0xFFA5A5A5)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (resultPreview.isNotEmpty() && resultPreview != expression) {
      Text(
        text = "= $resultPreview",
        style = MaterialTheme.typography.headlineMedium,
        fontSize = 28.sp,
        fontWeight = FontWeight.Light,
        color = Color(0xFFA5A5A5),
        textAlign = TextAlign.End,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("result_preview_display")
      )
    }
  }
}

@Composable
fun HistoryPanel(
  historyList: List<HistoryEntity>,
  onSelectHistory: (HistoryEntity) -> Unit,
  onClearAll: () -> Unit,
  onDelete: (Int) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxSize()
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Riwayat Perhitungan",
        fontSize = 16.sp,
        color = Color(0xFFA5A5A5),
        fontWeight = FontWeight.SemiBold
      )
      if (historyList.isNotEmpty()) {
        Text(
          text = "Bersihkan Semua",
          color = Color(0xFFFF453A),
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .clickable { onClearAll() }
            .padding(4.dp)
            .testTag("clear_all_history")
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (historyList.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Belum ada riwayat",
          color = Color(0xFF636366),
          fontSize = 16.sp,
          textAlign = TextAlign.Center
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(historyList, key = { it.id }) { item ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectHistory(item) }
              .testTag("history_item_${item.id}"),
            colors = CardDefaults.cardColors(
              containerColor = Color(0xFF1C1C1E)
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(
                modifier = Modifier.weight(1f)
              ) {
                Text(
                  text = item.expression,
                  color = Color(0xFFA5A5A5),
                  fontSize = 14.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "= ${item.result}",
                  color = Color.White,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              IconButton(
                onClick = { onDelete(item.id) },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Hapus",
                  tint = Color(0xFFFF453A),
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun KeypadArea(
  onButtonPress: (String) -> Unit
) {
  val keys = listOf(
    listOf("C", "(", ")", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "−"),
    listOf("1", "2", "3", "+"),
    listOf("±", "0", ".", "=")
  )

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    keys.forEach { row ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        row.forEach { char ->
          val isOperator = char in listOf("÷", "×", "−", "+", "=")
          val isMeta = char in listOf("C", "(", ")", "±")
          
          ColorRoleButton(
            label = char,
            isOperator = isOperator,
            isMeta = isMeta,
            onClick = { onButtonPress(char) },
            modifier = Modifier
              .weight(1f)
              .testTag("key_$char")
          )
        }
      }
    }
  }
}

@Composable
fun ColorRoleButton(
  label: String,
  isOperator: Boolean,
  isMeta: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val backgroundColor = when {
    isOperator -> Color(0xFFFF9F0A)
    isMeta -> Color(0xFF636366)
    else -> Color(0xFF2C2C2E)
  }

  val textColor = Color.White
  val fontSize = if (label == "Clear" || label == "AC") 18.sp else 24.sp
  val fontWeight = if (isOperator) FontWeight.SemiBold else FontWeight.Medium

  Box(
    modifier = modifier
      .height(68.dp)
      .clip(CircleShape)
      .background(backgroundColor)
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = fontSize,
      fontWeight = fontWeight,
      textAlign = TextAlign.Center
    )
  }
}
