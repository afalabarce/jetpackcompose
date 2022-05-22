@file:OptIn(ExperimentalMaterialApi::class)

package io.github.afalabarce.jetpackcompose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.afalabarce.jetpackcompose.utilities.format
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext

private typealias CalendarWeek = List<CalendarDay>
private val BlueCalendar = Color(0xFF005594)
//region Private model for Calendar

//region Private fields

private val CELL_SIZE = 28.dp

//endregion

//region Enums

private enum class DayOfWeek{
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday,
    Sunday;

    val number: Int
        get() = when(this){
            Monday -> 1
            Tuesday -> 2
            Wednesday -> 3
            Thursday -> 4
            Friday -> 5
            Saturday -> 6
            Sunday -> 7
        }

    val dayName: String
        get() = when(this){
            Monday -> "Lunes"
            Tuesday -> "Martes"
            Wednesday -> "Miércoles"
            Thursday -> "Jueves"
            Friday -> "Viernes"
            Saturday -> "Sábado"
            Sunday -> "Domingo"
        }

    companion object{
        fun fromNumber(value: Int): DayOfWeek = when(value){
            1 -> Sunday
            2 -> Monday
            3 -> Tuesday
            4 -> Wednesday
            5 -> Thursday
            6 -> Friday

            else -> Saturday
        }
    }
}

private enum class DaySelectedStatus{
    NonSelected,
    Selected,
    NonClickable,
    FirstDay,
    LastDay,
    FirstLastDay;
    fun isMarked(): Boolean = when (this) {
        Selected -> true
        FirstDay -> true
        LastDay -> true
        FirstLastDay -> true
        else -> false
    }

    fun color(selectedColor: Color): Color = when (this) {
        Selected -> selectedColor
        else -> Color.Transparent
    }
}

private enum class Month{
    None,
    January,
    February,
    March,
    April,
    May,
    June,
    July,
    August,
    September,
    Obtober,
    November,
    December;

    fun monthNumber(): Int = when(this){
        January -> 1
        February -> 2
        March -> 3
        April -> 4
        May -> 5
        June -> 6
        July -> 7
        August -> 8
        September -> 9
        Obtober -> 10
        November -> 11
        December -> 12
        else -> 0
    }

    override fun toString(): String = when(this){
        January -> "Enero"
        February -> "Febrero"
        March -> "Marzo"
        April -> "Abril"
        May -> "Mayo"
        June -> "Junio"
        July -> "Julio"
        August -> "Agosto"
        September -> "Septiembre"
        Obtober -> "Octubre"
        November -> "Noviembre"
        December -> "Diciembre"
        else -> "Ninguno"
    }

    companion object{
        fun fromNumber(value: Int): Month = when(value){
            1 -> January
            2 -> February
            3 -> March
            4 -> April
            5 -> May
            6 -> June
            7 -> July
            8 -> August
            9 -> September
            10 ->  Obtober
            11 ->  November
            12 ->  December
            else -> None
        }
    }
}

//endregion

//region Private extension functions

private inline fun <T>Flow<T>.collectSync(crossinline bodyCollect: (T) -> Unit) {
    val flow = this

    MainScope().launch {
        withContext(Dispatchers.IO){
            flow.collect{ v ->
                bodyCollect(v)
            }
        }
    }
}

//endregion

//region Private data classes

private class CalendarDay(val value: Int, val month: Month, val year: Int, status: DaySelectedStatus){
    var status by mutableStateOf(status)

    val date: Date by mutableStateOf(Calendar.getInstance().also { c ->
        if (value < 1)
            c.set(1900, 1, 1)
        else
            c.set(this.year, this.month.monthNumber() - 1, value)

        c.set(Calendar.HOUR, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
    }.time)

}

private data class CalendarMonth(val name: String, val month: Month, val year: Int ){
    val startDayOfWeek = mutableStateOf(DayOfWeek.Monday).apply {
        val firstDay = Calendar.getInstance().also { c -> c.set(this@CalendarMonth.year, this@CalendarMonth.month.monthNumber() - 1, 1) }
        this.value = DayOfWeek.fromNumber(firstDay.get(Calendar.DAY_OF_WEEK))
    }
    val shortName by lazy { name.substring(1, 3) }

    val lastDayOfMonth = mutableStateOf(0).apply {
        this.value = Calendar.getInstance().also { c ->
            c.set(this@CalendarMonth.year, this@CalendarMonth.month.monthNumber() - 1, 1)
            c.add(Calendar.MONTH, 1)
            c.add(Calendar.DATE, -1)
        }.get(Calendar.DATE)
    }
    val days = mutableListOf<CalendarDay>().apply {
        val lastDay = this@CalendarMonth.lastDayOfMonth.value
        val lastDayWeek = DayOfWeek.fromNumber(Calendar.getInstance().also { c -> c.set(this@CalendarMonth.year, this@CalendarMonth.month.monthNumber() - 1, lastDay) }.get(Calendar.DAY_OF_WEEK))

        addAll(IntRange(1, startDayOfWeek.value.number - 1).map { CalendarDay(0, Month.None, 0, DaySelectedStatus.NonClickable) })
        addAll(IntRange(1, lastDay).map { d -> CalendarDay(d, this@CalendarMonth.month, this@CalendarMonth.year, DaySelectedStatus.NonSelected) })
        addAll(IntRange(lastDayWeek.number + 1, 7).map { CalendarDay(0, Month.None, 0, DaySelectedStatus.NonClickable) })
    }.toList()

    val weeks by lazy {
        days.chunked(7).map { lst ->
            if (lst.size == 7)
                lst
            else
                lst.also{ l -> l.toMutableList().addAll(IntRange(1, 7 - lst.size)
                    .map { CalendarDay(0, Month.None, 0, DaySelectedStatus.NonClickable) }) }
        }.toList()
    }

    fun getDay(day: Int) = this.days.first { d -> d.value == day }
    fun getPreviousDay(day: Int) =  if (day <= 1)
        null
    else
        this.days.filter { x -> x.value < day }.maxByOrNull { x -> x.value }

    fun getNextDay(day: Int) = if (day >= this.lastDayOfMonth.value)
        null
    else
        this.days.filter { x -> x.value > day }.minByOrNull { x -> x.value }
    operator fun compareTo(other: CalendarMonth) = if (this.year == other.year)
        this.month.monthNumber().compareTo(other.month.monthNumber())
    else
        this.year.compareTo(other.year)

}

private class CalendarYear(val year: Int){
    val months: List<CalendarMonth>
        get() = IntRange(1,12).map { month -> CalendarMonth(Month.fromNumber(month).toString(), Month.fromNumber(month), this.year) }
}

private data class DaySelected(val day: Int, val month: CalendarMonth, val year: CalendarYear){
    val calendarDay by lazy { this.month.getDay(day) }

    override fun toString(): String = if(this.compareTo(DayEmptySelected) == 0) "" else "${this.day.format("00")} ${this.month.shortName}"

    operator fun compareTo(other: DaySelected) = if (this.month.compareTo(other.month) == 0)
        this.day.compareTo(other.day)
    else
        this.month.compareTo(other.month)
    companion object{
        val DayEmptySelected = DaySelected(-1, CalendarMonth("", Month.None, 0 ), CalendarYear(0))
    }
}

private class DatesSelectedState(private val year: CalendarYear){
    private var from by mutableStateOf(DaySelected.DayEmptySelected)
    private var to by mutableStateOf(DaySelected.DayEmptySelected)

    private fun selectDatesInBetween(from: DaySelected, to: DaySelected){
        val daysInRange = this.year.months.flatMap { m -> m.days }
            .filter { d -> d.status != DaySelectedStatus.NonClickable &&
                    from.calendarDay.date <= d.date &&
                    to.calendarDay.date >= d.date
            }.map { d -> d.also { ds ->  ds.status = DaySelectedStatus.Selected } }
        daysInRange.filter { d -> d.value == 1 }
            .map { d -> d.also { df -> df.status = DaySelectedStatus.FirstDay } }
        daysInRange.filter { d -> d.value == this.year.months.first { m -> m.month == d.month }.lastDayOfMonth.value }
            .map { d -> d.also { dl -> dl.status = DaySelectedStatus.LastDay } }
    }

    private fun setDates(newFrom: DaySelected, newTo: DaySelected){
        if (newTo == DaySelected.DayEmptySelected) {
            from = newFrom
            from.calendarDay.status = DaySelectedStatus.FirstLastDay
        } else {
            from = newFrom.apply { calendarDay.status = DaySelectedStatus.FirstDay }
            selectDatesInBetween(newFrom, newTo)
            to = newTo.apply { calendarDay.status = DaySelectedStatus.LastDay }
        }
    }

    fun daySelected(newDate: DaySelected) {
        if (from == DaySelected.DayEmptySelected && to == DaySelected.DayEmptySelected) {
            setDates(newDate, DaySelected.DayEmptySelected)
        } else if (from != DaySelected.DayEmptySelected && to != DaySelected.DayEmptySelected) {
            clearDates()
            daySelected(newDate = newDate)
        } else if (from == DaySelected.DayEmptySelected) {
            if (newDate < to) setDates(newDate, to)
            else if (newDate > to) setDates(to, newDate)
        } else if (to == DaySelected.DayEmptySelected) {
            if (newDate < from) setDates(newDate, from)
            else if (newDate > from) setDates(from, newDate)
        }
    }

    fun clearDates(){
        this.year.months.flatMap { m -> m.days }
            .filter { d -> d.status != DaySelectedStatus.NonClickable }
            .map { du -> du.also { dux -> dux.status = DaySelectedStatus.NonSelected } }
        from.calendarDay.status = DaySelectedStatus.NonSelected
        to.calendarDay.status = DaySelectedStatus.NonSelected
        from = DaySelected.DayEmptySelected
        to = DaySelected.DayEmptySelected
    }

    override fun toString(): String = if (this.from.compareTo(DaySelected.DayEmptySelected) == 0 && this.from.compareTo(this.to) == 0)
        ""
    else if (this.from.compareTo(this.to) == 0)
        this.from.toString()
    else
        "${this.from} - ${this.to}"

}

private class CalendarViewModel(override val coroutineContext: CoroutineContext) : CoroutineScope {
    private val _currentYear: MutableStateFlow<CalendarYear> by lazy { MutableStateFlow(CalendarYear(Calendar.getInstance().get(Calendar.YEAR))) }
    val currentYear: Flow<CalendarYear>
        get() = this._currentYear
    private val _currentMonth: MutableStateFlow<CalendarMonth> by lazy {
        MutableStateFlow(this._currentYear.value.months.first { m -> m.month == Month.fromNumber(Calendar.getInstance().get(Calendar.MONTH) + 1) })
    }
    val currentMonth: Flow<CalendarMonth>
        get() = this._currentMonth

    private val _currentDate: MutableStateFlow<Date> by lazy { MutableStateFlow(Calendar.getInstance().time) }
    val currentDate: Flow<Date>
        get() = this._currentDate

    fun setYear(newYear: Int) = runBlocking {
        withContext(Dispatchers.IO){
            val month = this@CalendarViewModel._currentMonth.value.month
            val day =  Calendar.getInstance().also { c -> c.time = this@CalendarViewModel._currentDate.value }.get(Calendar.DAY_OF_MONTH)
            val date = Calendar.getInstance().also { c -> c.set(newYear, month.monthNumber() - 1, day) }.time
            this@CalendarViewModel.setDate(date)
        }
    }

    fun setMonth(newMonth: Month) = runBlocking {
        withContext(Dispatchers.IO){
            val year = this@CalendarViewModel._currentYear.value.year
            val month = this@CalendarViewModel._currentYear.value.months.first { m -> m.month == newMonth }
            val day =  Calendar.getInstance().also { c -> c.time = this@CalendarViewModel._currentDate.value }.get(Calendar.DAY_OF_MONTH)
            val date = Calendar.getInstance().also { c -> c.set(year, newMonth.monthNumber() - 1, day) }.time
            this@CalendarViewModel.setDate(date)
        }
    }

    fun setDate(newDate: Date) = runBlocking {
        withContext(Dispatchers.IO){
            val calDate = Calendar.getInstance().also { c -> c.time = newDate }
            calDate.set(Calendar.HOUR, 0)
            calDate.set(Calendar.MINUTE, 0)
            calDate.set(Calendar.SECOND, 0)
            calDate.set(Calendar.MILLISECOND, 0)
            val date = calDate.time
            val calendarYear = CalendarYear(calDate.get(Calendar.YEAR))
            val calendarMonth = calendarYear.months.first { m -> m.month == Month.fromNumber(calDate.get(Calendar.MONTH) + 1) }

            this@CalendarViewModel._currentYear.emit(calendarYear)
            this@CalendarViewModel._currentMonth.emit(calendarMonth)
            this@CalendarViewModel._currentDate.emit(date)
        }
    }
}

//endregion

//endregion

//region Private composables

@Composable
private fun Circle(color: Color) {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(color)
    }
}

@Composable
private fun SemiRect(color: Color, lookingLeft: Boolean = true) {
    val layoutDirection = LocalLayoutDirection.current
    Canvas(modifier = Modifier.fillMaxSize()) {
        val offset = if (lookingLeft xor (layoutDirection == LayoutDirection.Rtl)) {
            Offset(0f, 0f)
        } else {
            Offset(size.width / 2, 0f)
        }
        val size = Size(width = size.width / 2, height = size.height)

        drawRect(size = size, topLeft = offset, color = color)
    }
}

@Composable
private fun getLeftRightWeekColors(week: CalendarWeek, month: CalendarMonth): Pair<Color, Color> {
    val materialColors = MaterialTheme.colors

    val firstDayOfTheWeek = week.first()
    val leftFillColor = if (firstDayOfTheWeek.value > 0) {
        val lastDayPreviousWeek = month.getPreviousDay(firstDayOfTheWeek.value)
        if (lastDayPreviousWeek?.status?.isMarked() == true && week[0].status.isMarked()) {
            materialColors.secondary
        } else {
            Color.Transparent
        }
    } else {
        Color.Transparent
    }

    val lastDayOfTheWeek = week.last()
    val rightFillColor = if (lastDayOfTheWeek.value > 0) {
        val firstDayNextWeek = month.getNextDay(lastDayOfTheWeek.value)
        if (firstDayNextWeek?.status?.isMarked() == true && lastDayOfTheWeek.status.isMarked()) {
            materialColors.secondary
        } else {
            Color.Transparent
        }
    } else {
        Color.Transparent
    }

    return leftFillColor to rightFillColor
}

@Composable
private fun MonthHeader(viewModel: CalendarViewModel,
                        headerBackColor: Color = Color.Transparent,
                        headerTextColor: Color = Color.Black,
                        headerButtonsBackColor: Color = Color.DarkGray,
) {
    val buttonSize: Dp = 24.dp
    var month by remember { mutableStateOf(CalendarMonth(Month.January.name, month = Month.January, 2000)) }

    viewModel.currentMonth.collectSync { newMonth ->
        month = newMonth
    }

    Column(modifier = Modifier.fillMaxWidth().background(headerBackColor)) {
        Row (modifier=Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 16.dp)) {
            FloatingActionButton(
                onClick = { viewModel.setYear(month.year - 1) },
                modifier = Modifier.size(buttonSize).weight(0.15F),
                shape = RoundedCornerShape(buttonSize),
                backgroundColor = headerButtonsBackColor,
                contentColor = Color.White
            ){
                Icon(Icons.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(month.year.format(),
                modifier = Modifier.weight(0.7F).height(buttonSize).align(Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                color = headerTextColor,
                style = MaterialTheme.typography.h6,

                )
            FloatingActionButton(
                onClick = { viewModel.setYear(month.year + 1) },
                modifier = Modifier.size(buttonSize) .weight(0.15F),
                shape = RoundedCornerShape(buttonSize),
                backgroundColor = headerButtonsBackColor,
                contentColor = Color.White
            ){
                Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        Row(modifier=Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 6.dp)) {
            FloatingActionButton(
                onClick = {
                    if (month.month != Month.January){
                        viewModel.setMonth(Month.fromNumber(month.month.monthNumber() - 1 ))
                    }else{
                        val year = month.year - 1
                        val monthDec = Month.December
                        viewModel.setYear(year).also { viewModel.setMonth(monthDec) }
                    }
                },
                modifier = Modifier.size(buttonSize).weight(0.15F),
                shape = RoundedCornerShape(buttonSize),
                backgroundColor = headerButtonsBackColor,
                contentColor = Color.White
            ){
                Icon(Icons.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(month.name.format(),
                modifier = Modifier.weight(0.7F).height(buttonSize).align(Alignment.CenterVertically),
                textAlign = TextAlign.Center,
                color = headerTextColor,
                style = MaterialTheme.typography.subtitle2,
            )
            FloatingActionButton(
                onClick = {
                    if(month.month != Month.December){
                        viewModel.setMonth(Month.fromNumber(month.month.monthNumber() + 1 ))
                    }else{
                        val year = month.year + 1
                        val monthJan = Month.January
                        viewModel.setYear(year).also { viewModel.setMonth(monthJan) }
                    }
                },
                modifier = Modifier.size(buttonSize).weight(0.15F),
                shape = RoundedCornerShape(buttonSize),
                backgroundColor = headerButtonsBackColor,
                contentColor = Color.White
            ){
                Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun DayStatusContainer(status: DaySelectedStatus, color: Color, content: @Composable () -> Unit) {
    if (status.isMarked()) {
        Box {
            Circle(color = color)
            // TODO: Only multiSelect
            //if (status == DaySelectedStatus.FirstDay) {
            //    SemiRect(color = color, lookingLeft = false)
            //} else if (status == DaySelectedStatus.LastDay) {
            //    SemiRect(color = color, lookingLeft = true)
            //}

            content()
        }
    } else {
        content()
    }
}

@Composable
private fun DayContainer(viewModel: CalendarViewModel,
                         modifier: Modifier = Modifier,
                         selected: Boolean = false,
                         onClick: () -> Unit = { },
                         onClickEnabled: Boolean = true,
                         backgroundColor: Color = Color.Transparent,
                         onClickLabel: String? = null,
                         content: @Composable () -> Unit
) {
    val stateDescriptionLabel = "${if (!selected) "No " else ""}Seleccionado"

    Surface(
        modifier = modifier
            .size(width = CELL_SIZE, height = CELL_SIZE)
            .then(
                if (onClickEnabled) {
                    modifier.semantics {
                        stateDescription = stateDescriptionLabel
                    }
                } else {
                    modifier.clearAndSetSemantics { }
                }
            ),
        onClick = onClick,
        enabled = onClickEnabled,
        color = backgroundColor,
        onClickLabel = onClickLabel
    ) {
        content()
    }
}

@Composable
private fun Day(name: String) {
    DayContainer(viewModel = CalendarViewModel(MainScope().coroutineContext)) {
        Text(
            modifier = Modifier.wrapContentSize(Alignment.Center),
            text = name,
            style = MaterialTheme.typography.caption.copy(Color.Black.copy(alpha = 0.6f))
        )
    }
}

@Composable
private fun Day(viewModel: CalendarViewModel,
                day: CalendarDay,
                month: CalendarMonth,
                selectedDayColor: Color,
                onDayClicked: (CalendarDay) -> Unit,
                modifier: Modifier = Modifier
) {
    val enabled = day.status != DaySelectedStatus.NonClickable
    var status by remember { mutableStateOf(day.status) }
    var calendarYear by remember { mutableStateOf(CalendarYear(month.year)) }
    viewModel.currentDate.collectSync { newDate ->
        if (enabled){
            val cNewDate = Calendar.getInstance().also { c -> c.time = newDate }
            val cDate = Calendar.getInstance().also { c -> c.time = day.date }
            val cNewDay = cNewDate.get(Calendar.DAY_OF_MONTH)
            val cNewMonth = cNewDate.get(Calendar.MONTH)
            val cNewYear = cNewDate.get(Calendar.YEAR)

            val cDay = cDate.get(Calendar.DAY_OF_MONTH)
            val cMonth = cDate.get(Calendar.MONTH)
            val cYear = cDate.get(Calendar.YEAR)
            status = if(cDay == cNewDay && cMonth == cNewMonth && cYear == cNewYear)
                DaySelectedStatus.Selected
            else
                DaySelectedStatus.NonSelected
        }
    }

    DayContainer(viewModel = viewModel,
        modifier = modifier.semantics {
            if (enabled) text = AnnotatedString("${day.value} ${month.name} ${month.year}")

        },
        selected = status != DaySelectedStatus.NonSelected,
        onClick = {
            viewModel.setDate(day.date)
            onDayClicked(day)
        },
        onClickEnabled = enabled,
        backgroundColor = status.color(selectedColor = selectedDayColor),
        onClickLabel = "Seleccionar"
    ) {
        DayStatusContainer(status = status, color = status.color(selectedColor = selectedDayColor)) {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .clearAndSetSemantics {},
                text = if (day.value > 0) day.value.format("00") else "",
                style = MaterialTheme.typography.body1.copy(color = Color.Black)
            )
        }
    }
}

@Composable
private fun DaysOfWeek(modifier: Modifier = Modifier) {
    Row(modifier = modifier.clearAndSetSemantics { }) {
        for (day in DayOfWeek.values()) {
            Day(name = day.dayName.take(1))
        }
    }
}

@Composable
private fun Week(viewModel: CalendarViewModel,
                 modifier: Modifier = Modifier,
                 month: CalendarMonth,
                 week: CalendarWeek,
                 selectedDayColor: Color,
                 onDayClicked: (CalendarDay) -> Unit
) {
    val (leftFillColor, rightFillColor) = getLeftRightWeekColors(week, month)

    Row(modifier = modifier) {
        val spaceModifiers = Modifier
            .weight(1f)
            .heightIn(max = CELL_SIZE)

        Surface(modifier = spaceModifiers, color = leftFillColor) {
            Spacer(Modifier.fillMaxHeight())
        }

        for (day in week) {
            Day(viewModel = viewModel,
                selectedDayColor = selectedDayColor,
                day = day,
                month = month,
                onDayClicked = onDayClicked)
        }

        Surface(modifier = spaceModifiers, color = rightFillColor) {
            Spacer(Modifier.fillMaxHeight())
        }
    }
}

@Composable
private fun ItemsCalendarMonth(
    viewModel: CalendarViewModel,
    headerBackColor: Color = Color.Transparent,
    headerTextColor: Color = Color.Black,
    headerButtonsBackColor: Color = Color.DarkGray,
    selectedDayColor: Color,
    onDayClicked: (Date) -> Unit
) {
    val contentModifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
    var currentDate by remember { mutableStateOf(Calendar.getInstance().time) }
    var calendarYear by remember { mutableStateOf(CalendarYear(Calendar.getInstance().also { c -> c.time = currentDate }.get(Calendar.YEAR))) }
    var month by remember { mutableStateOf(calendarYear.months.first { m -> m.month == Month.fromNumber(Calendar.getInstance().also { c -> c.time = currentDate }.get(Calendar.MONTH) + 1) }) }

    viewModel.currentYear.collectSync { newYear ->
        calendarYear = newYear
    }

    viewModel.currentMonth.collectSync { newMonth ->
        month = newMonth
    }

    viewModel.currentDate.collectSync { newDate ->
        currentDate = newDate
    }

    Scaffold (modifier = Modifier.fillMaxSize(),
        topBar = {
            MonthHeader(
                viewModel = viewModel,
                headerBackColor = headerBackColor,
                headerTextColor = headerTextColor,
                headerButtonsBackColor = headerButtonsBackColor,
            )
        },
        floatingActionButton = {
            FloatingActionButton(modifier = Modifier.size(48.dp),
                onClick = {
                    val now = Calendar.getInstance().time
                    viewModel.setDate(now)
                },
                shape = RoundedCornerShape(50),
                backgroundColor = BlueCalendar
            ) {
                Icon(Icons.Rounded.DateRange, "", tint = Color.White)
            }
        },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BottomAppBar(modifier = Modifier.height(52.dp),
                backgroundColor = BlueCalendar,
                cutoutShape = RoundedCornerShape(50),
                contentColor = Color.White,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {}
        }
    ) {
        Column (modifier = Modifier.fillMaxWidth()){
            DaysOfWeek(modifier = contentModifier)
            month.weeks.forEachIndexed { _, week ->
                Week(viewModel = viewModel,
                    modifier = contentModifier,
                    week = week,
                    month = month,
                    selectedDayColor = selectedDayColor,
                    onDayClicked = { day ->

                        onDayClicked(Calendar.getInstance().also { c -> c.set(month.year, month.month.monthNumber() - 1, day.value,0,0,0) }.time)
                    }
                )
            }
        }
    }

}

//endregion

/**
 * Draws a CalendarPicker with Today functionality
 * @param date
 * @param elevation
 * @param headerBackColor
 * @param headerButtonsBackColor
 * @param headerTextColor
 * @param selectedDayBackColor
 * @param onDayClicked
 */
@Composable
fun CalendarPicker(
    date: Date,
    elevation: Dp = 0.dp,
    headerBackColor: Color = Color.Transparent,
    headerTextColor: Color = Color.Black,
    headerButtonsBackColor: Color = Color.DarkGray,
    selectedDayBackColor: Color = Color.Green,
    onDayClicked: (Date) -> Unit
) {
    val viewModel = CalendarViewModel(MainScope().coroutineContext)

    Card(modifier = Modifier.width(200.dp).height(340.dp),
        shape = MaterialTheme.shapes.small,
        elevation = elevation
    ) {
        ItemsCalendarMonth(viewModel = viewModel,
            headerBackColor = headerBackColor,
            headerTextColor = headerTextColor,
            headerButtonsBackColor = headerButtonsBackColor,
            selectedDayColor = selectedDayBackColor,
            onDayClicked = onDayClicked)
    }

    viewModel.setDate(date)
}

/**
 * Draws a CalendarDropDown component
 * @param date
 * @param dateFormat
 * @param editorWidth
 * @param focusRequester
 * @param headerBackColor
 * @param headerButtonsBackColor
 * @param headerTextColor
 * @param label
 * @param paddingValues
 * @param readOnly
 * @param selectedDayBackColor
 * @param textFieldColors
 * @param onDayClicked
 */
@Composable
fun CalendarDropDown(label: String,
                     dateFormat: String = "dd/MM/yyyy",
                     focusRequester: FocusRequester? = null,
                     editorWidth: Dp,
                     paddingValues: PaddingValues = PaddingValues(0.dp),
                     textFieldColors: TextFieldColors = TextFieldDefaults.textFieldColors(),
                     date: MutableState<Date?>,
                     readOnly: Boolean = false,
                     headerBackColor: Color = Color.Transparent,
                     headerTextColor: Color = Color.Black,
                     headerButtonsBackColor: Color = Color.DarkGray,
                     selectedDayBackColor: Color = Color.Green,
                     onDayClicked: (Date) -> Unit){

    var dateStr by remember { mutableStateOf(TextFieldValue ("")) }
    var expandedDropDown by remember { mutableStateOf(false) }
    var dropDrownDate by remember { mutableStateOf(Calendar.getInstance().also { c -> c.timeInMillis = 0 }.time) }

    Box(modifier = Modifier.width(editorWidth).padding(paddingValues)){
        OutlinedTextField(value = dateStr,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester ?: FocusRequester.Default),
            label = { Text(label) },
            colors = textFieldColors,
            readOnly = readOnly,
            singleLine = true,
            trailingIcon = {
                Icon(imageVector = Icons.Rounded.DateRange,
                    contentDescription = null,
                    modifier = Modifier.focusable(false).clickable(true) {
                        if (!readOnly)
                            expandedDropDown = !expandedDropDown
                    }
                )

                DropdownMenu(expanded = expandedDropDown,
                    onDismissRequest = { expandedDropDown = false }
                ) {
                    CalendarPicker(date = dropDrownDate,
                        headerBackColor = headerBackColor,
                        headerTextColor = headerTextColor,
                        headerButtonsBackColor = headerButtonsBackColor,
                        selectedDayBackColor = selectedDayBackColor,
                        onDayClicked = { newDate ->
                            dateStr = TextFieldValue(newDate.format(dateFormat))
                            expandedDropDown = false
                            onDayClicked(newDate)
                        })
                }
            },
            onValueChange = { newDate ->
                if (newDate.text.isNotEmpty()){
                    try{
                        val currentDate = dateStr.text
                        val calNow = Calendar.getInstance()
                        val currentMonth = calNow.get(Calendar.MONTH) + 1
                        val currentYear = calNow.get(Calendar.YEAR)
                        val tmpDate = newDate.text.replace(currentDate, "")

                        var nDate = if (currentDate.length > tmpDate.length)
                            "$tmpDate${currentDate.substring(tmpDate.length)}"
                        else
                            newDate.text
                        val selectionStart = when (tmpDate.length){
                            2, 5 -> tmpDate.length
                            else -> tmpDate.length - 1
                        }

                        nDate.replace("\t", "")

                        if (!Pattern.compile("\\d\\d/\\d\\d/\\d\\d\\d\\d").matcher(nDate).matches()){
                            nDate = when(nDate.length){
                                2 -> "$nDate/${currentMonth.format("00")}/$currentYear"
                                5 -> "$nDate/$currentYear"
                                else -> nDate
                            }
                        }

                        if (nDate.length > 10)
                            nDate = nDate.substring(0, 10)

                        if (nDate.length == 10)
                            SimpleDateFormat(dateFormat, Locale.getDefault()).parse(nDate)

                        dropDrownDate = SimpleDateFormat(dateFormat, Locale.getDefault()).parse(nDate)
                        date.value = dropDrownDate
                        dateStr = TextFieldValue(nDate).copy(nDate, selection = TextRange(selectionStart + 1, nDate.length))
                    }catch (ex: Exception){

                    }
                }
            }
        )
        if (dateStr.text == "" || dateStr.text != (date.value?.format(dateFormat) ?: "")){
            dateStr = TextFieldValue(date.value?.format(dateFormat) ?: "")
            dropDrownDate = date.value
        }
    }
}