package net.engawapg.app.viewonlyviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingFolderScreen(viewModel: SettingFolderViewModel = viewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarScrollState())
    val statusBarColor = TopAppBarDefaults.centerAlignedTopAppBarColors()
        .containerColor(scrollFraction = scrollBehavior.scrollFraction).value
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(statusBarColor)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val navController = LocalNavController.current
            SmallTopAppBar(
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
            item { SettingFolderDescription() }
            items(viewModel.uiState.folderList) { folder ->
                SettingFolderCell(folder)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingFolderCell(folder: FolderItem) {
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
                    text = folder.path,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Checkbox(
                checked = true,
                onCheckedChange = {},
            )
        }
        Divider(
            color = MaterialTheme.colorScheme.outline,
            thickness = Dp.Hairline,
        )
    }
}
