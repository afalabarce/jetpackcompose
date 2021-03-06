package io.github.afalabarce.jetpackcompose.utilities

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun Date.format(strFormat: String = "dd/MM/yyyy"):String = SimpleDateFormat(strFormat).format(this)
fun Int.format(strFormat: String = "#,##0"):String = DecimalFormat(strFormat).format(this)
fun Long.format(strFormat: String = "#,##0"):String = DecimalFormat(strFormat).format(this)
fun Float.format(strFormat: String = "#,##0.00"):String = DecimalFormat(strFormat).format(this)
fun Double.format(strFormat: String = "#,##0.00"):String = DecimalFormat(strFormat).format(this)