# jetpackcompose
Jetpack Compose Composables. 

Contiene ocho composables (por ahora) útiles para el día a día.  
  
 El primero de los composables es CalendarDropDown, que nos genera el típico desplegable con un calendario en el que seleccionar una fecha. Soporta el botón Hoy, para posicionarnos en la fecha actual.  
 
 El segundo composable, relacionado con el primero, es un Calendar (que nos permite colocar un calendario directamente en nuestra app.  
  
 El tercer composable, es un SwipeableCard, es un Card, que nos permite agregarle acciones en Swipe horizontal, tanto a izquierda como a derecha.   
 Debido a errores extraños he tenido que establecer en el composable el mapAnchors (el que se encarga de decirle al composable cuanto debe desplazar el Card a izquierda o derecha) desde la propia llamada.

 El cuarto composable, es un CircularProgressIndicator, que permite en una única llamada establecer un background al toroide del progreso según va decrementando. Además agrega un campo content que nos permite meter un composable en el interior.  

El quinto, es un LabelledSwitch, que nos permite utilizar un Switch con su label a la izquierda (además de un leadingIcon).

El sexto, es un AlertDialog Sin paddings, el cual nos permite personalizar muchísimo la apariencia de nuestros alertdialog.

El septimo, Un Canvas de dibujo que nos permite dibujar líneas y agregar una marca de agua, delante o detrás de los trazos, a fin de que por ejemplo, podamos emular una firma con un sello.

Además se agrega una extensión a Modifier que permite poner un borde punteado a cualquier composable.

Para terminar, se ha agregado un Service, que permite utilizar ViewModels, ideal para que un servicio que tengamos implementado ejecute código de forma reactiva.

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

## PolygonalProgressBar

Este composable permite de una forma muy sencilla la utilización de progressbar con diseño poligonal, soporta desde 0 vértices (una circunferencia) hasta n vértices, con un mínimo de 3 (un triángulo).  

Un vídeo del funcionamiento de este composable se puede ver en [Youtube](https://www.youtube.com/watch?v=ilMw3KR6Nvk).  

Y el ejemplo de código mostrado en el vídeo sería el siguiente:  

```kotlin
@Composable
fun TestPolygon(){
    val infiniteTransition = rememberInfiniteTransition()
    var progress by remember { mutableStateOf(0f) }
    val progressFinite by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Circular Infinite Progressbar", fontSize = 14.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        PolygonalProgressBar(
            modifier = Modifier.size(120.dp),
            size = 90.dp,
            vertexNumber = 0,
            stroke = 16f,
            isInfinite = true,
            infiniteDelayInMillis = 1500,
        )

        Text(text = "Polygonal Infinite Progressbar", fontSize = 14.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        PolygonalProgressBar(
            modifier = Modifier.size(120.dp),
            size = 90.dp,
            vertexNumber = 7,
            stroke = 16f,
            isInfinite = true,
            infiniteDelayInMillis = 1100,
        )

        Text(text = "Pulsation Progressbar", fontSize = 14.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        PolygonalProgressBar(
            modifier = Modifier.size(120.dp),
            size = 90.dp,
            vertexNumber = 7,
            stroke = 16f,
            isPulsation = true,
            pulsationTimeInMillis = 1000
        )

        Text(text = progress.toString(), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Slider(
            modifier = Modifier.padding(horizontal = 16.dp),
            value = progress,
            onValueChange = { value ->
                progress = value.round(2)
            },
        )

        Text(text = "Circular Deterministic Progressbar", fontSize = 14.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        PolygonalProgressBar(
            modifier = Modifier.size(120.dp),
            size = 90.dp,
            vertexNumber = 0,
            progress = progress,
            stroke = 16f,
            isInfinite = false,
            infiniteDelayInMillis = 1100,
        )

        Text(text = "Polygonal Deterministic Progressbar", fontSize = 14.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        PolygonalProgressBar(
            modifier = Modifier.size(120.dp),
            rotationDegress = 13f,
            size = 90.dp,
            vertexNumber = 9,
            progress = progress,
            stroke = 16f,
            isInfinite = false,
            infiniteDelayInMillis = 1100,
        )
    }
}

```   


## Authenticator

Con esta clase, puedes controlar fácilmente la creación y actualización de cuentas de usuario en el sistema de Android AccountManager  

**Utilización**:

1. Crea tu propia clase YourOwnAuthenticatorService que hereda de Service.  

   1.1. **Sobreescribe el método onBind, con tu propia implementación de Authenticator**:  

```kotlin
        override fun onBind(intent: Intent?) = Authenticator(
               this,
               YourLoginActivity::class.java,
               R.string.your_account_type_name_resource_id,
               R.string.your_custom_message_on_single_account_error,
               R.string.your_custom_message_on_unsupported_account_error,
               R.string.your_custom_message_on_unsupported_token_error,
               R.string.your_custom_message_on_unsupported_features_error,
               true/false // true if only one account is allowed, false if app are designed to multiple accounts
        )
        
```  
        
 2. **Crea tu propia actividad LoginActivity. Esta actividad necesitará capturar algunos extras de su intent**:  
 
      2.1. En el método onCreate, captura algunos de los extras del intent, que nos servirán para preparar la creación o actualización de la cuenta desde la app o desde el AccountManager:  
      
        2.1.1. Instancia tu propio Authenticator (como en el punto 1.1).  
        
        4.1.2. Carga los datos necesarios desde this.intent.extras:  
                
```kotlin      
        this.fromApp = try{ (this.intent.extras!![Authenticator.ACTION_LOGIN_TYPE] as AuthenticatorLoginType) == AuthenticatorLoginType.App} catch(_: Exception){ false }
        this.loginUser = try{ this.intent.getSerializableExtra(Authenticator.KEY_ACCOUNT) as YourAppAccount<b>IUser</b> }catch(_: Exception { null }        
```     

        2.1.3. Normalmente en un alta, limpiarás todos los campos de login.  

        2.1.4. Normalmente, si actualizas los datos de un usuario, lo harás cargando this.loginUser (extraido en 2.1.2).  

        2.1.5. Si el login ha sido correcto, guardaremos la cuenta en el sistema de cuentas de Android AccountManager:  
            
```kotlin
       this.authenticator.saveUserAccount(this, R.string.your_account_type_name_resource_id, this.loginUser)            
```  

3. **En tu AndroidManifest.xml se necesita hacer algunas cosas**   
     3.1. Agrega algunos permisos:  
     
```xml

             <uses-permission android:name="android.permission.MANAGE_ACCOUNTS" />
             <uses-permission android:name="android.permission.AUTHENTICATE_ACCOUNTS" />
             <uses-permission android:name="android.permission.USE_CREDENTIALS" />
             <uses-permission android:name="android.permission.GET_ACCOUNTS" />
             <uses-permission android:name="android.permission.READ_PROFILE" />
```   
     
     3.2. Agrega una referencia a tu servicio YourOwnAuthenticatorService en la sección <application>   
     
```xml
            <service android:name=".auth.YourOwnAuthenticator" exported="true">
                 <intent-filter>
                      <action android:name="android.accounts.AccountAuthenticator" />
                  </intent-filter>

                  <meta-data
                      android:name="android.accounts.AccountAuthenticator"
                      android:resource="@xml/authenticator" />
            </service>
  
```   
  
 4. **Agrega un recurso en la ruta xml/authenticator.xml, con el siguiente contenido**:   
  
```xml
  
      <xml version="1.0" encoding="UTF-8"?>

      <account-authenticator xmlns:android="http://schemas.android.com/apk/res/android"

              android:accountType="@string/your_account_type_name_resource_id"
              android:icon="@mipmap/ic_launcher"
              android:label="@string/app_name"
              android:smallIcon="@mipmap/ic_launcher">
      </account-authenticator>
        
```   
 
**¡ Y ESTO ES TODO!**   

Como nota final, si deseas incluir este proyecto en tus apps, en tu build.gradle sólo deberás agregar lo siguiente:   

```
implementation 'io.github.afalabarce:jetpackcompose:1.3.3'
```   

Si piensas que me merezco un café, puedes hacerme un [PayPalMe](https://www.paypal.com/paypalme/afalabarce)
