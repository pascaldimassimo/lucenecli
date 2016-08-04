/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.apache.lucene.document.Document

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonPropertyOrder(Array("docID"))
@JsonInclude(Include.NON_NULL)
class MetaDocument(val docID: Integer,
                   val doc: Document) {

  @JsonSerialize(using = classOf[DocumentSerializer])
  @JsonGetter(value = "fields")
  def getDoc: Document = doc
}

@JsonPropertyOrder(Array("docID", "segmentName", "segmentDocID", "isDeleted"))
class MetaDocumentForDocuments(docID: Integer,
                               doc: Document,
                               val segmentDocID: Integer,
                               val segmentName: String,
                               val deleted: Boolean) extends MetaDocument(docID, doc)

@JsonPropertyOrder(Array("docID", "score"))
class MetaDocumentForSearch(docID: Integer,
                            doc: Document,
                            val score: Float) extends MetaDocument(docID, doc)
