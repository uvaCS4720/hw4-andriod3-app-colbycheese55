package edu.nd.pmcburne.hello

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.nd.pmcburne.hello.ui.theme.MyApplicationTheme
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MapViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    MainScreen(viewModel, modifier = Modifier.padding(innerPadding))
                    viewModel.loadData()
                    Screen(viewModel)
                }
            }
        }
    }
}



@Composable
fun Screen(vm: MapViewModel) {
    val landmarks by vm.landmarks.collectAsState()
    val tags by vm.tags.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // allow scrolling if content is long
        verticalArrangement = Arrangement.spacedBy(16.dp) // space between children
    ) {
        Text(
            text = "UVA Landmark Viewer",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        SelectionDropdown(
            tags = tags,
            onChange = { tag, state -> vm.setTag(tag, state) }
        )

        // Map takes remaining space
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GoogleMap(
                landmarks = landmarks,
                tags = tags
            )
        }
    }
}

@Composable
fun SelectionDropdown(
    tags: Map<String, Boolean>,
    onChange: (String, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sortedTags = tags.toSortedMap()

    Box(modifier = Modifier.fillMaxWidth()) {
        // Full-width trigger button
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = "Select tags",
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                sortedTags.forEach { (tag, selected) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChange(tag, !selected) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = selected,
                            onCheckedChange = { state -> onChange(tag, state) }
                        )
                        Text(
                            text = tag,
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun GoogleMap(landmarks: List<Landmark>, tags: Map<String, Boolean>) {
    val filteredLandmarks = landmarks.filter { l ->
        l.tag_list.any { tag -> tags[tag] == true}
    }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(filteredLandmarks) {
        if (filteredLandmarks.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            filteredLandmarks.forEach { landmark ->
                boundsBuilder.include(LatLng(landmark.latitude, landmark.longitude))
            }
            val bounds = boundsBuilder.build()
            // Animate camera to fit all markers with some padding (in pixels)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        filteredLandmarks.forEach { landmark ->
            Marker(
                state = MarkerState(
                    position = LatLng(landmark.latitude, landmark.longitude)
                ),
                title = landmark.name,
                snippet = landmark.description
            )
        }
    }
}