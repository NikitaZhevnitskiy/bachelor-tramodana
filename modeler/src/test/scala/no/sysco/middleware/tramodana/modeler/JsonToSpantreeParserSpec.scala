package no.sysco.middleware.tramodana.modeler

import no.sysco.middleware.tramodana.schema.JsonSpanProtocol
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JsonToSpantreeParserSpec extends WordSpec with JsonSpanProtocol{


  "A json string storing Spantree data" when {
    "storing several Spantrees" should{
      "parse to a list of Spantrees with the same count of Spantrees" in{
        
      }
    }
  }

}
