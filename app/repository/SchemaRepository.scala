package repository

import java.io.File

import com.google.inject.ImplementedBy
import model.Schema

@ImplementedBy(classOf[SchemaFileSystemDataSource])
trait SchemaRepository {
  def save(schema: Schema): Either[SchemaStorageError, Unit]

  def fetchFile(schemaId: String): Option[File]
}

sealed trait SchemaStorageError

case object FailedToSaveSchema extends SchemaStorageError
