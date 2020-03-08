package model

import com.fasterxml.jackson.databind.JsonNode

@SerialVersionUID(909090909212L)
final case class Schema(schemaId: String, jsonNode: JsonNode) extends Serializable