package net.engawapg.app.viewonlyviewer

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import net.engawapg.app.viewonlyviewer.ui.theme.ViewOnlyViewerTheme

enum class SettingsScreenEvent {
    SelectBack
}

val groupedSettingKeys = listOf(
    "Category1" to listOf("TapCountToOpenSettings", "Test", "Test"),
    "Category2" to listOf("Test", "Test", "Test", "Test"),
)
val settingMap = mutableMapOf<String, Any>(
    "TapCountToOpenSettings" to 3,
    "Test" to "TestValue",
)

internal object SettingsScreenTokens {
//    val ItemKeyStyle = MaterialTheme.typography.titleLarge
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
        SettingsList(groupedSettingKeys)
    }
}

@Composable
fun SettingsList(groupedKeys: List<Pair<String, List<String>>>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        for (group in groupedKeys) {
            item {
                SettingsHeader(group.first)
            }
            items(group.second) { key ->
                when (val value = settingMap[key]) {
                    null -> Unit
                    is Boolean -> Unit
                    else -> SettingItemCell(key = key, value)
                }
            }
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
fun SettingItemCell(key: String, value: Any) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(
                SettingsScreenTokens.ItemPaddingStart,
                SettingsScreenTokens.ItemPaddingVertical
            )
        ) {
            Text(
                text = key,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Preview(
    name = "Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    widthDp = 360,
    heightDp = 200,
)
@Preview(
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 360,
    heightDp = 200,
)
@Composable
fun SettingPreview() {
    ViewOnlyViewerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsList(groupedSettingKeys)
        }
    }
}