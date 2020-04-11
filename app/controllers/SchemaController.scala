package controllers

import com.google.inject.{ImplementedBy, Inject}
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{Action, AnyContent, ControllerComponents, DefaultActionBuilder, PlayBodyParsers}
import repository.SchemaRepository

@ImplementedBy(classOf[SchemaControllerImpl])
trait SchemaController {
  def uploadSchema(schemaId: String): Action[AnyContent]

  def downloadSchema(schemaId: String): Action[AnyContent]

  def validateSchemaWith(schemaId: String): Action[AnyContent]

  def invalidOperation(): Action[AnyContent]
}

case class SchemaControllerComponents @Inject()(schemaRepository: SchemaRepository,
                                                actionBuilder: DefaultActionBuilder,
                                                parsers: PlayBodyParsers,
                                                messagesApi: MessagesApi,
                                                langs: Langs,
                                                fileMimeTypes: FileMimeTypes,
                                                executionContext: scala.concurrent.ExecutionContext) extends ControllerComponents
