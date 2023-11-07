package me.quenchjian.template.ui.util.event

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.absoluteValue

private const val DISTANCE = 50
private const val VELOCITY = 100

enum class SwipeDirection { LEFT, TOP, RIGHT, BOTTOM }

@SuppressLint("ClickableViewAccessibility")
fun View.onSwipe(
  distance: Int = DISTANCE,
  velocity: Int = VELOCITY,
  listener: (SwipeDirection) -> Unit
) {
  val detector = GestureDetector(context, SwipeListener(distance, velocity, listener))
  setOnTouchListener { _, event -> detector.onTouchEvent(event) }
}

private class SwipeListener(
  private val distance: Int,
  private val velocity: Int,
  private val listener: (SwipeDirection) -> Unit
) : GestureDetector.SimpleOnGestureListener() {

  override fun onFling(
    e1: MotionEvent?,
    e2: MotionEvent,
    velocityX: Float,
    velocityY: Float
  ): Boolean {
    if (e1 == null) {
      return false
    }
    val diffX = (e1.x - e2.x).absoluteValue
    val diffY = (e1.y - e2.y).absoluteValue
    if (diffX > diffY) {
      if (diffX >= distance && velocityX >= velocity) {
        listener.invoke(if (e1.x > e2.x) SwipeDirection.LEFT else SwipeDirection.RIGHT)
        return true
      }
    } else {
      if (diffY >= distance && velocityY >= velocity) {
        listener.invoke(if (e1.y > e2.y) SwipeDirection.TOP else SwipeDirection.BOTTOM)
        return true
      }
    }
    return false
  }

  override fun onDown(e: MotionEvent): Boolean {
    return true
  }
}