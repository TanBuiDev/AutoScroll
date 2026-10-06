package com.personal.autoscroll.ui.automation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.autoscroll.R

@Composable
fun AppChooserDialog(viewModel: AutomationViewModel, onDismiss: () -> Unit, onSelect: (SelectableApp) -> Unit) {
    val apps by viewModel.installedApps.collectAsStateWithLifecycle()
    val loading by viewModel.appsLoading.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_app)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = search, onValueChange = { search = it }, singleLine = true,
                    label = { Text(stringResource(R.string.search_apps)) })
                if (loading) CircularProgressIndicator()
                val filtered = apps.filter { it.name.contains(search, ignoreCase = true) }
                if (!loading && filtered.isEmpty()) Text(stringResource(R.string.no_matching_apps))
                LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    items(filtered, key = { it.packageName }) { app ->
                        TextButton(onClick = { onSelect(app) }, modifier = Modifier.fillMaxWidth()) { Text(app.name) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
