package me.quenchjian.template.concurrent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class AndroidCoroutineProvider @Inject constructor() : CoroutineProvider {

  override val main: CoroutineContext = Dispatchers.Main
  override val background: CoroutineContext = BackgroundExecutor().asCoroutineDispatcher()

  companion object {
    private const val CORE_POOL_SIZE = 3
    private const val MAX_POOL_SIZE = Int.MAX_VALUE
    private const val KEEP_ALIVE_TIME = 60L
  }

  private class BackgroundExecutor : Executor {
    private val executor = ThreadPoolExecutor(
      CORE_POOL_SIZE,
      MAX_POOL_SIZE,
      KEEP_ALIVE_TIME,
      TimeUnit.SECONDS,
      LinkedBlockingDeque()
    )

    override fun execute(command: Runnable) {
      executor.execute(command)
    }
  }
}