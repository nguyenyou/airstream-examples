package com.raquo.airstream.state

import com.raquo.airstream.core.Transaction

import scala.util.{Failure, Success, Try}

/** #nc: this is replacement for LazyDerivedVar class, added for bin compat in 17.2.0 release.
  *  - In 18.0, this will become the one and only LazyDerivedVar
  *
  * [[LazyDerivedVar2]] has the same Var contract as DerivedVar,
  * but it only computes its value lazily, e.g. when you
  * ask for it with .now(), or when its signal has subscribers.
  *
  * Unlike the regular DerivedVar, you don't need to provide an Owner
  * to create LazyDerivedVar, and you're allowed to update this Var
  * even if its signal has no subscribers.
  *
  * @param updateParent  (currentParentValue, nextValue) => nextParentValue.
  */
class LazyDerivedVar2[A, B](
  parent: Var[A],
  override val signal: StrictSignal[B],
  updateParent: (Try[A], Try[B]) => Option[Try[A]],
  displayNameSuffix: String
) extends Var[B] {

  override private[state] def underlyingVar: SourceVar[?] = parent.underlyingVar

  // #Note this getCurrentValue implementation is different from SourceVar
  //  - SourceVar's getCurrentValue looks at an internal currentValue variable
  //  - That currentValue gets updated immediately before the signal (in an already existing transaction)
  //  - I hope this doesn't introduce weird transaction related timing glitches
  //  - But even if it does, I think keeping derived var's current value consistent with its signal value
  //    is more important, otherwise it would be madness if the derived var was accessed after its owner
  //    was killed
  override private[state] def getCurrentValue: Try[B] = signal.tryNow()

  override private[state] def setCurrentValue(value: Try[B], transaction: Transaction): Unit = {
    val maybeNextValue = Try(updateParent(parent.tryNow(), value)) match {
      case Success(nextValue) => nextValue
      case Failure(err) => Some(Failure(err))
    }
    maybeNextValue.foreach { nextValue =>
      // This can update the parent without causing an infinite loop because
      // the parent updates this derived var's signal, it does not call
      // setCurrentValue on this var directly.
      parent.setCurrentValue(nextValue, transaction)
    }
  }

  override protected def defaultDisplayName: String = parent.displayName + displayNameSuffix
}
