package me.quenchjian.template.ui.util.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import me.quenchjian.template.ui.ViewHelper
import me.quenchjian.template.ui.util.event.onClick

abstract class ItemViewHolder(override val rootView: View) : RecyclerView.ViewHolder(rootView), ViewHelper {

  var positionInAdapter = -1
    get() = when {
      field > -1 -> field
      else -> bindingAdapterPosition
    }

  fun resetDefaultStyle() {}
  fun toggleSelectStyle() {}

  fun onItemViewClick(click: (Int) -> Unit) {
    rootView.onClick { click.invoke(positionInAdapter) }
  }
}