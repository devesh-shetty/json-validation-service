package controllers

import com.google.inject.ImplementedBy
import play.api.mvc.{Action, AnyContent}

@ImplementedBy(classOf[SchemaControllerImpl])
trait SchemaController {
  def index: Action[AnyContent]

  def echo(content: String): Action[AnyContent]
}
