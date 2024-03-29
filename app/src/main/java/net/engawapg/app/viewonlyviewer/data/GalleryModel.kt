package net.engawapg.app.viewonlyviewer.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.compose.runtime.Stable
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

@Stable
data class GalleryItem(
    val uri: Uri,
    val isVideo: Boolean,
    val height: Int,
    val width: Int,
)

@Stable
data class FolderItem(
    val name: String,
    val id: Int = 0,
    val path: String = "/path/to/folder",
    val thumbnailUri: Uri = Uri.parse("content://media/external/file/22"),
)

class GalleryModel(private val context: Context) {
    val galleryItemsFlow = MutableSharedFlow<List<GalleryItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun load(ignoreFolderIds: Set<String> = setOf()) {
        val list = mutableListOf<GalleryItem>()

        val contentUri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.PARENT,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.WIDTH,
        )
        /* Question marks for the number of the set size */
        val questions = ignoreFolderIds.joinToString { "?" }
        val selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " in (?,?) " +
                " AND " +
                MediaStore.Files.FileColumns.PARENT + " not in ($questions) "
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        ) + ignoreFolderIds.toTypedArray()
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        context.contentResolver.query(
            contentUri, projection, selection, selectionArgs, sortOrder
        )?.use { cursor -> /* cursorは、検索結果の各行の情報にアクセスするためのオブジェクト。*/
            /* 必要な情報が格納されている列番号を取得する。 */
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val mediaTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)

            /* 順にカーソルを動かしながら、情報を取得していく。*/
            while (cursor.moveToNext()) {
                /* IDからURIを取得 */
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(contentUri, id)
                /* MediaTypeを取得 */
                val mediaType = cursor.getInt(mediaTypeCol)
                val isVideo = (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
                /* Get image size */
                val height = cursor.getInt(heightCol)
                val width = cursor.getInt(widthCol)

                list.add(GalleryItem(uri, isVideo, height, width))
            }
        }
        galleryItemsFlow.tryEmit(list)
    }

    val folderItemsFlow = MutableSharedFlow<List<FolderItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun loadFolders() {
        val ctx = context
        val list = mutableListOf<FolderItem>()

        val contentUri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.PARENT,
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.MediaColumns.DATA,
        )
        val selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " in (?, ?) "
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        )
        val sortColumns = arrayOf(
            MediaStore.Files.FileColumns.PARENT,
            MediaStore.Files.FileColumns.DATE_ADDED,
        )
        val queryArgs = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, sortColumns)
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
        }

        ctx.contentResolver.query(
            contentUri, projection, queryArgs, null
        )?.use { cursor ->
            /* Get column index */
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val parentCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.PARENT)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            var prevParent = -1

            /* Get information from cursor */
            while (cursor.moveToNext()) {
                /* Add the newest item of the same parent */
                val parent = cursor.getInt(parentCol)
                if (prevParent != parent) {
                    prevParent = parent

                    val id = cursor.getLong(idCol)
                    val imageUri = ContentUris.withAppendedId(contentUri, id)
                    val imagePath = cursor.getString(dataCol)
                    val directoryPath = imagePath.substringBeforeLast('/')
                    val directoryName = directoryPath.substringAfterLast('/')
                    val parentPath = directoryPath.substringBeforeLast('/')
                    val item = FolderItem(
                        id = parent,
                        name = directoryName,
                        path = parentPath,
                        thumbnailUri = imageUri
                    )
                    list.add(item)
//                    Log.d("GalleryModel", item.toString())
                }
            }
        }
        folderItemsFlow.tryEmit(list)
    }
}