package controllers

import com.mdipirro.security.TaintTracked
import models.Widget
import play.api.data.*
import play.api.mvc.*
import repositories.WidgetRepository

import javax.inject.Inject

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
class SecuredWidgetController @Inject()(cc: MessagesControllerComponents, repo: WidgetRepository) extends MessagesAbstractController(cc) {
  import WidgetForm.*

  // The URL to the widget.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "WidgetController" references are inside the .scala file.
  private val postUrl = routes.SecuredWidgetController.createWidget

  def listWidgets = Action { implicit request: MessagesRequest[AnyContent] =>
    // Pass an unpopulated form to the template
    Ok(views.html.listWidgets(repo.listWidgets().open, form, postUrl))
  }

  // This will be the action that handles our form post
  def createWidget = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { (formWithErrors: Form[Data]) =>
      BadRequest( views.html.listWidgets(repo.listWidgets().open, formWithErrors, postUrl))
    }

    val successFunction = { (data: Data) =>
      val rawWidget = Widget(name = data.name, price = data.price)
      val taintedWidget = TaintTracked(rawWidget)
      
      //repo.addWidget(taintedWidget) // THIS DOES NOT COMPILE!
      //repo.addWidget(TaintTracked.unsafe(rawWidget)) // THIS DOES COMPILE!!
      
      val sanitisedWidget = taintedWidget.sanitise { widget =>
        if widget.name.forall(c => c.isLetterOrDigit || c == ' ' || c == ':') then
          Right(widget)
        else
          Left("Sanitisation failed: The name of the widget must not contain any special characters.")
      }
      sanitisedWidget match {
        case Left(error) => Redirect(routes.SecuredWidgetController.listWidgets).flashing("Error" -> error)
        case Right(widget) =>
          repo.addWidget(widget)
          Redirect(routes.SecuredWidgetController.listWidgets).flashing("info" -> "Widget added!")
      }
      
      /*val composedWidget = taintedWidget.flatMap { w =>
        TaintTracked.unsafe(s"A new ${w.name}")
      }
      composedWidget.open*/
    }

    val formValidationResult = form.bindFromRequest()
    formValidationResult.fold(errorFunction, successFunction)
  }
}
