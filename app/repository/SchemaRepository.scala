package repository

import com.google.inject.ImplementedBy
import model.Schema

@ImplementedBy(classOf[SchemaFileSystemDataSource])
trait SchemaRepository {
  def save(schema: Schema): Either[SchemaStorageError, Unit]
}

sealed trait SchemaStorageError

case object FailedToSaveSchema extends SchemaStorageError
