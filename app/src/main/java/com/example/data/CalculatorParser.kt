package com.example.data

import java.util.Locale

object CalculatorParser {
    fun evaluate(expression: String): Double {
        val cleaned = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace(" ", "")

        if (cleaned.isEmpty()) return 0.0
        return try {
            val parser = Parser(cleaned)
            parser.parse()
        } catch (e: Exception) {
            Double.NaN
        }
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return "Tak terdefinisi"
        
        // Check if value behaves as an integer
        if (value == value.toLong().toDouble()) {
            return value.toLong().toString()
        }
        
        return try {
            // Limits to 10 decimal places safely
            val formatted = String.format(Locale.US, "%.10f", value)
            
            // Remove trailing zeros and possible trailing dot
            var trimmed = formatted
            if (trimmed.contains(".")) {
                trimmed = trimmed.replace(Regex("0+$"), "")
                if (trimmed.endsWith(".")) {
                    trimmed = trimmed.substring(0, trimmed.length - 1)
                }
            }
            trimmed
        } catch (e: Exception) {
            value.toString()
        }
    }

    private class Parser(private val str: String) {
        private var pos = 0

        fun parse(): Double {
            val res = parseExpression()
            if (pos < str.length) {
                // Trailing unparsed tokens
                return Double.NaN
            }
            return res
        }

        private fun peek(): Char? {
            return if (pos < str.length) str[pos] else null
        }

        private fun next(): Char? {
            return if (pos < str.length) str[pos++] else null
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                val ch = peek()
                if (ch == '+') {
                    next()
                    x += parseTerm()
                } else if (ch == '-') {
                    next()
                    x -= parseTerm()
                } else {
                    break
                }
            }
            return x
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                val ch = peek()
                if (ch == '*' || ch == '×') {
                    next()
                    x *= parseFactor()
                } else if (ch == '/' || ch == '÷') {
                    next()
                    val divisor = parseFactor()
                    x = if (divisor != 0.0) x / divisor else Double.NaN
                } else {
                    break
                }
            }
            return x
        }

        private fun parseFactor(): Double {
            val ch = peek()
            if (ch == '+') {
                next()
                return parseFactor()
            }
            if (ch == '-') {
                next()
                return -parseFactor()
            }
            if (ch == '(') {
                next() // consume '('
                val result = parseExpression()
                val nextCh = peek()
                if (nextCh == ')') {
                    next() // consume ')'
                }
                // Handle percentage on bracketed groups e.g., (2+3)% -> 0.05
                if (peek() == '%') {
                    next()
                    return result / 100.0
                }
                return result
            }

            val start = pos
            if (peek() in '0'..'9' || peek() == '.') {
                if (peek() == '.') {
                    next()
                }
                while (peek() in '0'..'9') {
                    next()
                }
                if (peek() == '.') {
                    next()
                    while (peek() in '0'..'9') {
                        next()
                    }
                }
                val valStr = str.substring(start, pos)
                var value = valStr.toDoubleOrNull() ?: 0.0
                if (peek() == '%') {
                    next()
                    value /= 100.0
                }
                return value
            }

            return 0.0
        }
    }
}
