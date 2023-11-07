package me.quenchjian.template.ui

import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat

interface ViewHelper {

  val rootView: View
  val context get() = rootView.context
  val displayMetrics get() = context.resources.displayMetrics
  fun string(@StringRes resId: Int, vararg args: Any) = context.getString(resId, *args)
  @Px fun dimen(@DimenRes resId: Int) = context.resources.getDimension(resId)
  @ColorInt fun color(@ColorRes resId: Int) = ContextCompat.getColor(context, resId)
  fun colorStateList(@ColorRes resId: Int) = ContextCompat.getColorStateList(context, resId)
  fun drawableOrNull(@DrawableRes resId: Int) = AppCompatResources.getDrawable(context, resId)
  fun drawable(@DrawableRes resId: Int) = drawableOrNull(resId) ?: EMPTY
  @Px fun Float.dp(): Int = (this * displayMetrics.density).toInt()
  @Px fun Float.sp(): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, displayMetrics).toInt()

  companion object {
    private val EMPTY = ColorDrawable()
  }
}