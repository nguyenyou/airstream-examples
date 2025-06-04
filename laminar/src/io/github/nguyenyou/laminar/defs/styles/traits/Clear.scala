package io.github.nguyenyou.laminar.defs.styles.traits

import io.github.nguyenyou.laminar.keys.StyleProp
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter

// #NOTE: GENERATED CODE
//  - This file is generated at compile time from the data in Scala DOM Types
//  - See `project/DomDefsGenerator.scala` for code generation params
//  - Contribute to https://github.com/raquo/scala-dom-types to add missing tags / attrs / props / etc.

trait Clear extends None { this: StyleProp[?] =>

  /** The element is moved down to clear past left floats. */
  lazy val left: StyleSetter = this := "left"

  /** The element is moved down to clear past right floats. */
  lazy val right: StyleSetter = this := "right"

  /** The element is moved down to clear past both left and right floats. */
  lazy val both: StyleSetter = this := "both"

}
