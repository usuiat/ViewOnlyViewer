package net.engawapg.app.viewonlyviewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

enum class SettingsScreenEvent {
    SelectBack
}

internal object SettingsScreenTokens {
    val ItemPaddingStart = 20.dp
    val ItemPaddingVertical = 8.dp
    val HeaderPaddingTop = 28.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onEvent: (SettingsScreenEvent)->Unit = {}) {
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    val statusBarColor = TopAppBarDefaults.centerAlignedTopAppBarColors()
        .containerColor(scrollFraction = scrollBehavior.scrollFraction).value
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(statusBarColor)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SmallTopAppBar(
                title = { Text(stringResource(R.string.settings))},
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(SettingsScreenEvent.SelectBack) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back),
                        )
                    }
                }
            )
        }
    ) {
        SettingsList()
    }
}

@Composable
fun SettingsList() {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            SettingsHeader(title = stringResource(id = R.string.setting_header_childproof))
            SettingCellTapCountToOpenSettings()
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .padding(
                start = SettingsScreenTokens.ItemPaddingStart,
                top = SettingsScreenTokens.HeaderPaddingTop,
                bottom = SettingsScreenTokens.ItemPaddingVertical
            )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun SettingCellTapCountToOpenSettings() {
    val context = LocalContext.current
    val tapCount = SettingTapCountToOpenSettings.getState(context)
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    val options = listOf(
        stringResource(id = R.string.setting_value_tap_count_to_open_settings_1),
        stringResource(id = R.string.setting_value_tap_count_to_open_settings_2),
        stringResource(id = R.string.setting_value_tap_count_to_open_settings_3),
        stringResource(id = R.string.setting_value_tap_count_to_open_settings_4),
        stringResource(id = R.string.setting_value_tap_count_to_open_settings_5),
    )

    SettingItemCell(
        title = stringResource(id = R.string.setting_title_tap_count_to_open_settings),
        value = options[tapCount.value - 1],
        onClick = { showDialog = true }
    )

    if (showDialog) {
        RadioButtonDialog(
            title = stringResource(id = R.string.setting_title_tap_count_to_open_settings),
            text = stringResource(id = R.string.setting_desc_tap_count_to_open_settings),
            options = options,
            selected = tapCount.value - 1,
            onSelect = { selected ->
                scope.launch { SettingTapCountToOpenSettings.set(selected + 1, context) }
                showDialog = false
            },
        )
    }
}

@Composable
fun SettingItemCell(
    title: String,
    value: String,
    onClick: ()->Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(
                SettingsScreenTokens.ItemPaddingStart,
                SettingsScreenTokens.ItemPaddingVertical
            )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RadioButtonDialog(
    title: String,
    text: String,
    options: List<String>,
    selected: Int,
    onSelect:(Int)->Unit,
) {
    AlertDialog(
        onDismissRequest = { onSelect(selected) }, // No change
        confirmButton = {},
        modifier = Modifier
            .fillMaxWidth(0.9f),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(Modifier.selectableGroup()) {
                Text(
                    text = text,
                )
                options.forEachIndexed { index, option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (index == selected),
                                onClick = {
                                    onSelect(index)
                                },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (index == selected),
                            onClick = null
                        )
                        Text(
                            text = option,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}
