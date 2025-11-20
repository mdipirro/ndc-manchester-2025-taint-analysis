package repositories

import com.mdipirro.security.{TaintLevel, TaintTracked}
import models.Widget

import scala.collection.mutable

class WidgetRepository {
  private val widgets = mutable.ArrayBuffer(
    Widget("Widget 1", 123),
    Widget("Widget 2", 456),
    Widget("Widget 3", 789)
  )

  def listWidgets(): TaintTracked[TaintLevel.Pure.type, Seq[Widget]] = TaintTracked.unsafe(widgets.toSeq)

  def addWidget(widget: TaintTracked[TaintLevel.Sanitised.type, Widget]): Unit = widgets += widget.open
}
