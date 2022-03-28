package net.engawapg.app.viewonlyviewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter

private const val COLUMN_NUM = 4

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Gallery(items: List<GalleryItem>, onItemSelected: (Int)->Unit = {}) {
    if (items.isNotEmpty()) {
        LazyVerticalGrid(cells = GridCells.Fixed(COLUMN_NUM)) {
            itemsIndexed(items) { index, item ->
                GalleryItem(item = item, onSelected = { onItemSelected(index) })
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun GalleryItem(item: GalleryItem, onSelected: ()->Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .clickable { onSelected() }
    ) {

        /* Videoは白いアイコンをオーバーレイするので、見えやすいようにサムネイル画像を少し暗くする */
        val filter = if (item.isVideo) {
            ColorFilter.tint(Color(0xffdddddd), BlendMode.Multiply)
        } else {
            null
        }

        Image(
            painter = rememberImagePainter(item.uri),
            contentDescription = "Image",
            contentScale = ContentScale.Crop,
            colorFilter = filter
        )

        if (item.isVideo) {
            Image(
                painter = painterResource(id = R.drawable.videoindicator),
                contentDescription = "videoIndicator",
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            )
        }
    }
}
