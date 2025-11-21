package repositories

import com.mdipirro.security.{CanOpen, TaintLevel, TaintTracked}
import models.Widget

import scala.collection.mutable

class WidgetRepository {
  private val widgets = mutable.ArrayBuffer(
    Widget("Widget 1", 123),
    Widget("Widget 2", 456),
    Widget("Widget 3", 789)
  )

  def listWidgets(): TaintTracked[TaintLevel.Pure.type, Seq[Widget]] = TaintTracked.unsafe(widgets.toSeq)

  def addWidget[T <: TaintLevel](widget: TaintTracked[T, Widget])(using CanOpen[T]): Unit = widgets += widget.open
}
