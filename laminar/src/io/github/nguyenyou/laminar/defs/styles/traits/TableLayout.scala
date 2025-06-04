package io.github.nguyenyou.laminar.defs.styles.traits

import io.github.nguyenyou.laminar.keys.StyleProp
import io.github.nguyenyou.laminar.modifiers.KeySetter.StyleSetter

// #NOTE: GENERATED CODE
//  - This file is generated at compile time from the data in Scala DOM Types
//  - See `project/DomDefsGenerator.scala` for code generation params
//  - Contribute to https://github.com/raquo/scala-dom-types to add missing tags / attrs / props / etc.

trait TableLayout extends Auto { this: StyleProp[?] =>

  /**
    * An automatic table layout algorithm is commonly used by most browsers for
    * table layout. The width of the table and its cells depends on the content
    * thereof.
    */
  override lazy val auto: StyleSetter = this := "auto"

  /**
    * Table and column widths are set by the widths of table and col elements
    * or by the width of the first row of cells. Cells in subsequent rows do
    * not affect column widths.
    */
  lazy val fixed: StyleSetter = this := "fixed"

}
