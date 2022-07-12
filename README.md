# jetpackcompose
Jetpack Compose Composables. 

Contiene cuatro composables (por ahora) útiles para el día a día.  
  
 El primero de los composables es CalendarDropDown, que nos genera el típico desplegable con un calendario en el que seleccionar una fecha. Soporta el botón Hoy, para posicionarnos en la fecha actual.  
 
 El segundo composable, relacionado con el primero, es un Calendar (que nos permite colocar un calendario directamente en nuestra app.  
  
 El tercer composable, es un SwipeableCard, es un Card, que nos permite agregarle acciones en Swipe horizontal, tanto a izquierda como a derecha.   
 Debido a errores extraños he tenido que establecer en el composable el mapAnchors (el que se encarga de decirle al composable cuanto debe desplazar el Card a izquierda o derecha) desde la propia llamada.

 El cuarto composable, es un CircularProgressIndicator, que permite en una única llamada establecer un background al toroide del progreso según va decrementando. Además agrega un campo content que nos permite meter un composable en el interior.  

 Un ejemplo de llamada de SwipeableCard, sería el siguiente:
 
 ```kotlin
 // Función que nos devuelve todas las posibles acciones de un objeto Task, en función de sus posibilidades
 private fun taskActions(task: Task): Array<SwipeAction>{
        return arrayOf(SwipeAction(
            order = if (task.task.isOpen && !task.isWorking) 0 else -1,
            key = "StartTimeLapse",
            title = "Iniciar",
            imageVector = Icons.Filled.PunchClock,
            color = DarkBlue,
            tint = White,
            true
        ),
            SwipeAction(
                order = if (task.task.isOpen && task.isWorking) 0 else -1,
                key = "ShowTimeLapse",
                title = "Ver",
                imageVector = Icons.Filled.PunchClock,
                color = DarkGreen,
                tint = White,
                true
            ),
            SwipeAction(
                order = if (task.task.isOpen) 0 else -1,
                key = "Modify",
                title = "Editar",
                imageVector = Icons.Filled.Edit,
                color = DarkGreen,
                tint = White,
                false
            ),
            SwipeAction(
                order = if (!task.withTimeLapse) 1 else -1,
                key = "Delete",
                title = "Borrar",
                imageVector = Icons.Filled.Delete,
                color = Red,
                tint = White,
                false
            ),
            SwipeAction(
                order = if (task.task.isOpen) 2 else -1,
                key = "End",
                title = "Fin",
                imageVector = Icons.Filled.Close,
                color = DarkBlue,
                tint = White,
                false
            ),
            SwipeAction(
                //order = if (!task.task.isOpen || (task.withTimeLapse && !task.isWorking)) 2 else -1,
                order = if (task.withTimeLapse && !task.isWorking) 2 else -1,
                key = "ShowResume",
                title = "Resumen",
                imageVector = Icons.Filled.Checklist,
                color = DarkOrange,
                tint = White,
                true
            )
        ).filter {  x -> x.order >= 0 }.toTypedArray()
        
        
     // Función que pintaría el SwipeableCard, se integraría por ejemplo en un LazyColumn
     fun TaskItem(task: Task) {
        val tasksActions = this.taskActions(task)
        SwipeableCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .padding(3.dp),
            shape = MaterialTheme.shapes.small,
            anchors = getAnchorMap(LocalDensity.current, 82.dp, tasksActions),
            elevation = 3.dp,
            buttonWidth = 82.dp,
            border = BorderStroke(1.dp, color = Color.LightGray),
            swipeActions = tasksActions
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.size(64.dp)) {
                        Icon(
                            imageVector =  if (task.isWorking) Icons.Filled.PunchClock else Icons.Filled.Task,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = if (task.task.isOpen && !task.isWorking) DarkGreen else if (task.isWorking) DarkBlue else Color.Red
                        )

                        if (task.withTimeLapse){
                            Box(modifier = Modifier.size(28.dp).shadow(
                                elevation = 1.dp,
                                shape = RoundedCornerShape(5.dp)
                            ).background(DarkOrange).align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center){
                                Icon(
                                    imageVector =  Icons.Filled.PunchClock,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = White
                                )
                            }

                        }
                    }
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(text = task.task.title, style = MaterialTheme.typography.body1)
                    Text(
                        text = "${stringResource(R.string.project_label)} ${task.task.project}",
                        style = MaterialTheme.typography.caption
                    )
                    Text(
                        text = "${stringResource(R.string.creation_date_title)} ${task.task.creationDate.format("dd/MM/yyyy HH:mm")}",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
        
 ```
   
Como podemos ver, el funcionamiento es bastante simple...

Espero que sea de ayuda, tanto didácticamente, como para su uso en tus apps...

Otro elemento que se peude obtener en el módulo es un ViewModelService, esto es, permite la creación de servicios que pueden suscribirse a ViewModels que nos van a permitir el acceso reactivo a bases de datos (por ejemplo).

Un ejemplo de uso de ViewModelService sería el siguiente:

```kotlin
@AndroidEntryPoint
class MyViewModelService: ViewModelService() {
    
    @Inject lateinit var viewModel: MyServiceViewModel
    
    ...
    
}
```


Como nota final, si deseas incluir este proyecto en tus apps, en tu build.gradle sólo deberás agregar lo siguiente:

```
implementation 'io.github.afalabarce:jetpackcompose:1.2.3'
```

