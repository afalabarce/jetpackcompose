package io.github.afalabarce.jetpackcompose

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.util.Locale

@Stable
class AmountVisualTransformation(
    private val locale: Locale = Locale.getDefault(),
    private val hasMovedCursor: Boolean = true
) : VisualTransformation {

    private val symbols = DecimalFormatSymbols(locale)
    private val decimalSeparator = symbols.decimalSeparator
    private val thousandSeparator = symbols.groupingSeparator

    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text
        if (input.isValid()) {
            val inputWithSeparator = input.withCorrectSeparator()
            val numberOfDecimals = inputWithSeparator.getDecimalsNumber()
            val forNumber = if (numberOfDecimals > Zero) inputWithSeparator.getPart(Zero) else input
            var numberText = forNumber.withCorrectSeparator().formatThousands()

            if (input.last() == decimalSeparator) {
                numberText = numberText.dropLast(One) + decimalSeparator.toString()
            }

            if (numberOfDecimals > Zero) {
                numberText += decimalSeparator.toString()
                numberText += inputWithSeparator.getPart(One)
            }

            val offsetMapping = if (hasMovedCursor) {
                OKMovedCursorOffsetMapping(
                    originalInput = input,
                    inputTransformed = numberText,
                    thousandSeparator = thousandSeparator
                )
            } else {
                OKFixedCursorOffsetMapping(
                    textLength = input.length,
                    contentLength = numberText.length.safeNull()
                )
            }

            val annotatedString = AnnotatedString(
                text = numberText
            )

            return TransformedText(annotatedString, offsetMapping)
        } else {
            return TransformedText(text, OffsetMapping.Identity)
        }
    }

    private class OKFixedCursorOffsetMapping(
        private val textLength: Int,
        private val contentLength: Int
    ) : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return contentLength
        }

        override fun transformedToOriginal(offset: Int): Int {
            return textLength
        }
    }

    private class OKMovedCursorOffsetMapping(
        private val originalInput: String,
        private val inputTransformed: String,
        private val thousandSeparator: Char
    ) : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return offset + getOffsetCount(offset)
        }

        override fun transformedToOriginal(offset: Int): Int {
            return offset - getOffsetCount(offset)
        }

        private fun getOffsetCount(offset: Int): Int {
            val lastCursor = originalInput.length == offset
            val inputOffset = if (lastCursor) {
                inputTransformed
            } else
                inputTransformed.substring(Zero, offset)
            return inputOffset.count { it == thousandSeparator }
        }
    }

    private fun String.withCorrectSeparator(): String {
        val text = if (this.containsAny()) {
            var separatorText = this.replace(Comma, decimalSeparator)
            separatorText = separatorText.replace(Dot, decimalSeparator)
            separatorText
        } else {
            this
        }
        return text
    }

    private fun String.containsAny(): Boolean {
        return AllowedChars.any { this.contains(it) }
    }

    private fun Int?.safeNull() = this ?: Zero

    private fun String.isValid(): Boolean = (isNotEmpty() && startCorrect() && this.isValidDecimal())

    private fun String.isValidDecimal(): Boolean = try {
        val commaReplaced = this.replace(Comma, Dot)
        val correctDecimal = commaReplaced.replace(decimalSeparator, Dot)
        BigDecimal(correctDecimal)
        true
    } catch (exception: NumberFormatException) {
        false
    }

    private fun String.startCorrect(): Boolean {
        val regex = Regex(StartRegex)
        return !regex.matches(this) && this.first().isDigit()
    }

    private fun String.getDecimalsNumber(): Int {
        val decimalParts = this.split(decimalSeparator)
        return if (decimalParts.size == MaxSize) {
            decimalParts[One].length
        } else {
            Zero
        }
    }

    private fun String.getPart(index: Int): String {
        val decimalParts = this.split(decimalSeparator)
        return if (decimalParts.size == MaxSize) {
            decimalParts[index]
        } else {
            EmptyString
        }
    }

    private fun String.formatThousands(): String {
        val addDecimal = !last().isDigit()
        val reversed = this.reversed().replace(decimalSeparator.toString(), EmptyString)
        val formatted = StringBuilder()
        
        for ((index, char) in reversed.withIndex()) {
            if (index > Zero && index % Thousands == Zero && char.isDigit()) {
                formatted.append(thousandSeparator)
            }
            formatted.append(char)
        }
        val formatThousands = formatted.reverse().toString()
        return if (addDecimal) formatThousands + decimalSeparator else formatThousands
    }
}

private const val Zero = 0
private const val One = 1
private const val MaxSize = 2
private const val Thousands = 3
private const val Comma = ','
private const val Dot = '.'
private const val EmptyString = ""
private const val StartRegex = "^0\\d+"
private val AllowedChars = setOf(Comma, Dot)