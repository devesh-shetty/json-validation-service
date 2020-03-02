package controllers

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.google.inject.{Inject, Singleton}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Writes}
import play.api.mvc.{AbstractController, Action, AnyContent}

import scala.util.{Failure, Success, Try}

@Singleton
private[controllers] final class SchemaControllerImpl @Inject()(scc: SchemaControllerComponents) extends AbstractController(scc) with SchemaController {
  override def index: Action[AnyContent] = Action { request =>
    Ok(s"Hello world: $request")
  }

  override def echo(content: String): Action[AnyContent] = Action { request =>
    Ok(s"Echoing $content $request")
  }

  override def uploadSchema(schemaId: String): Action[AnyContent] = Action { request =>
    def convertToResult(node: Try[JsonNode]) = node match {
      case Success(_) =>
        val response = Response(
          action = ActionConstants.ACTION_UPLOAD,
          id = schemaId,
          status = ActionConstants.RESPONSE_SUCCESS,
          message = None
        )
        Created(Json.toJson(response))
      case Failure(_) =>
        val response = Response(
          action = ActionConstants.ACTION_UPLOAD,
          id = schemaId,
          status = ActionConstants.RESPONSE_ERROR,
          message = Some(ActionConstants.RESPONSE_ERROR_INVALID_JSON_MESSAGE)
        )
        BadRequest(Json.toJson(response))
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

  private def convertToJsonNode(string: String) = Try(new ObjectMapper().readTree(string))

  implicit val responseWrites: Writes[Response] = (
    (JsPath \ "action").write[String] and
      (JsPath \ "id").write[String] and
      (JsPath \ "status").write[String] and
      (JsPath \ "message").writeNullable[String]
    ) (unlift(Response.unapply))
}

object ActionConstants {
  val ACTION_UPLOAD = "uploadSchema"
  val RESPONSE_SUCCESS = "success"
  val RESPONSE_ERROR = "error"
  val RESPONSE_ERROR_INVALID_JSON_MESSAGE = "Invalid JSON"
  val RESPONSE_ERROR_MISSING_JSON_MESSAGE = "Missing JSON schema"
}

private final case class Response(action: String, id: String, status: String, message: Option[String])
