package router

import controllers.SchemaController
import javax.inject.Inject
import play.api.routing.{Router, SimpleRouter}
import play.api.routing.sird.{GET, UrlContext}

final class SchemaRouter @Inject()(controller: SchemaController) extends SimpleRouter {
  override def routes: Router.Routes = {
    case GET(p"/") => controller.index
    case GET(p"/$content") => controller.echo(content)
  }
}
