package controllers

import com.fasterxml.jackson.databind.node.{NullNode, ObjectNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.google.inject.{Inject, Singleton}
import model.Schema
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Writes}
import play.api.mvc.{AbstractController, Action, AnyContent}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.jdk.CollectionConverters._
import scala.util.{Try, Using}

@Singleton
private[controllers] final class SchemaControllerImpl @Inject()(scc: SchemaControllerComponents) extends AbstractController(scc) with SchemaController {
  override def uploadSchema(schemaId: String): Action[AnyContent] = Action { request =>
    def convertToResult(node: Try[Option[JsonNode]]) = {
      val optionalSchema = for {
        jsonNode <- node.toOption.flatten
        schema = Schema(schemaId, jsonNode)
      } yield scc.schemaRepository.save(schema)

      optionalSchema match {
        case Some(savedSchemaResult) =>
          savedSchemaResult match {
            case Left(_) =>
              val response = Response(
                action = ActionConstants.ACTION_UPLOAD,
                id = schemaId,
                status = ActionConstants.RESPONSE_ERROR,
                message = Some(ActionConstants.RESPONSE_ERROR_FAILED_TO_UPLOAD_JSON_SCHEMA)
              )
              InternalServerError(Json.toJson(response))
            case Right(_) =>
              val response = Response(
                action = ActionConstants.ACTION_UPLOAD,
                id = schemaId,
                status = ActionConstants.RESPONSE_SUCCESS,
                message = None
              )
              Created(Json.toJson(response))
          }
        case None =>
          val response = Response(
            action = ActionConstants.ACTION_UPLOAD,
            id = schemaId,
            status = ActionConstants.RESPONSE_ERROR,
            message = Some(ActionConstants.RESPONSE_ERROR_INVALID_JSON_MESSAGE)
          )
          BadRequest(Json.toJson(response))
      }
    }

    val result = for {
      formUrlEncodedData <- request.body.asFormUrlEncoded
      formData <- formUrlEncodedData.headOption
      jsonSchema = formData._1
      result = (convertToJsonNode _ andThen convertToResult) (jsonSchema)
    } yield result

    result getOrElse {
      val response = Response(
        action = ActionConstants.ACTION_UPLOAD,
        id = schemaId,
        status = ActionConstants.RESPONSE_ERROR,
        message = Some(ActionConstants.RESPONSE_ERROR_MISSING_JSON_MESSAGE)
      )
      BadRequest(Json.toJson(response))
    }
  }

  override def downloadSchema(schemaId: String): Action[AnyContent] = Action {
    scc.schemaRepository.fetchFile(schemaId) match {
      case Some(file) => Ok.sendFile(file, inline = false)(scc.executionContext, scc.fileMimeTypes)
      case None =>
        val response = Response(
          action = ActionConstants.ACTION_DOWNLOAD,
          id = schemaId,
          status = ActionConstants.RESPONSE_ERROR,
          message = Some(ActionConstants.RESPONSE_ERROR_SCHEMA_NOT_FOUND)
        )
        NotFound(Json.toJson(response))
    }
  }

  override def validateSchemaWith(schemaId: String): Action[AnyContent] = Action { request =>
    val processingReportOptional = for {
      formUrlEncodedData <- request.body.asFormUrlEncoded
      formData <- formUrlEncodedData.headOption
      jsonSchema = formData._1
      jsonNode <- convertToJsonNode(jsonSchema).toOption.flatten
      updatedJsonNode <- Some(stripNullValues(jsonNode))

      storedSchemaFile <- scc.schemaRepository.fetchFile(schemaId)
      storedSchemaFileContents <- Using(Source.fromFile(storedSchemaFile)) {
        _.mkString
      }.toOption
      storedJsonNode <- convertToJsonNode(storedSchemaFileContents).toOption.flatten
      storedJsonSchema <- Try(JsonSchemaFactory.byDefault().getJsonSchema(storedJsonNode)).toOption
      processingReport <- Try(storedJsonSchema.validate(updatedJsonNode)).toOption
    } yield processingReport

    processingReportOptional match {
      case Some(processingReport) if processingReport.isSuccess =>
        val response = Response(
          action = ActionConstants.ACTION_VALIDATE,
          id = schemaId,
          status = ActionConstants.RESPONSE_SUCCESS,
          message = None
        )
        Ok(Json.toJson(response))
      case Some(processingReport) =>
        val errorMessage = new StringBuilder()
        processingReport.forEach { processingMessage =>
          errorMessage ++= processingMessage.getMessage
        }
        val response = Response(
          action = ActionConstants.ACTION_VALIDATE,
          id = schemaId,
          status = ActionConstants.RESPONSE_ERROR,
          message = Some(errorMessage.mkString)
        )
        BadRequest(Json.toJson(response))
      case None =>
        val response = Response(
          action = ActionConstants.ACTION_VALIDATE,
          id = schemaId,
          status = ActionConstants.RESPONSE_ERROR,
          message = Some(ActionConstants.RESPONSE_ERROR_SCHEMA_NOT_FOUND)
        )
        BadRequest(Json.toJson(response))
    }
  }

  private def stripNullValues(jsonNode: JsonNode): JsonNode = {
    val objectNode = jsonNode.asInstanceOf[ObjectNode]
    val fieldNames = objectNode.fields().asScala
    val nullNodeKeys = ArrayBuffer[String]()

    fieldNames.foreach { fieldName =>
      fieldName.getValue match {
        case _: NullNode =>
          nullNodeKeys += fieldName.getKey
        case _: ObjectNode =>
          objectNode.set(fieldName.getKey, stripNullValues(fieldName.getValue))
        case _ =>
      }
    }

    objectNode.remove(nullNodeKeys.asJava)
    objectNode
  }

  private def convertToJsonNode(string: String) = Try(Option(new ObjectMapper().readTree(string)))

  private implicit val responseWrites: Writes[Response] = (
    (JsPath \ "action").write[String] and
      (JsPath \ "id").write[String] and
      (JsPath \ "status").write[String] and
      (JsPath \ "message").writeNullable[String]
    ) (unlift(Response.unapply))
}

object ActionConstants {
  val ACTION_UPLOAD = "uploadSchema"
  val ACTION_DOWNLOAD = "downloadSchema"
  val ACTION_VALIDATE = "validateDocument"
  val RESPONSE_SUCCESS = "success"
  val RESPONSE_ERROR = "error"
  val RESPONSE_ERROR_INVALID_JSON_MESSAGE = "Invalid JSON"
  val RESPONSE_ERROR_MISSING_JSON_MESSAGE = "Missing JSON schema"
  val RESPONSE_ERROR_FAILED_TO_UPLOAD_JSON_SCHEMA = "Attempt to upload json schema failed"
  val RESPONSE_ERROR_SCHEMA_NOT_FOUND = "Schema not found"
  val RESPONSE_ERROR_VALIDATION_FAILED = "Validation failed"
}

private final case class Response(action: String, id: String, status: String, message: Option[String])
