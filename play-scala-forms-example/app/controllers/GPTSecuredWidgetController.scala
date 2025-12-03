package controllers

import com.mdipirro.security.TaintTracked
import models.Widget
import play.api.data.*
import play.api.mvc.*

import javax.inject.Inject
import scala.collection.*

/**
 * The classic WidgetController using MessagesAbstractController.
 *
 * Instead of MessagesAbstractController, you can use the I18nSupport trait,
 * which provides implicits that create a Messages instance from a request
 * using implicit conversion.
 *
 * See https://www.playframework.com/documentation/latest/ScalaForms#Passing-MessagesProvider-to-Form-Helpers
 * for details.
 */
class GPTSecuredWidgetController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  import WidgetForm.*

  private val widgets = mutable.ArrayBuffer(
    Widget("Widget 1", 123),
    Widget("Widget 2", 456),
    Widget("Widget 3", 789)
  )

  // The URL to the widget.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "WidgetController" references are inside the .scala file.
  private val postUrl = routes.GPTSecuredWidgetController.createWidget

  def listWidgets: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    // Pass an unpopulated form to the template
    Ok(views.html.listWidgets(widgets.toSeq, form, postUrl))
  }

  // This will be the action that handles our form post
  def createWidget: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { (formWithErrors: Form[Data]) =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.listWidgets(widgets.toSeq, formWithErrors, postUrl))
    }

    /*
    Propmt that generated the code below:

    I want to secure the VulnerableWidgetController class by using the TaintTracked class that you have in the scope.
    In particular, I want to force sanitisation on the widget created in createWidget. Any widgets should be sanitised
    before invoking widgets += widget

     */

    val successFunction = { (data: Data) =>
      // Sanitize widget before adding
      val rawWidget = Widget(name = data.name, price = data.price)
      val taintedWidget = TaintTracked(rawWidget)
      // Example sanitisation: ensure name is alphanumeric and price is positive
      def sanitiseWidget(w: Widget): Either[String, Widget] = {
        val nameSanitised = w.name.replaceAll("[^A-Za-z0-9 ]", "")
        if (nameSanitised.isEmpty) Left("Name must be alphanumeric.")
        else if (w.price < 0) Left("Price must be positive.")
        else Right(w.copy(name = nameSanitised))
      }
      taintedWidget.sanitise(sanitiseWidget) match {
        case Right(sanitisedWidget) =>
          widgets += sanitisedWidget.open // Only add sanitised widget
          Redirect(routes.GPTSecuredWidgetController.listWidgets).flashing("info" -> "Widget added!")
        case Left(error) =>
          BadRequest(views.html.listWidgets(widgets.toSeq, form.fill(data).withGlobalError(error), postUrl))
      }
    }

    val formValidationResult = form.bindFromRequest()
    formValidationResult.fold(errorFunction, successFunction)
  }
}
