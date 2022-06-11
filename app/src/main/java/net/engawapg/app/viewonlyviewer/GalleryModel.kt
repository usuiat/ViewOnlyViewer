package net.engawapg.app.viewonlyviewer

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Stable

@Stable
data class GalleryItem(val uri: Uri, val isVideo: Boolean)

class GalleryModel {
    private var _items = listOf<GalleryItem>()
    val items get() = _items

    fun load(ctx: Context) {
        val list = mutableListOf<GalleryItem>()

        val contentUri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
        )
        val selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +
                " OR " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        val selectionArgs = null
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        ctx.contentResolver.query(
            contentUri, projection, selection, selectionArgs, sortOrder
        )?.use { cursor -> /* cursorは、検索結果の各行の情報にアクセスするためのオブジェクト。*/
            /* 必要な情報が格納されている列番号を取得する。 */
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val mediaTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            /* 順にカーソルを動かしながら、情報を取得していく。*/
            while (cursor.moveToNext()) {
                /* IDからURIを取得 */
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(contentUri, id)
                /* MediaTypeを取得 */
                val mediaType = cursor.getInt(mediaTypeCol)
                val isVideo = (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

                list.add(GalleryItem(uri, isVideo))
//                Log.d("GalleryModel", "$uri $isVideo")
            }
        }

        _items = list
    }
}