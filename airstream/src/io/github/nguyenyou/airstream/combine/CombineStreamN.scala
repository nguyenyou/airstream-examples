package io.github.nguyenyou.airstream.combine

import io.github.nguyenyou.airstream.common.{InternalParentObserver, MultiParentStream}
import io.github.nguyenyou.airstream.core.{EventStream, Observable, Protected}
import io.github.nguyenyou.ew.JsArray

import scala.scalajs.js
import scala.util.Try

/**
  * @param parentStreams Never update this array - this stream owns it.
  * @param combinator Must not throw! Must be pure.
  */
class CombineStreamN[A, Out](
  parentStreams: JsArray[EventStream[A]],
  combinator: JsArray[A] => Out
) extends MultiParentStream[A, Out] with CombineObservable[Out] {

  // @TODO[API] Maybe this should throw if parents.isEmpty

  override protected val parents: JsArray[Observable[A]] = {
    // #Note this is safe as long as we don't put non-streams into this JsArray.
    parentStreams.asInstanceOf[JsArray[Observable[A]]]
  }

  override protected val topoRank: Int = Protected.maxTopoRank(parents) + 1

  private val maybeLastParentValues: JsArray[js.UndefOr[Try[A]]] = parents.map(_ => js.undefined)

  override protected val parentObservers: JsArray[InternalParentObserver[?]] = {
    parents.mapWithIndex { (parent, ix) =>
      InternalParentObserver.fromTry[A](
        parent,
        (nextParentValue, trx) => {
          maybeLastParentValues.update(ix, nextParentValue)
          if (inputsReady) {
            onInputsReady(trx)
          }
        }
      )
    }
  }

  override protected def inputsReady: Boolean = {
    var allReady: Boolean = true
    maybeLastParentValues.forEach { lastValue =>
      if (lastValue.isEmpty) {
        allReady = false
      }
    }
    allReady
  }

  override protected def combinedValue: Try[Out] = {
    // #Note don't call this unless you have first verified that
    //  inputs are ready, otherwise this asInstanceOf will not be safe.
    CombineObservable.jsArrayCombinator(maybeLastParentValues.asInstanceOf[JsArray[Try[A]]], combinator)
  }
}
