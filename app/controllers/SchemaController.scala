package controllers

import com.google.inject.{ImplementedBy, Inject}
import dao.SchemaRepository
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{Action, AnyContent, ControllerComponents, DefaultActionBuilder, PlayBodyParsers}

@ImplementedBy(classOf[SchemaControllerImpl])
trait SchemaController {
  def index: Action[AnyContent]

  def echo(content: String): Action[AnyContent]

  def uploadSchema(schemaId: String): Action[AnyContent]
}

case class SchemaControllerComponents @Inject()(schemaDao: SchemaRepository,
                                                actionBuilder: DefaultActionBuilder,
                                                parsers: PlayBodyParsers,
                                                messagesApi: MessagesApi,
                                                langs: Langs,
                                                fileMimeTypes: FileMimeTypes,
                                                executionContext: scala.concurrent.ExecutionContext) extends ControllerComponents
