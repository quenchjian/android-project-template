package me.quenchjian.template.concurrent

import kotlin.coroutines.CoroutineContext

interface CoroutineProvider {
  val main: CoroutineContext
  val background: CoroutineContext
}