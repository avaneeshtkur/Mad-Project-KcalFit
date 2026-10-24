package com.kcalfit.app.ui.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kcalfit.app.domain.activity.ActivityCatalog
import com.kcalfit.app.domain.activity.ActivityCategory
import com.kcalfit.app.domain.activity.ActivityType

/** Search field and category chips; the matching rows come from [activityPickerItems]. */
@Composable
fun ActivitySearchControls(query: String, onQuery: (String) -> Unit, category: ActivityCategory?,
    onCategory: (ActivityCategory?) -> Unit) {
    val focus = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(query, onQuery, Modifier.fillMaxWidth(), label = { Text("Search activities") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = { if (query.isNotEmpty()) IconButton({ onQuery("") }) { Icon(Icons.Default.Close, "Clear search") } },
            singleLine = true, shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }))
        // A scrolling chip row keeps every category reachable on narrow screens and large text.
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(category == null, { onCategory(null) }, label = { Text("All") }) }
            items(ActivityCategory.entries) { entry ->
                FilterChip(category == entry, { onCategory(if (category == entry) null else entry) }, label = { Text(entry.label) })
            }
        }
    }
}

/** Popular activities first when browsing, ranked matches when searching. */
fun LazyListScope.activityPickerItems(query: String, category: ActivityCategory?, onSelect: (ActivityType) -> Unit,
    onClear: () -> Unit) {
    val groups = if (query.isBlank()) ActivityCatalog.browse(category)
        else listOf("Results" to ActivityCatalog.search(query, category)).filter { it.second.isNotEmpty() }
    if (groups.isEmpty()) {
        item(key = "activity_empty") {
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("No activities match “${query.trim()}”${category?.let { " in ${it.label}" } ?: ""}.",
                    style = MaterialTheme.typography.bodyLarge)
                Text("Try a shorter or different word, or browse a category.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClear) { Text("Clear search") }
            }
        }
        return
    }
    groups.forEach { (title, activities) ->
        item(key = "activity_header_$title") {
            Text(title, Modifier.padding(top = 4.dp), style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)
        }
        items(activities, key = { "activity_${title}_${it.id}" }) { activity ->
            ActivityRow(activity, showCategory = query.isNotBlank() || title == "Popular") { onSelect(activity) }
        }
    }
}

@Composable
private fun ActivityRow(activity: ActivityType, showCategory: Boolean, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.heightIn(min = 56.dp).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(activity.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(if (showCategory) "${activity.category.label} · ${activity.tracking.summary}" else activity.tracking.summary,
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SelectedActivity(activity: ActivityType, onChange: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Selected: ${activity.name}", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(activity.category.label, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            TextButton(onChange) { Text("Change") }
        }
    }
}
