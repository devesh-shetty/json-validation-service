package controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.DefaultActionBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, CREATED, GET, INTERNAL_SERVER_ERROR, POST, contentAsJson, defaultAwaitTimeout, status, stubControllerComponents}
import repository.{FailedToSaveSchema, SchemaRepository}

final class SchemaControllerImplSpec extends PlaySpec {
  "uploadSchema" should {
    "return the success payload for valid json schema" in {
      val mockSchemaRepository = mock[SchemaRepository]
      when(mockSchemaRepository.save(any())).thenReturn(Right(()))
      val controller = new SchemaControllerImpl(stubSchemaControllerComponents(mockSchemaRepository))
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
      val mockSchemaRepository = mock[SchemaRepository]
      val controller = new SchemaControllerImpl(stubSchemaControllerComponents(mockSchemaRepository))
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
      verify(mockSchemaRepository, never()).save(any())
    }

    "return the error payload for missing schema" in {
      val mockSchemaRepository = mock[SchemaRepository]
      val controller = new SchemaControllerImpl(stubSchemaControllerComponents(mockSchemaRepository))
      val schemaId = "invalid-schema"

      val result = controller.uploadSchema(schemaId)(FakeRequest(POST, ""))
      status(result)(defaultAwaitTimeout) mustEqual BAD_REQUEST
      contentAsJson(result)(defaultAwaitTimeout) mustEqual Json.parse(s"""{"action":"${ActionConstants.ACTION_UPLOAD}","id":"$schemaId", "status":"${ActionConstants.RESPONSE_ERROR}", "message": "${ActionConstants.RESPONSE_ERROR_MISSING_JSON_MESSAGE}"}""")
      verify(mockSchemaRepository, never()).save(any())
    }

    "return the error payload for wrong http verb" in {
      val mockSchemaRepository = mock[SchemaRepository]
      val controller = new SchemaControllerImpl(stubSchemaControllerComponents(mockSchemaRepository))
      val schemaId = "invalid-schema"

      val result = controller.uploadSchema(schemaId)(FakeRequest(GET, ""))
      status(result)(defaultAwaitTimeout) mustEqual BAD_REQUEST
      contentAsJson(result)(defaultAwaitTimeout) mustEqual Json.parse(s"""{"action":"${ActionConstants.ACTION_UPLOAD}","id":"$schemaId", "status":"${ActionConstants.RESPONSE_ERROR}", "message": "${ActionConstants.RESPONSE_ERROR_MISSING_JSON_MESSAGE}"}""")
      verify(mockSchemaRepository, never()).save(any())
    }

    "return the error payload on failing to save the schema" in {
      val mockSchemaRepository = mock[SchemaRepository]
      when(mockSchemaRepository.save(any())).thenReturn(Left(FailedToSaveSchema))
      val controller = new SchemaControllerImpl(stubSchemaControllerComponents(mockSchemaRepository))
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
      status(result)(defaultAwaitTimeout) mustEqual INTERNAL_SERVER_ERROR
      contentAsJson(result)(defaultAwaitTimeout) mustEqual Json.parse(s"""{"action":"${ActionConstants.ACTION_UPLOAD}","id":"$schemaId", "status":"${ActionConstants.RESPONSE_ERROR}", "message": "${ActionConstants.RESPONSE_ERROR_FAILED_TO_UPLOAD_JSON_SCHEMA}"}""")
    }
  }

  private def stubSchemaControllerComponents(mockSchemaRepository: SchemaRepository) = {
    val stub = stubControllerComponents()
    SchemaControllerComponents(
      schemaDao = mockSchemaRepository,
      actionBuilder = DefaultActionBuilder(stub.parsers.default)(stub.executionContext),
      parsers = stub.parsers,
      messagesApi = stub.messagesApi,
      langs = stub.langs,
      fileMimeTypes = stub.fileMimeTypes,
      executionContext = stub.executionContext
    )
  }
}
