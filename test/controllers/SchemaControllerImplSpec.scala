package controllers

import dao.SchemaRepository
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.DefaultActionBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, CREATED, GET, POST, contentAsJson, defaultAwaitTimeout, status, stubControllerComponents}

final class SchemaControllerImplSpec extends PlaySpec {
  private lazy val stubSchemaControllerComponents = {
    val stub = stubControllerComponents()
    SchemaControllerComponents(
      schemaDao = mock[SchemaRepository],
      actionBuilder = DefaultActionBuilder(stub.parsers.default)(stub.executionContext),
      parsers = stub.parsers,
      messagesApi = stub.messagesApi,
      langs = stub.langs,
      fileMimeTypes = stub.fileMimeTypes,
      executionContext = stub.executionContext
    )
  }

  "uploadSchema" should {
    "return the success payload for valid json schema" in {
      val controller = new SchemaControllerImpl(stubSchemaControllerComponents)
      val schemaId = "valid-schema"
      val jsonSchema =
        """{
          |  "$schema": "http://json-schema.org/draft-04/schema#",
          |  "type": "object",
          |  "properties": {
          |    "source": {
          |      "type": "string"
          |    },
          |    "destination": {
          |      "type": "string"
          |    }
          |   }
          |}""".stripMargin
      val request = FakeRequest(POST, "")
        .withFormUrlEncodedBody(jsonSchema -> "1")

      val result = controller.uploadSchema(schemaId)(request)
      status(result)(defaultAwaitTimeout) mustEqual CREATED
      contentAsJson(result)(defaultAwaitTimeout) mustEqual Json.parse(s"""{"action":"${ActionConstants.ACTION_UPLOAD}","id":"$schemaId", "status":"${ActionConstants.RESPONSE_SUCCESS}"}""")
    }

    "return the error payload for invalid json schema" in {
      val controller = new SchemaControllerImpl(stubSchemaControllerComponents)
      val schemaId = "invalid-schema"
      val jsonSchema =
        """{
          |  "$schema": "http://json-schema.org/draft-04/schema#",
          |  "properties":
          |    "source": {
          |      "type": "string"
          |    },
          |    "destination": {
          |      "type": "string"
          |}""".stripMargin
      val request = FakeRequest(POST, "")
        .withFormUrlEncodedBody(jsonSchema -> "1")

      val result = controller.uploadSchema(schemaId)(request)
      status(result)(defaultAwaitTimeout) mustEqual BAD_REQUEST
      contentAsJson(result)(defaultAwaitTimeout) mustEqual Json.parse(s"""{"action":"${ActionConstants.ACTION_UPLOAD}","id":"$schemaId", "status":"${ActionConstants.RESPONSE_ERROR}", "message": "${ActionConstants.RESPONSE_ERROR_INVALID_JSON_MESSAGE}"}""")
    }

    "return the error payload for missing schema" in {
      val controller = new SchemaControllerImpl(stubSchemaControllerComponents)
      val schemaId = "invalid-schema"

      val result = controller.uploadSchema(schemaId)(FakeRequest(POST, ""))
      status(result)(defaultAwaitTimeout) mustEqual BAD_REQUEST
      contentAsJson(result)(defaultAwaitTimeout) mustEqual Json.parse(s"""{"action":"${ActionConstants.ACTION_UPLOAD}","id":"$schemaId", "status":"${ActionConstants.RESPONSE_ERROR}", "message": "${ActionConstants.RESPONSE_ERROR_MISSING_JSON_MESSAGE}"}""")
    }

    "return the error payload for wrong http verb" in {
      val controller = new SchemaControllerImpl(stubSchemaControllerComponents)
      val schemaId = "invalid-schema"

      val result = controller.uploadSchema(schemaId)(FakeRequest(GET, ""))
      status(result)(defaultAwaitTimeout) mustEqual BAD_REQUEST
      contentAsJson(result)(defaultAwaitTimeout) mustEqual Json.parse(s"""{"action":"${ActionConstants.ACTION_UPLOAD}","id":"$schemaId", "status":"${ActionConstants.RESPONSE_ERROR}", "message": "${ActionConstants.RESPONSE_ERROR_MISSING_JSON_MESSAGE}"}""")
    }
  }
}
