/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import org.apache.lucene.document.Document

import scala.collection.JavaConverters._

object DocumentSerializer {
  val DOC_FIELDS_TO_PRINT: String = "docFieldsToPrint"
}

class DocumentSerializer extends JsonSerializer[Document] {

  def serialize(doc: Document,
                generator: JsonGenerator,
                provider: SerializerProvider) = {

    val att = provider.getAttribute(
      DocumentSerializer.DOC_FIELDS_TO_PRINT).asInstanceOf[Option[Any]]

    // If None, don't display fields.
    // If empty list, show all fields.
    // If a string, only show that field.
    // If non empty list, show fields from list
    att.map { fl =>
      val fieldsToPrint: List[_] = fl match {
        case l: List[_] => l
        case s: String => List(s)
      }

      generator.writeStartObject
      doc.getFields.asScala.foreach(field => {
        if (fieldsToPrint.isEmpty || fieldsToPrint.contains(field.name)) {
          generator.writeStringField(field.name, field.stringValue)
        }
      })
      generator.writeEndObject
    }
  }

}
