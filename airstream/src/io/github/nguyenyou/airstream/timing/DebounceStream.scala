package io.github.nguyenyou.airstream.timing

import io.github.nguyenyou.airstream.common.{InternalTryObserver, SingleParentStream}
import io.github.nguyenyou.airstream.core.{EventStream, Transaction}

import scala.scalajs.js
import scala.scalajs.js.timers.SetTimeoutHandle
import scala.util.Try

// @TODO[Test] Verify debounce

/** This stream emits the last event emitted by `parent`, but only after `intervalMs` has elapsed
  * since `parent` emitted the previous event.
  *
  * Essentially, this stream emits the parent's last event, but only once the parent stops emitting
  * events for `intervalMs`.
  *
  * When stopped, this stream "forgets" about any pending events.
  *
  * See also [[ThrottleStream]]
  */
class DebounceStream[A](
  override protected val parent: EventStream[A],
  intervalMs: Int
) extends SingleParentStream[A, A] with InternalTryObserver[A] {

  private var maybeLastTimeoutHandle: js.UndefOr[SetTimeoutHandle] = js.undefined

  override protected val topoRank: Int = 1

  /** Every time [[parent]] emits an event, we clear the previous timer and set a new one.
    * This stream only emits when the parent has stopped emitting for [[intervalMs]] ms.
    */
  override protected def onTry(nextValue: Try[A], transaction: Transaction): Unit = {
    maybeLastTimeoutHandle.foreach(js.timers.clearTimeout)
    maybeLastTimeoutHandle = js.timers.setTimeout(intervalMs.toDouble) {
      // println(s"> init trx from DebounceEventStream.onTry($nextValue)")
      Transaction(fireTry(nextValue, _))
    }
  }

  override protected def onStop(): Unit = {
    maybeLastTimeoutHandle.foreach(js.timers.clearTimeout)
    maybeLastTimeoutHandle = js.undefined
    super.onStop()
  }
}
