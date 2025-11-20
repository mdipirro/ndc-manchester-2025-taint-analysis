package controllers

import com.mdipirro.security.{TaintLevel, TaintTracked}
import models.Widget
import play.api.data.*
import play.api.i18n.*
import play.api.mvc.*
import repositories.WidgetRepository

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
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest( views.html.listWidgets(repo.listWidgets().open, formWithErrors, postUrl))
    }

    val successFunction = { (data: Data) =>
      // This is the good case, where the form was successfully parsed as a Data object.
      val rawWidget = Widget(name = data.name, price = data.price)
      val taintedWidget = for {
        input <- TaintTracked(rawWidget)
        prefixed <- TaintTracked.unsafe("Demo widget: ")
      } yield input.copy(name = prefixed + input.name)
      //repo.addWidget(taintedWidget) // THIS DOES NOT COMPILE!
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
    }

    val formValidationResult = form.bindFromRequest()
    formValidationResult.fold(errorFunction, successFunction)
  }
}
