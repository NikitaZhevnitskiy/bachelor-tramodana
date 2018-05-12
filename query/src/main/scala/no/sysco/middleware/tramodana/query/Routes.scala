package no.sysco.middleware.tramodana.query

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._

class Routes(streamsService: StreamsService) extends BpmnJsonProtocol {


  val route =
    pathPrefix("query") {
      pathPrefix("healthcheck") {
        get {
          complete("OK")
        }
      } ~
        pathPrefix("keys") {
          pathEnd {
            complete(streamsService.getAllValues())
          } ~
            path(Segment) { segment =>
              val bpmnF = streamsService.getValueByKey(segment)
              onSuccess(bpmnF) {
                case Some(v) => complete(v)
                case None => complete(StatusCodes.NotFound)
              }
            }
        }

    }

}
