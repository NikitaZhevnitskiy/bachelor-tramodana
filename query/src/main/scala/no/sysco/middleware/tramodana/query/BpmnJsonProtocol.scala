package no.sysco.middleware.tramodana.query

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


final case class BpmnFlow(operationName: String, xml: String)

trait BpmnJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit def bpmnFlowFormat: RootJsonFormat[BpmnFlow] = jsonFormat(BpmnFlow, "operation_name", "xml")
}
