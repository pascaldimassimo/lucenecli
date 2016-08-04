/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli

import java.io.PrintWriter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.lucene.document.Document

abstract class Command(val name: String) {

  def process(params: List[String],
              opts: Map[String, Any],
              context: Context): Context

  def help(out: PrintWriter)

  protected val mapper = new ObjectMapper
  mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)

  /**
    * Print Lucene Document
    *
    * @param id  id
    * @param doc Lucene Document
    * @param fl  stored fieldnames to display.
    *            If None, don't display fields.
    *            If empty list, show all fields.
    *            If a string, only show that field.
    *            If non empty list, show fields from list
    * @param out PrintWriter
    */
  protected def printDocument(id: Int,
                              doc: Document,
                              fl: Option[Any],
                              out: PrintWriter) = {
    val meta: MetaDocument = new MetaDocument(id, doc)
    printMetaDocument(meta, fl, out)
    out.println("")
  }

  /**
    * Print Meta Document
    *
    * @param meta meta document
    * @param fl   stored fieldnames to display.
    *             If None, don't display fields.
    *             If empty list, show all fields.
    *             If a string, only show that field.
    *             If non empty list, show fields from list
    * @param out  PrintWriter
    */
  protected def printMetaDocument(meta: MetaDocument,
                                  fl: Option[Any],
                                  out: PrintWriter) = {

    val writer = mapper.writer.withAttribute(
      DocumentSerializer.DOC_FIELDS_TO_PRINT, fl)
    writer.writeValue(out, meta)
  }

}
