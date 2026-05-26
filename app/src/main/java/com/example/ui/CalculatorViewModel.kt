package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CalculatorParser
import com.example.data.HistoryEntity
import com.example.data.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HistoryRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HistoryRepository(database.historyDao())
    }

    val historyEntries: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _expression = MutableStateFlow("")
    val expression = _expression.asStateFlow()

    private val _resultPreview = MutableStateFlow("")
    val resultPreview = _resultPreview.asStateFlow()

    private val _showHistory = MutableStateFlow(false)
    val showHistory = _showHistory.asStateFlow()

    fun onButtonPress(btn: String) {
        when (btn) {
            "AC", "Clear" -> {
                _expression.value = ""
                _resultPreview.value = ""
            }
            "⌫" -> {
                val current = _expression.value
                if (current.isNotEmpty()) {
                    _expression.value = current.substring(0, current.length - 1)
                    updatePreview()
                }
            }
            "=" -> {
                val currentExpr = _expression.value
                if (currentExpr.isNotEmpty()) {
                    val evaluated = CalculatorParser.evaluate(currentExpr)
                    if (!evaluated.isNaN()) {
                        val formatted = CalculatorParser.formatResult(evaluated)
                        
                        viewModelScope.launch {
                            repository.insert(
                                HistoryEntity(
                                    expression = currentExpr,
                                    result = formatted
                                )
                            )
                        }
                        
                        _expression.value = formatted
                        _resultPreview.value = ""
                    } else {
                        _resultPreview.value = "Error"
                    }
                }
            }
            "±" -> {
                toggleSign()
                updatePreview()
            }
            else -> {
                val isOp = btn in listOf("+", "−", "×", "÷")
                if (isOp && _expression.value.isEmpty()) {
                    if (btn == "−") {
                        _expression.value = "−"
                    }
                    return
                }
                
                if (isOp && _expression.value.isNotEmpty()) {
                    val lastChar = _expression.value.last().toString()
                    if (lastChar in listOf("+", "−", "×", "÷")) {
                        _expression.value = _expression.value.dropLast(1) + btn
                        return
                    }
                }

                _expression.value += btn
                updatePreview()
            }
        }
    }

    private fun toggleSign() {
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "−"
            return
        }

        if (current.startsWith("−") && !current.contains(Regex("[+×÷]"))) {
            _expression.value = current.substring(1)
            return
        } else if (!current.contains(Regex("[+×÷−]"))) {
            _expression.value = "−$current"
            return
        }

        _expression.value += "−"
    }

    private fun updatePreview() {
        val currentExpr = _expression.value
        if (currentExpr.isEmpty()) {
            _resultPreview.value = ""
            return
        }

        val lastChar = currentExpr.last().toString()
        if (lastChar in listOf("+", "−", "×", "÷", "(")) {
            val tempExpr = currentExpr.dropLast(1)
            if (tempExpr.isNotEmpty()) {
                val evaluated = CalculatorParser.evaluate(tempExpr)
                if (!evaluated.isNaN()) {
                    _resultPreview.value = CalculatorParser.formatResult(evaluated)
                }
            }
            return
        }

        val evaluated = CalculatorParser.evaluate(currentExpr)
        if (!evaluated.isNaN()) {
            _resultPreview.value = CalculatorParser.formatResult(evaluated)
        } else {
            _resultPreview.value = ""
        }
    }

    fun selectHistory(history: HistoryEntity) {
        _expression.value = history.expression
        _resultPreview.value = history.result
        _showHistory.value = false
    }

    fun toggleHistoryPanel() {
        _showHistory.value = !_showHistory.value
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}

class CalculatorViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
