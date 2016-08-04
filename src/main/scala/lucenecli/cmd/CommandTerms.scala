/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context, Utils}
import org.apache.lucene.index.IndexOptions._
import org.apache.lucene.index._
import org.apache.lucene.search.DocIdSetIterator

object CommandTerms extends Command("terms") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    if (params.size != 2) {
      context.out.println("Syntax error: command 'terms' requires a segment name and a field name")
      return context.stateless
    }

    wrapper.getSegmentContext(params.head).map(segmentContext => {
      printTerms(segmentContext, params(1), context.out)
    }).getOrElse({
      context.out.println("Invalid segment name")
    })

    context.stateless
  }

  def printTerms(segmentContext: LeafReaderContext,
                 fieldname: String,
                 out: PrintWriter) {

    val reader = segmentContext.reader.asInstanceOf[SegmentReader]
    val fi: FieldInfo = reader.getFieldInfos.fieldInfo(fieldname)
    val terms: Terms = reader.terms(fieldname)
    if (terms == null) {
      out.println(s"field $fieldname does not exists")
      return
    }
    val te: TermsEnum = terms.iterator

    Stream.continually(te.next)
      .takeWhile(_ != null)
      .foreach(term => {

        out.println(s"${term.utf8ToString} (df: ${te.docFreq})")
        fi.getIndexOptions match {
          case DOCS =>
            printDocs(te, false, out)
          case DOCS_AND_FREQS =>
            printDocs(te, true, out)
          case DOCS_AND_FREQS_AND_POSITIONS =>
            printDocsAndPosition(te, out)
          case DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS =>
            printDocsAndPosition(te, out)
          case NONE =>
        }
      })
  }

  private def printDocs(te: TermsEnum, hasFreq: Boolean, out: PrintWriter) {
    val postings = te.postings(null);
    Stream.continually(postings.nextDoc)
      .takeWhile(_ != DocIdSetIterator.NO_MORE_DOCS)
      .foreach(docId => {
        out.print(s"\tdocID: $docId")
        if (hasFreq) {
          out.print(s", freq: ${postings.freq}")
        }
        out.println
      })
  }

  private def printDocsAndPosition(te: TermsEnum, out: PrintWriter) {
    val postings = te.postings(null, PostingsEnum.POSITIONS);
    Stream.continually(postings.nextDoc)
      .takeWhile(_ != DocIdSetIterator.NO_MORE_DOCS)
      .foreach(docId => {

        val pos = (1 to postings.freq).map(_ =>
          if (postings.startOffset != -1)
            s"${postings.nextPosition} (${postings.startOffset}-${postings.endOffset})"
          else
            s"${postings.nextPosition}")
          .mkString("[", ", ", "]")
        out.print(s"\tdocID: $docId, freq: ${postings.freq}, positions: $pos")

        Option(postings.getPayload).map(payload => {
          val payloadValue = Utils.bytesToHex(
            payload.bytes, payload.offset, payload.length)
          out.print(s", payload: $payloadValue")
        })

        out.println
      })
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tterms [segment] [field]\tDisplay terms of a segment.")
}
