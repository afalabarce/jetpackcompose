package io.github.afalabarce.jetpackcompose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.afalabarce.jetpackcompose.utilities.format
import java.util.*
import kotlin.reflect.KProperty

enum class DataType{
    Long,
    Decimal,
    Date,
    Boolean,
    Image,
    List,
    String
}

data class GridColumn(
    val visibleIndex: Int,
    val title: String,
    val dataType: DataType,
    val fieldName: String = "",
    val width: Dp,
    val height: Dp,
    val textAlign: TextAlign = TextAlign.Center,
    val textSize: TextUnit = 12.sp,
    val fontWeight: FontWeight = FontWeight.Normal,
    val backgroundColor: Color = Color.Gray,
    val textColor: Color = Color.Black,
    val dataFormat: String = "",
    val visible: Boolean = true
)
@Composable
private fun HeaderCell(column: GridColumn, onClick: (GridColumn) -> Unit){
    Button(
        onClick = { onClick(column) },
        modifier = Modifier
            .size(column.width, column.height)
            .padding(0.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = column.backgroundColor)
    ) {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            text = column.title,
            fontSize = column.textSize,
            textAlign = column.textAlign,
            fontWeight = column.fontWeight
        )
    }
}

@Composable
private fun <T>DataRow(row: T, columns: List<GridColumn>){
    val classType = row!!::class

    Row(modifier = Modifier
        .fillMaxWidth(),
        verticalAlignment = Alignment.Top) {
        columns.filter { c -> c.visible }.sortedBy { c -> c.visibleIndex }.forEach { column ->
            val columnField = classType.members.firstOrNull { p -> p.name == column.fieldName} as? KProperty

            if (columnField != null){
                val cellValue = columnField.getter.call(row).toString()
                /*try {
                    when (column.dataType) {
                        DataType.Long -> (columnField.get(row) as Long).format(column.dataFormat)
                        DataType.Decimal -> (columnField.get(row) as Float).format(column.dataFormat)
                        DataType.Boolean -> (columnField.get(row) as Boolean).toString()
                        DataType.Date -> (columnField.get(row) as? Date
                            ?: Calendar.getInstance().time).format(column.dataFormat)
                        else -> columnField.get(row).toString()
                    }
                } catch (ex: java.lang.Exception) {
                    columnField.name
                }
*/
                Text(
                    text = cellValue,
                    fontSize = column.textSize,
                    modifier = Modifier.size(column.width, column.height),
                    color = column.textColor
                )
            }
        }
    }
}

@Composable
fun <T>DataGrid(
    modifier: Modifier,
    columns: List<GridColumn>,
    items: List<T>,
    onColumnClick: (GridColumn) -> Unit
){
    Column(modifier = modifier
        .horizontalScroll(rememberScrollState())) {
        Row(modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
        ) {
            columns.filter { c -> c.visible }
                   .sortedBy { x -> x.visibleIndex }
                   .forEach { column -> HeaderCell(column = column, onClick = onColumnClick) }
        }
        LazyColumn(modifier = Modifier.fillMaxHeight()){
            items(items){row -> DataRow(row, columns = columns) }
        }
    }
}

data class TestEntity(val column1: String, val column2: Long, val column3: Float, val column4: Date)

@Preview(showSystemUi = true)
@Composable
fun DataGridPreview(){

    val columns = listOf(
        GridColumn(
            visibleIndex = 0,
            title = "Header 1",
            dataType = DataType.String,
            fieldName = "column1",
            width = 110.dp,
            height = 32.dp
        ),
        GridColumn(
            visibleIndex = 1,
            title = "Header 2",
            dataType = DataType.Long,
            fieldName = "column2",
            width = 110.dp,
            height = 32.dp
        ),
        GridColumn(
            visibleIndex = 3,
            title = "Header 3",
            dataType = DataType.Decimal,
            fieldName = "column3",
            width = 110.dp,
            height = 32.dp
        ),
        GridColumn(
            visibleIndex = 2,
            title = "Header 4",
            dataType = DataType.Date,
            fieldName = "column4",
            width = 110.dp,
            height = 32.dp
        )
    )

    DataGrid(
        modifier = Modifier.fillMaxSize(),
        columns = columns,
        items = listOf(
                    TestEntity("Data 1", 1000L,2.5f, Calendar.getInstance().time),
                    TestEntity("Data 2", 100L, 4.5f, Calendar.getInstance().time)
        ),
        onColumnClick = {}
    )
}