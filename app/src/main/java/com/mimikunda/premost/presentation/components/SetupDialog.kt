package com.mimikunda.premost.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mimikunda.premost.R
import com.mimikunda.premost.data.StreamConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupDialog(
    initialConfig: StreamConfig?,
    onSave: (url: String, referer: String, origin: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var url by remember(initialConfig) { mutableStateOf(initialConfig?.url ?: "") }
    var referer by remember(initialConfig) { mutableStateOf(initialConfig?.referer ?: "") }
    var origin by remember(initialConfig) { mutableStateOf(initialConfig?.origin ?: "") }
    var isAdvancedExpanded by remember { mutableStateOf(referer.isNotBlank() || origin.isNotBlank()) }

    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }

    val isUrlValid = remember(url) {
        val trimmed = url.trim()
        trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (initialConfig == null) stringResource(R.string.setup_dialog_title_new) else stringResource(R.string.setup_dialog_title_edit),
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(stringResource(R.string.setup_dialog_url_label)) },
                placeholder = { Text(stringResource(R.string.setup_dialog_url_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            clipboardManager.getText()?.text?.let { pastedText ->
                                url = pastedText
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = stringResource(R.string.setup_dialog_paste_content_desc)
                        )
                    }
                },
                isError = url.isNotBlank() && !isUrlValid,
                supportingText = {
                    if (url.isNotBlank() && !isUrlValid) {
                        Text(stringResource(R.string.setup_dialog_url_error))
                    } else {
                        Text(stringResource(R.string.setup_dialog_url_helper))
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isUrlValid) {
                            onSave(url.trim(), referer.trim(), origin.trim())
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAdvancedExpanded = !isAdvancedExpanded }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.setup_dialog_advanced_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isAdvancedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isAdvancedExpanded) stringResource(R.string.setup_dialog_collapse_content_desc) else stringResource(R.string.setup_dialog_expand_content_desc),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isAdvancedExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = referer,
                        onValueChange = { referer = it },
                        label = { Text(stringResource(R.string.setup_dialog_referer_label)) },
                        placeholder = { Text(stringResource(R.string.setup_dialog_referer_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = origin,
                        onValueChange = { origin = it },
                        label = { Text(stringResource(R.string.setup_dialog_origin_label)) },
                        placeholder = { Text(stringResource(R.string.setup_dialog_origin_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isUrlValid) {
                        onSave(url.trim(), referer.trim(), origin.trim())
                    }
                },
                enabled = isUrlValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.setup_dialog_save_button),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
