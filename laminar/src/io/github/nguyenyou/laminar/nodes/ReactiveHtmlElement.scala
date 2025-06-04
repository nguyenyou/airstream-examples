package io.github.nguyenyou.laminar.nodes

import io.github.nguyenyou.airstream.ownership.DynamicSubscription
import com.raquo.ew.JsArray
import io.github.nguyenyou.laminar.DomApi
import io.github.nguyenyou.laminar.inputs.InputController
import io.github.nguyenyou.laminar.keys.{HtmlProp, Key}
import io.github.nguyenyou.laminar.tags.{CustomHtmlTag, HtmlTag}
import org.scalajs.dom

import scala.scalajs.js

class ReactiveHtmlElement[+Ref <: dom.html.Element](
  override val tag: HtmlTag[Ref],
  final override val ref: Ref
) extends ReactiveElement[Ref] {

  /** List of value controllers installed on this element */
  private var controllers: js.UndefOr[JsArray[InputController[?, ?, ?]]] = js.undefined

  /**
    * List of binders for props that are controllable.
    * Note: this includes both controlled and uncontrolled binders
    */
  private var controllablePropBinders: js.UndefOr[JsArray[String]] = js.undefined

  private def appendValueController(controller: InputController[?, ?, ?]): Unit = {
    controllers.fold {
      controllers = js.defined(JsArray(controller))
    }(_.push(controller))
  }

  private def appendControllablePropBinder(propDomName: String): Unit = {
    controllablePropBinders.fold {
      controllablePropBinders = js.defined(JsArray(propDomName))
    }(_.push(propDomName))
  }

  private[laminar] def hasBinderForControllableProp(domPropName: String): Boolean = {
    controllablePropBinders.exists(_.includes(domPropName))
  }

  private[laminar] def hasOtherControllerForSameProp(thisController: InputController[?, ?, ?]): Boolean = {
    controllers.exists(_.asScalaJs.exists { otherController =>
      otherController.propDomName == thisController.propDomName && otherController != thisController
    })
  }

  private[laminar] def bindController(controller: InputController[?, ?, ?]): DynamicSubscription = {
    val dynSub = controller.bind()
    appendValueController(controller)
    dynSub
  }

  // --

  private[laminar] def controllableProps: js.UndefOr[JsArray[String]] = {
    if (DomApi.isCustomElement(ref)) {
      tag match {
        case t: CustomHtmlTag[_] => t.allowableControlProps
        case _ => js.undefined
      }
    } else {
      InputController.htmlControllableProps
    }
  }

  private[laminar] def isControllableProp(propDomName: String): Boolean = {
    controllableProps.exists(_.includes(propDomName))
  }

  private def hasController(propDomName: String): Boolean = {
    controllers.exists(_.asScalaJs.exists(_.propDomName == propDomName))
  }

  override private[laminar] def onBoundKeyUpdater(key: Key): Unit = {
    key match {
      case p: HtmlProp[_, _] =>
        if (isControllableProp(p.name)) {
          if (hasController(p.name)) {
            throw new Exception(s"Can not add uncontrolled `${p.name} <-- ???` to element `${DomApi.debugNodeDescription(ref)}` that already has an input controller for `${p.name}` property.")
          } else {
            appendControllablePropBinder(p.name)
          }
        }
      case _ => ()
    }
  }

  override def toString: String = {
    // `ref` is not available inside ReactiveElement's constructor due to initialization order, so fall back to `tag`.
    s"ReactiveHtmlElement(${if (ref != null) ref.outerHTML else s"tag=${tag.name}"})"
  }
}

object ReactiveHtmlElement {

  type Base = ReactiveHtmlElement[dom.html.Element]
}
