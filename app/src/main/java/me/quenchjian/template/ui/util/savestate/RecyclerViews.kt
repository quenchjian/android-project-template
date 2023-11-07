package me.quenchjian.template.ui.util.savestate

import android.os.Bundle
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.saveScrollPosition(state: Bundle) {
  val outState = layoutManager?.onSaveInstanceState() ?: return
  state.putParcelable("recycler-view-$id", outState)
}

fun RecyclerView.restoreScrollPosition(state: Bundle) {
  @Suppress("DEPRECATION")
  val saved: Parcelable = state.getParcelable("recycler-view-$id") ?: return
  val adapter = adapter ?: return
  adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
      restore()
    }

    override fun onChanged() {
      restore()
    }

    private fun restore() {
      adapter.unregisterAdapterDataObserver(this)
      layoutManager?.onRestoreInstanceState(saved)
    }
  })
}