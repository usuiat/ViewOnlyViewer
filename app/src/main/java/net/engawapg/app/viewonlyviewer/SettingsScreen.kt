package net.engawapg.app.viewonlyviewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

enum class SettingsScreenEvent {
    SelectBack
}

internal object SettingsScreenTokens {
    val ItemPaddingStart = 20.dp
    val ItemPaddingVertical = 16.dp
    val HeaderPaddingTop = 36.dp
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
        item { SettingsHeader(title = stringResource(id = R.string.setting_header_childproof)) }
        item { SettingCellTapCountToOpenSettings() }
        item { SettingCellMultiGoBack() }

        item { SettingsHeader(title = stringResource(id = R.string.setting_header_about)) }
        item { SettingCellVersion() }
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
        modifier = Modifier.clickable { showDialog = true }
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
fun SettingCellMultiGoBack() {
    val context = LocalContext.current
    val num = SettingMultiGoBack.getState(context)
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val options = listOf(
        stringResource(id = R.string.setting_value_multi_go_back_1),
        stringResource(id = R.string.setting_value_multi_go_back_2),
        stringResource(id = R.string.setting_value_multi_go_back_3),
        stringResource(id = R.string.setting_value_multi_go_back_4),
        stringResource(id = R.string.setting_value_multi_go_back_5),
    )

    SettingItemCell(
        title = stringResource(id = R.string.setting_title_multi_go_back),
        value = options[num.value - 1],
        modifier = Modifier.clickable { showDialog = true },
    )

    if (showDialog) {
        RadioButtonDialog(
            title = stringResource(id = R.string.setting_title_multi_go_back),
            text = stringResource(id = R.string.setting_desc_multi_go_back),
            options = options,
            selected = num.value - 1,
            onSelect = { selected ->
                scope.launch { SettingMultiGoBack.set(selected + 1, context) }
                showDialog = false
            }
        )
    }
}

@Composable
fun SettingCellVersion() {
    SettingItemCell(
        title = stringResource(id = R.string.setting_title_version),
        value = BuildConfig.VERSION_NAME,
    )
}

@Composable
fun SettingsHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier
                .padding(
                    start = SettingsScreenTokens.ItemPaddingStart,
                    top = SettingsScreenTokens.HeaderPaddingTop,
                ),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
fun SettingItemCell(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    start = SettingsScreenTokens.ItemPaddingStart,
                    top = SettingsScreenTokens.ItemPaddingVertical,
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(
                    start = SettingsScreenTokens.ItemPaddingStart,
                    bottom = SettingsScreenTokens.ItemPaddingVertical,
                )
            )
            Divider(
                color = MaterialTheme.colorScheme.outline,
                thickness = Dp.Hairline,
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
            Column(
                Modifier
                    .selectableGroup()
                    .verticalScroll(rememberScrollState())
            ) {
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
