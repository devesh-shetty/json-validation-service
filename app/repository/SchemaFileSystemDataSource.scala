package repository

import java.io.{File, PrintWriter}

import com.google.inject.Singleton
import model.Schema

import scala.util.{Failure, Success, Try}

@Singleton
private[repository] final class SchemaFileSystemDataSource extends SchemaRepository {
  override def save(schema: Schema): Either[SchemaStorageError, Unit] = {
    val result = for {
      schemaDirectory <- Try(new File(SchemaFileSystemDataSource.DIRECTORY_PATH))
      createdSchemaDirectory <- createIfDirectoryDoesNotExist(schemaDirectory)
      schemaFile <- Try(new File(createdSchemaDirectory, schema.schemaId + SchemaFileSystemDataSource.FILE_FORMAT))
      printWriter <- Try(new PrintWriter(schemaFile))
      _ <- Try(printWriter.write(schema.jsonNode.toString))
      didEncounterError <- Try(printWriter.checkError())
      result <- if (didEncounterError) Success(Left(FailedToSaveSchema)) else Success(Right(()))
    } yield result

    result getOrElse Left(FailedToSaveSchema)
  }

  override def fetchFile(schemaId: String): Option[File] = for {
    schemaDirectory <- Try(new File(SchemaFileSystemDataSource.DIRECTORY_PATH)).toOption
    schemaFile <- Try(new File(schemaDirectory, schemaId + SchemaFileSystemDataSource.FILE_FORMAT)).toOption
  } yield schemaFile

  private def createIfDirectoryDoesNotExist(directory: File) = {
    def createDirectory(directory: File) = for {
      isCreated <- Try(directory.mkdirs())
      result <- if (isCreated) Success(directory) else Failure(new Exception(s"Failed to create directory with path ${directory.getPath}"))
    } yield result

    Try(directory.exists()) match {
      case Success(doesDirectoryExist) => if (doesDirectoryExist) Success(directory) else createDirectory(directory)
      case Failure(exception) => Failure(exception)
    }
  }
}

private object SchemaFileSystemDataSource {
  val DIRECTORY_PATH = "schema"
  val FILE_FORMAT = ".json"
}
