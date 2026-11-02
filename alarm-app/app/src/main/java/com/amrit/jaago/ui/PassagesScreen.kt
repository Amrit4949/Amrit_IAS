package com.amrit.jaago.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amrit.jaago.model.AlarmStore

@Composable
fun PassagesScreen(onBack: () -> Unit) {
    val passages by AlarmStore.customPassages.collectAsState()
    var draft by remember { mutableStateOf("") }
    var useCustom by remember { mutableStateOf(AlarmStore.useCustomPassages) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("My passages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard(
                    title = "Use my own passages",
                    subtitle = "When this is on, typing challenges use your paragraphs instead of the built in ones."
                ) {
                    SwitchRow(
                        title = "Prefer my passages",
                        subtitle = if (passages.isEmpty()) {
                            "Add at least one below first"
                        } else {
                            "${passages.size} saved"
                        },
                        checked = useCustom,
                        onCheckedChange = {
                            useCustom = it
                            AlarmStore.useCustomPassages = it
                        }
                    )
                }
            }
            item {
                SectionCard(
                    title = "Add a passage",
                    subtitle = "At least 20 characters. Stick to plain letters and punctuation you can reach on the keyboard."
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it.take(600) },
                        label = { Text("Paste or write a paragraph") },
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Button(
                        onClick = {
                            AlarmStore.addPassage(draft)
                            draft = ""
                        },
                        enabled = draft.trim().length >= 20,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save passage")
                    }
                }
            }
            items(passages) { passage ->
                SectionCard(title = "${passage.length} characters") {
                    Text(passage, style = MaterialTheme.typography.bodySmall)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = { AlarmStore.removePassage(passage) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete passage")
                        }
                    }
                }
            }
            item {
                Column(modifier = Modifier.height(48.dp)) { }
            }
        }
    }
}
