package com.example

import com.example.data.CalculatorParser
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testSimpleOperations() {
    assertEquals(5.0, CalculatorParser.evaluate("2 + 3"), 1e-9)
    assertEquals(4.0, CalculatorParser.evaluate("10 − 6"), 1e-9) // with mathematical minus
    assertEquals(15.0, CalculatorParser.evaluate("3 × 5"), 1e-9) // with multiplication symbol
    assertEquals(2.5, CalculatorParser.evaluate("5 ÷ 2"), 1e-9) // with division symbol
  }

  @Test
  fun testPrecedence() {
    assertEquals(17.0, CalculatorParser.evaluate("2 + 3 × 5"), 1e-9)
    assertEquals(2.0, CalculatorParser.evaluate("10 − 4 × 2"), 1e-9)
  }

  @Test
  fun testParentheses() {
    assertEquals(25.0, CalculatorParser.evaluate("(2 + 3) × 5"), 1e-9)
    assertEquals(12.0, CalculatorParser.evaluate("2 × (3 + 3)"), 1e-9)
  }

  @Test
  fun testPercentage() {
    assertEquals(0.5, CalculatorParser.evaluate("50%"), 1e-9)
    assertEquals(10.5, CalculatorParser.evaluate("10 + 50%"), 1e-9)
    assertEquals(5.0, CalculatorParser.evaluate("100 × 5%"), 1e-9)
    assertEquals(0.05, CalculatorParser.evaluate("(2 + 3)%"), 1e-9)
  }

  @Test
  fun testFormatting() {
    assertEquals("5", CalculatorParser.formatResult(5.000))
    assertEquals("2.5", CalculatorParser.formatResult(2.5))
    assertEquals("0.3333333333", CalculatorParser.formatResult(1.0 / 3.0))
    assertEquals("Error", CalculatorParser.formatResult(Double.NaN))
  }
}
