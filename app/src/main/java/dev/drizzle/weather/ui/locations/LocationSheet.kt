package dev.drizzle.weather.ui.locations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.drizzle.weather.domain.model.SavedLocation
import dev.drizzle.weather.domain.model.SearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSheet(
    onDismiss: () -> Unit,
    viewModel: LocationsViewModel = viewModel(factory = LocationsViewModel.Factory),
) {
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val searching by viewModel.searching.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        if (adding) {
            AddLocationContent(
                query = query,
                results = results,
                searching = searching,
                onQueryChange = viewModel::onQueryChange,
                onBack = {
                    adding = false
                    viewModel.onQueryChange("")
                },
                onPick = {
                    viewModel.add(it)
                    viewModel.onQueryChange("")
                    onDismiss()
                },
            )
        } else {
            SavedLocationsContent(
                locations = locations,
                onSelect = {
                    viewModel.select(it)
                    onDismiss()
                },
                onDelete = viewModel::delete,
                onAdd = { adding = true },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedLocationsContent(
    locations: List<SavedLocation>,
    onSelect: (SavedLocation) -> Unit,
    onDelete: (SavedLocation) -> Unit,
    onAdd: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(
            text = "Locations",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
            items(locations, key = { it.id }) { location ->
                ListItem(
                    headlineContent = { Text(location.name) },
                    supportingContent = if (location.isCurrentLocation) {
                        { Text("Using your location") }
                    } else {
                        null
                    },
                    leadingContent = {
                        Icon(
                            imageVector = if (location.isCurrentLocation) Icons.Filled.MyLocation else Icons.Filled.LocationOn,
                            contentDescription = null,
                        )
                    },
                    trailingContent = if (!location.isCurrentLocation) {
                        {
                            IconButton(onClick = { onDelete(location) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    } else {
                        null
                    },
                    modifier = Modifier.clickable { onSelect(location) },
                )
            }
        }
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("Add location") },
            leadingContent = { Icon(Icons.Filled.Add, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onAdd),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLocationContent(
    query: String,
    results: List<SearchResult>,
    searching: Boolean,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onPick: (SearchResult) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Row(
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = "Add location", style = MaterialTheme.typography.titleLarge)
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search for a city") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
        )

        if (searching) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
            items(results) { result ->
                val subtitle = listOfNotNull(result.admin1, result.country)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                ListItem(
                    headlineContent = { Text(result.name) },
                    supportingContent = if (subtitle.isNotBlank()) {
                        { Text(subtitle) }
                    } else {
                        null
                    },
                    modifier = Modifier.clickable { onPick(result) },
                )
            }
        }
    }
}
