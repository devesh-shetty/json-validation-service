package router

import controllers.SchemaController
import javax.inject.Inject
import play.api.routing.sird.{GET, POST, UrlContext}
import play.api.routing.{Router, SimpleRouter}

final class SchemaRouter @Inject()(controller: SchemaController) extends SimpleRouter {
  override def routes: Router.Routes = {
    case POST(p"/schema/$schemaId") => controller.uploadSchema(schemaId)
    case GET(p"/schema/$schemaId") => controller.downloadSchema(schemaId)
    case POST(p"/validate/$schemaId") => controller.validateSchemaWith(schemaId)
  }
}
