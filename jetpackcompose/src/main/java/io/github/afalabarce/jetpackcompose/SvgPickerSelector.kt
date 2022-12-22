package io.github.afalabarce.jetpackcompose

import android.os.Environment
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.*
import java.util.*

private class SvgPickerViewModel(private val svgIconsPaths: List<String> = listOf(
    "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}${Environment.DIRECTORY_PICTURES}",
    "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}${Environment.DIRECTORY_DOWNLOADS}"
)): ViewModel(){
    private val svgIconsPathsWatcher = this.svgIconsPaths.map { path ->
        File(path).toPath()
    }
    private val pathWatcherService: WatchService = FileSystems.getDefault().newWatchService()
    private val _pictures by lazy { MutableStateFlow<List<ByteArray>>(listOf()) }
    val pictures: StateFlow<List<ByteArray>>
        get() = this._pictures

    init {
        this.svgIconsPathsWatcher.forEach { watcherPath ->
            watcherPath.register(
                this.pathWatcherService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
            )
        }

        this._pictures.update {
            this@SvgPickerViewModel.svgIconsPathsWatcher.flatMap { path ->
                path.toFile().listFiles()?.toList() ?: listOf()
            }.filter { f -> f.extension.lowercase(Locale.getDefault()) == "svg" }.map { svg ->
                svg.readBytes()
            }.distinct()
        }

    }

    fun refreshWatcher(){
        viewModelScope.launch(Dispatchers.IO) {
            this@SvgPickerViewModel.folderWatcher { svgPictures ->
                this@SvgPickerViewModel._pictures.update {
                    svgPictures.filter { f -> f.extension.lowercase(Locale.getDefault()) == "svg" }.map { svg ->
                        svg.readBytes()
                    }
                }
            }
        }
    }

    private suspend fun folderWatcher(onWatch: (List<File>) -> Unit) {
        withContext(Dispatchers.IO) {
            while (true) {
                val key = this@SvgPickerViewModel.pathWatcherService.take()

                key.pollEvents().filter { evt ->
                    listOf(StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY).any { x ->
                        x == evt.kind()
                    } && evt is WatchEvent<*>
                } .forEach { _ ->
                    onWatch(
                        this@SvgPickerViewModel.svgIconsPathsWatcher.flatMap { path ->
                            path.toFile().listFiles()?.toList() ?: listOf()
                        }
                    )
                    key.reset()

                    return@forEach
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SvgPickerSelector(
    modifier: Modifier = Modifier,
    thumbnailSize: Dp = 128.dp,
    @StringRes giveMeMoreIconsTitle: Int = R.string.give_me_more_icons,
    svgIconsPaths: List<String> = listOf(
        "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}${Environment.DIRECTORY_PICTURES}",
        "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}${Environment.DIRECTORY_DOWNLOADS}"
    ),
    thumbnailPadding: PaddingValues = PaddingValues(4.dp),
    onClickGiveMoreIcons: () -> Unit,
    onClickedItem: (ByteArray) -> Unit
){
    val context = LocalContext.current
    val viewModel by remember{ mutableStateOf(SvgPickerViewModel(svgIconsPaths)) }
    val grantedPermission = rememberPermissionState(permission = android.Manifest.permission.READ_EXTERNAL_STORAGE)
    if (grantedPermission.status != PermissionStatus.Granted){
        LaunchedEffect(key1 = "QueryReadStoragePermission",){
            grantedPermission.launchPermissionRequest()
        }
    }else{
        viewModel.refreshWatcher()
    }

    val svgPictures by viewModel.pictures.collectAsState()

    Column(
        modifier = modifier.fillMaxHeight(0.75f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            onClick = onClickGiveMoreIcons,
        ) {
            Text(text = stringResource(id = giveMeMoreIconsTitle))
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            modifier = modifier.fillMaxSize(),
            columns = GridCells.Adaptive(thumbnailSize),
            contentPadding = thumbnailPadding,
            state = rememberLazyGridState()
        ){
            items(svgPictures){ picture ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(picture)
                        .decoderFactory(SvgDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(thumbnailSize)
                        .padding(4.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onBackground,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            onClickedItem(picture)
                        }
                )
            }
        }
    }
}