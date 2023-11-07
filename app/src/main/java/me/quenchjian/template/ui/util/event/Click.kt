package me.quenchjian.template.ui.util.event

import android.os.Handler
import android.os.Looper
import android.view.View

private const val DEFAULT_INTERVAL = 500L

fun View.onClick(interval: Long = DEFAULT_INTERVAL, click: (View) -> Unit) {
  setOnClickListener(NoFastClick(interval, click))
}

private class NoFastClick(
  private val interval: Long = DEFAULT_INTERVAL,
  private val wrapped: (View) -> Unit = {}
): View.OnClickListener {

  companion object {
    private var enable = true
    private val enableAgain: () -> Unit = { enable = true}
    private val handler = Handler(Looper.getMainLooper())
  }

  override fun onClick(v: View) {
    if (enable) {
      enable = false
      handler.postDelayed(enableAgain, interval)
      wrapped.invoke(v)
    }
  }
}