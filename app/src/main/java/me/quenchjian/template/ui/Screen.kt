package me.quenchjian.template.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner

interface Screen {

  interface UiView : ViewHelper {
    fun attach(state: Bundle)
    fun detach(state: Bundle)
  }

  class ViewStateObserver(
    private val uiView: UiView,
    private val host: SavedStateRegistryOwner
  ) : DefaultLifecycleObserver {

    private val key = "${uiView::class.simpleName}-ui-view-state"
    private var viewState: Bundle? = null

    init {
      host.savedStateRegistry.registerSavedStateProvider(key) { getOrCreateViewState() }
    }

    override fun onCreate(owner: LifecycleOwner) {
      val state = host.savedStateRegistry.consumeRestoredStateForKey(key) ?: return
      uiView.attach(state)
    }

    override fun onDestroy(owner: LifecycleOwner) {
      val state = host.savedStateRegistry.getSavedStateProvider(key)?.saveState() ?: return
      uiView.detach(state)
    }

    private fun getOrCreateViewState(): Bundle {
      if (viewState == null) {
        viewState = Bundle()
      }
      return viewState!!
    }
  }

  abstract class UiActivity : AppCompatActivity() {

    private lateinit var uiView: UiView

    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      uiView = onCreateView()
      setContentView(uiView.rootView)
      lifecycle.addObserver(ViewStateObserver(uiView, this))
    }

    abstract fun onCreateView(): UiView
  }

  abstract class UiFragment : Fragment {
    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    private lateinit var uiView: UiView

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      val v = onCreateView(view).also { uiView = it }
      viewLifecycleOwner.lifecycle.addObserver(ViewStateObserver(v, this))
    }

    abstract fun onCreateView(view: View): UiView
  }
}