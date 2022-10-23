package net.engawapg.app.viewonlyviewer.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import net.engawapg.app.viewonlyviewer.LocalNavController
import net.engawapg.app.viewonlyviewer.R

@Composable
fun SettingFolderScreen(viewModel: SettingFolderViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    SettingFolderContent(
        uiState = uiState,
        onChangeFolderVisibility = { folderId, visibility ->
            viewModel.setFolderVisibility(folderId, visibility)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingFolderContent(
    uiState: SettingFolderUiState,
    onChangeFolderVisibility: (Int, Boolean) -> Unit,
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
                title = { Text(stringResource(R.string.setting_title_folder)) },
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
        LazyColumn(contentPadding = innerPadding) {
            if (uiState is SettingFolderUiState.Loaded) {
                item { SettingFolderDescription() }
                items(uiState.settingFolderItems) { folder ->
                    SettingFolderCell(
                        folder = folder
                    ) { checked ->
                        onChangeFolderVisibility(folder.id, checked)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingFolderDescription() {
    Text(
        text = stringResource(id = R.string.setting_desc_folder),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier
            .padding(
                horizontal = SettingsScreenTokens.ItemPaddingStart,
                vertical = SettingsScreenTokens.ItemPaddingVertical
            )
    )
}

@Composable
fun SettingFolderCell(folder: SettingFolderItem, onCheckedChange: (Boolean) -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SettingsScreenTokens.ItemPaddingStart,
                    vertical = SettingsScreenTokens.ItemPaddingVertical
                )
        ) {
            AsyncImage(
                model = folder.thumbnailUri,
                contentDescription = "Thumbnail of the folder",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .size(80.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = folder.parentPath,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Checkbox(
                checked = folder.visibility,
                onCheckedChange = onCheckedChange,
            )
        }
        Divider(
            color = MaterialTheme.colorScheme.outline,
            thickness = Dp.Hairline,
        )
    }
}
