package controllers

import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import javax.inject.Inject

class IndexController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  def index = Action {
    Ok(views.html.index())
  }
}
