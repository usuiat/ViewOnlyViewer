package net.engawapg.app.viewonlyviewer.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.Float.max
import kotlin.math.PI
import kotlin.math.abs

@Composable
fun rememberContentZoomState(
    minScale: Float = 1f,
    maxScale: Float,
): ContentZoomState {
    return remember {
        ContentZoomState(
            minScale = minScale,
            maxScale = maxScale,
        )
    }
}

@Stable
class ContentZoomState(
    private val minScale: Float,
    private val maxScale: Float,
) {
    private var _scale = Animatable(1f).apply {
        // Set lowerBound less than minScale so that animation can indicate the scale has reached
        // the minimum value.
        updateBounds(minScale * 0.9f, maxScale)
    }
    val scale: Float
        get() = _scale.value

    private var _offsetX = Animatable(0f)
    private var _offsetY = Animatable(0f)
    val offset: Offset
        get() = Offset(_offsetX.value, _offsetY.value)

    private var elementSize = Size.Zero // Size of the composable element.
    fun setElementSize(size: Size) {
        elementSize = size
    }

    private var contentSize = Size.Zero // Size of the inner content such as an image size.
    fun setContentSize(size: Size) {
        contentSize = size
    }

    private val velocityTracker = VelocityTracker()
    private var willFling = true // If true the fling operation will be executed.
    private var consumeEvent: Boolean? = null // If true the event can be consumed.

    fun startGesture() {
        // Reset if the event is consumed.
        consumeEvent = null
    }

    // If consumeEvent is null, judge if the event can be consumed.
    fun canConsumeGesture(pan: Offset, zoom: Float): Boolean {
        return consumeEvent ?: run {
            var consume = true
            if (zoom == 1f) { // One finger gesture
                if (scale == 1f) {  // Not zoomed
                    consume = false
                } else {
                    val ratio = (abs(pan.x) / abs(pan.y))
                    if (ratio > 5) {   // Horizontal drag
                        if ((pan.x < 0) && (_offsetX.value == _offsetX.lowerBound)) {
                            // Drag R to L when right edge of the content is shown.
                            consume = false
                        }
                        if ((pan.x > 0) && (_offsetX.value == _offsetX.upperBound)) {
                            // Drag L to R when left edge of the content is shown.
                            consume = false
                        }
                    }
                }
            }
            consumeEvent = consume
            consume
        }
    }

    suspend fun applyGesture(position: Offset, pan: Offset, zoom: Float, timeMillis: Long) {
        coroutineScope {
            launch {
                _scale.snapTo(_scale.value * zoom)
            }

            val scaledSize = contentSize * scale
            val boundX = max((scaledSize.width - elementSize.width), 0f) / 2f
            val boundY = max((scaledSize.height - elementSize.height), 0f) / 2f
            _offsetX.updateBounds(-boundX, boundX)
            _offsetY.updateBounds(-boundY, boundY)
            launch {
                _offsetX.snapTo(_offsetX.value + pan.x)
            }
            launch {
                _offsetY.snapTo(_offsetY.value + pan.y)
            }
            velocityTracker.addPosition(timeMillis, position)
        }

        if (zoom != 1f) {
            willFling = false
        }
    }

    suspend fun fling() = coroutineScope {
        if (willFling) {
            val velocity = velocityTracker.calculateVelocity()
            val decay = exponentialDecay<Float>(3f)
            launch {
                _offsetX.animateDecay(velocity.x, decay)
            }
            launch {
                _offsetY.animateDecay(velocity.y, decay)
            }
        }
        willFling = true

        if (scale < 1f) {
            launch {
                _scale.animateTo(1f)
            }
        }
    }
}

suspend fun PointerInputScope.detectTransformGesturesWithoutConsuming(
    panZoomLock: Boolean = false,
    onGestureStart: () -> Unit = {},
    onGestureEnd: () -> Unit = {},
    onGesture: (event: PointerEvent, centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit
) {
    forEachGesture {
        awaitPointerEventScope {
            var rotation = 0f
            var zoom = 1f
            var pan = Offset.Zero
            var pastTouchSlop = false
            val touchSlop = viewConfiguration.touchSlop
            var lockedToPanZoom = false

            awaitFirstDown(requireUnconsumed = false)
            onGestureStart()
            do {
                val event = awaitPointerEvent()
                val canceled = event.changes.fastAny { it.isConsumed }
                if (!canceled) {
                    val zoomChange = event.calculateZoom()
                    val rotationChange = event.calculateRotation()
                    val panChange = event.calculatePan()

                    if (!pastTouchSlop) {
                        zoom *= zoomChange
                        rotation += rotationChange
                        pan += panChange

                        val centroidSize = event.calculateCentroidSize(useCurrent = false)
                        val zoomMotion = abs(1 - zoom) * centroidSize
                        val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                        val panMotion = pan.getDistance()

                        if (zoomMotion > touchSlop ||
                            rotationMotion > touchSlop ||
                            panMotion > touchSlop
                        ) {
                            pastTouchSlop = true
                            lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                        }
                    }

                    if (pastTouchSlop) {
                        val centroid = event.calculateCentroid(useCurrent = false)
                        val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                        if (effectiveRotation != 0f ||
                            zoomChange != 1f ||
                            panChange != Offset.Zero
                        ) {
                            onGesture(event, centroid, panChange, zoomChange, effectiveRotation)
                        }
                        // Do NOT consume PointerInputChange here.
//                        event.changes.fastForEach {
//                            if (it.positionChanged()) {
////                                it.consume()
//                            }
//                        }
                    }
                }
            } while (!canceled && event.changes.fastAny { it.pressed })
            onGestureEnd()
        }
    }
}

fun PointerEvent.consumeChanges() {
    changes.fastForEach {
        if (it.positionChanged()) {
            it.consume()
        }
    }
}
