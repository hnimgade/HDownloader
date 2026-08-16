package com.hdownloader.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hdownloader.core.category.model.Category
import com.hdownloader.core.network.UrlParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUrlDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onAdd: (url: String, categoryId: Long?, startImmediately: Boolean) -> Unit,
) {
    var url by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()) }
    var multiThread by remember { mutableStateOf(true) }
    var wifiOnly by remember { mutableStateOf(false) }
    var startImmediately by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Add download",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = url,
                onValueChange = {
                    url = it
                    error = null
                },
                label = { Text("URL") },
                placeholder = { Text("https://example.com/file.zip") },
                singleLine = true,
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = url.substringAfterLast('/').substringBefore('?').ifBlank { "" },
                onValueChange = {},
                label = { Text("Filename") },
                singleLine = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            CategoryDropdown(
                categories = categories,
                selected = selectedCategory,
                onSelect = { selectedCategory = it },
            )
            Spacer(modifier = Modifier.height(16.dp))

            SettingRow(
                title = "Multi-thread download",
                subtitle = "Split the file into parallel connections",
                checked = multiThread,
                onCheckedChange = { multiThread = it },
            )
            SettingRow(
                title = "Wi-Fi only",
                subtitle = "Pause when not on Wi-Fi",
                checked = wifiOnly,
                onCheckedChange = { wifiOnly = it },
            )
            SettingRow(
                title = "Start immediately",
                subtitle = "Begin download as soon as added",
                checked = startImmediately,
                onCheckedChange = { startImmediately = it },
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                OutlinedButton(
                    onClick = { onAdd(url, selectedCategory?.id, startImmediately) },
                    enabled = validateUrl(url, onInvalid = { error = it }),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Add download")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onAdd(url, selectedCategory?.id, true) },
                    enabled = validateUrl(url, onInvalid = { error = it }),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Start")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<Category>,
    selected: Category?,
    onSelect: (Category?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selected?.name ?: "No category",
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("No category") },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onSelect(category)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun validateUrl(url: String, onInvalid: (String) -> Unit): Boolean {
    if (url.isBlank()) {
        onInvalid("Enter a URL")
        return false
    }
    if (!UrlParser.isValid(url)) {
        onInvalid("That does not look like a valid http(s) URL")
        return false
    }
    return true
}
