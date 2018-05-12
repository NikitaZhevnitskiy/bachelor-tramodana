package no.sysco.middleware.tramodana.query

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._

class Routes(streamsService: StreamsService) {
  import StatusCodes._

  val route =
    pathPrefix("query") {
      pathPrefix("healthcheck") {
        get {
          complete("OK")
        }
      } ~
      pathPrefix("keys"){
        pathEnd {
          streamsService.getAllValues()
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Get all values</h1>"))
        } ~
        path(Segment) { segment =>
          streamsService.getValueByKey(segment)
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Get value by key</h1>"))
        }
      }
    }


}
