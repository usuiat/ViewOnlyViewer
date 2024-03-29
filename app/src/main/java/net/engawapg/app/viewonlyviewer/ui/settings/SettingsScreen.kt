package net.engawapg.app.viewonlyviewer.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.lifecycle.viewmodel.compose.viewModel
import net.engawapg.app.viewonlyviewer.BuildConfig
import net.engawapg.app.viewonlyviewer.LocalNavController
import net.engawapg.app.viewonlyviewer.R
import net.engawapg.app.viewonlyviewer.data.ColorThemeSetting
import net.engawapg.app.viewonlyviewer.data.DarkThemeSetting
import net.engawapg.app.viewonlyviewer.util.findActivity

private const val URL_PRIVACY_POLICY = "https://engawapg.net/software/viewonlyviewer/viewonlyviewer-privacy-policy/"

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsContent(
        uiState = uiState,
        onChangeDarkTheme = { darkTheme -> viewModel.setDarkTheme(darkTheme) },
        onChangeColorTheme = { colorTheme -> viewModel.setColorTheme(colorTheme) },
        onChangeTapCountToOpenSettings = { tapCount -> viewModel.setTapCountToOpenSettings(tapCount) },
        onChangeMultiGoBack = { multiGoBack -> viewModel.setMultiGoBack(multiGoBack) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onChangeDarkTheme: (DarkThemeSetting) -> Unit,
    onChangeColorTheme: (ColorThemeSetting) -> Unit,
    onChangeTapCountToOpenSettings: (Int) -> Unit,
    onChangeMultiGoBack: (Int) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
        topBar = {
            val navController = LocalNavController.current
            TopAppBar(
                title = { Text(stringResource(R.string.settings))},
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back),
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState is SettingsUiState.Success) {
            SettingsList(
                darkTheme = uiState.darkTheme,
                onChangeDarkTheme = onChangeDarkTheme,
                colorTheme = uiState.colorTheme,
                onChangeColorTheme = onChangeColorTheme,
                tapCountToOpenSettings = uiState.tapCountToOpenSettings,
                onChangeTapCountToOpenSettings = onChangeTapCountToOpenSettings,
                multiGoBack = uiState.multiGoBack,
                onChangeMultiGoBack = onChangeMultiGoBack,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
fun SettingsList(
    darkTheme: DarkThemeSetting,
    onChangeDarkTheme: (DarkThemeSetting) -> Unit,
    colorTheme: ColorThemeSetting,
    onChangeColorTheme: (ColorThemeSetting) -> Unit,
    tapCountToOpenSettings: Int,
    onChangeTapCountToOpenSettings: (Int) -> Unit,
    multiGoBack: Int,
    onChangeMultiGoBack: (Int) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxWidth()
    ) {
        item { SettingsHeader(title = stringResource(id = R.string.setting_header_display)) }
        item { SettingCellFolder() }

        item { SettingsHeader(title = stringResource(id = R.string.setting_header_childproof)) }
        item {
            SettingCellTapCountToOpenSettings(
                tapCountToOpenSettings = tapCountToOpenSettings,
                onChangeTapCountToOpenSettings = onChangeTapCountToOpenSettings,
            )
        }
        item {
            SettingCellMultiGoBack(
                multiGoBack = multiGoBack,
                onChangeMultiGoBack = onChangeMultiGoBack,
            )
        }

        item { SettingsHeader(title = stringResource(id = R.string.setting_header_theme)) }
        item {
            SettingCellDarkTheme(
                darkTheme = darkTheme,
                onChangeDarkTheme = onChangeDarkTheme,
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            item {
                SettingCellColorTheme(
                    colorTheme = colorTheme,
                    onChangeColorTheme = onChangeColorTheme,
                )
            }
        }

        item { SettingsHeader(title = stringResource(id = R.string.setting_header_about)) }
        item { SettingCellVersion() }
        item { SettingPrivacyPolicy() }
    }
}

@Composable
fun SettingCellFolder() {
    val navController = LocalNavController.current
    SettingItemCell(
        title = stringResource(id = R.string.setting_title_folder),
        value = stringResource(id = R.string.setting_desc_folder),
        modifier = Modifier.clickable { navController.navigate("setting_folder") },
    )
}

@Composable
fun SettingCellTapCountToOpenSettings(
    tapCountToOpenSettings: Int,
    onChangeTapCountToOpenSettings: (Int) -> Unit,
) {
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
        value = options[tapCountToOpenSettings - 1],
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        RadioButtonDialog(
            title = stringResource(id = R.string.setting_title_tap_count_to_open_settings),
            text = stringResource(id = R.string.setting_desc_tap_count_to_open_settings),
            options = options,
            selected = tapCountToOpenSettings - 1,
            onSelect = { selected ->
                onChangeTapCountToOpenSettings(selected + 1)
                showDialog = false
            },
        )
    }
}

@Composable
fun SettingCellMultiGoBack(
    multiGoBack: Int,
    onChangeMultiGoBack: (Int) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    val options = listOf(
        stringResource(id = R.string.setting_value_multi_go_back_1),
        stringResource(id = R.string.setting_value_multi_go_back_2),
        stringResource(id = R.string.setting_value_multi_go_back_3),
        stringResource(id = R.string.setting_value_multi_go_back_4),
        stringResource(id = R.string.setting_value_multi_go_back_5),
    )

    SettingItemCell(
        title = stringResource(id = R.string.setting_title_multi_go_back),
        value = options[multiGoBack - 1],
        modifier = Modifier.clickable { showDialog = true },
    )

    if (showDialog) {
        RadioButtonDialog(
            title = stringResource(id = R.string.setting_title_multi_go_back),
            text = stringResource(id = R.string.setting_desc_multi_go_back),
            options = options,
            selected = multiGoBack - 1,
            onSelect = { selected ->
                onChangeMultiGoBack(selected + 1)
                showDialog = false
            }
        )
    }
}

@Composable
fun SettingCellDarkTheme(
    darkTheme: DarkThemeSetting,
    onChangeDarkTheme: (DarkThemeSetting) -> Unit
) {
    val current = darkTheme.toInt()
    var showDialog by remember { mutableStateOf(false) }

    val options = listOf(
        stringResource(id = R.string.setting_value_off),
        stringResource(id = R.string.setting_value_on),
        stringResource(id = R.string.setting_value_use_system_settings),
    )

    SettingItemCell(
        title = stringResource(id = R.string.setting_title_darktheme),
        value = options[current],
        modifier = Modifier.clickable { showDialog = true },
    )

    if (showDialog) {
        RadioButtonDialog(
            title = stringResource(id = R.string.setting_title_darktheme),
            options = options,
            selected = current,
            onSelect = { selected ->
                onChangeDarkTheme(DarkThemeSetting.fromInt(selected))
                showDialog = false
            }
        )
    }
}

@Composable
fun SettingCellColorTheme(
    colorTheme: ColorThemeSetting,
    onChangeColorTheme: (ColorThemeSetting) -> Unit,
) {
    val current = colorTheme.toInt()
    var showDialog by remember { mutableStateOf(false) }

    val options = listOf(
        stringResource(id = R.string.setting_value_app_colors),
        stringResource(id = R.string.setting_value_use_wallpaper_colors),
    )

    SettingItemCell(
        title = stringResource(id = R.string.setting_title_colortheme),
        value = options[current],
        modifier = Modifier.clickable { showDialog = true },
    )

    if (showDialog) {
        RadioButtonDialog(
            title = stringResource(id = R.string.setting_title_colortheme),
            options = options,
            selected = current,
            onSelect = { selected ->
                onChangeColorTheme(ColorThemeSetting.fromInt(selected))
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
fun SettingPrivacyPolicy() {
    val activity = LocalContext.current.findActivity()
    SettingItemCell(
        title = stringResource(id = R.string.setting_title_privacy_policy),
        value = stringResource(id = R.string.setting_value_open_in_browser),
        modifier = Modifier.clickable {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(URL_PRIVACY_POLICY))
            activity.startActivity(webIntent)
        }
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RadioButtonDialog(
    title: String,
    text: String = "",
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
