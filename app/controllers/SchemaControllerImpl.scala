package controllers

import com.google.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, BaseController, ControllerComponents, Request}

@Singleton
private[controllers] final class SchemaControllerImpl @Inject()(cc: ControllerComponents) extends AbstractController(cc) with SchemaController {
  override def index: Action[AnyContent] = Action { request =>
    Ok("Hello world")
  }

  override def echo(content: String): Action[AnyContent] = Action { request =>
    Ok(s"Echoing $content")
  }
}