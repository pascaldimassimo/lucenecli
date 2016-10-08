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
import org.apache.lucene.util.BytesRef

import scala.annotation.tailrec

object CommandTerms extends Command("terms") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    if (params.size < 1) {
      context.out.println("Syntax error: command 'terms' requires a field name and an optional prefix and segment name")
      return context.stateless
    }

    val resume = context.commandToResume.contains(this)
    val segmentNameOpt = params.lift(1)

    val nbToPrint = opts.get("rows")
      .map(_.asInstanceOf[Int]).getOrElse(10)

    val startIndex = if (resume)
      context.state.get("startIndex").get.asInstanceOf[Int]
    else 0

    val readerOpt = if (segmentNameOpt.isDefined) {
      wrapper.getSegmentContext(segmentNameOpt.get).map(
        segmentContext => segmentContext.reader.asInstanceOf[SegmentReader]
      )
    } else {
      // Full index reader
      Some(SlowCompositeReaderWrapper.wrap(wrapper.reader))
    }

    if (readerOpt.isEmpty) {
      context.out.println("Invalid segment name")
      context.stateless
    } else {
      val moreTerms = printTerms(
        readerOpt.get, params.head, opts, startIndex, nbToPrint, context.out)
      if (moreTerms) {
        context.out.println("Type \"it\" for more")
        val state = Map[String, Any](
          "startIndex" -> (startIndex + nbToPrint))
        new Context(context.out, context.wrapper, Some(this), state, params, opts)
      }
      else {
        context.stateless
      }
    }
  }

  def printTerms(reader: LeafReader,
                 fieldname: String,
                 opts: Map[String, Any],
                 start: Int,
                 nb: Int,
                 out: PrintWriter): Boolean = {

    val fi: FieldInfo = reader.getFieldInfos.fieldInfo(fieldname)
    val terms: Terms = reader.terms(fieldname)
    if (terms == null) {
      out.println(s"field $fieldname does not exists")
      return false
    }

    val end = start + nb
    val prefixOpt = opts.get("prefix").map(_.asInstanceOf[String])
    val fullDisplayOpt = opts.get("full").map(_.asInstanceOf[Boolean])

    @tailrec
    def iter(te: TermsEnum,
             current: Int): BytesRef = {

      val term = te.next
      if (term != null && current < end) {
        // TODO use seek when we don't start at 0 (instead of re-iterate each time)
        if (current >= start &&
          (!prefixOpt.isDefined || term.utf8ToString().startsWith(prefixOpt.get))) {
          out.println(s"${term.utf8ToString} (df: ${te.docFreq})")
          if (fullDisplayOpt.isDefined && fullDisplayOpt.get) {
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
          }
        }

        // Don't increment counter if we have a prefix and the terms
        // don't match it yet. Otherwise, increment counter.
        val nextCurrent = if (prefixOpt.isDefined &&
          !term.utf8ToString().startsWith(prefixOpt.get)) current
        else current + 1

        iter(te, nextCurrent)

      } else term
    }

    // Start iterating and return whether or not we reached the end of the
    // iterator
    iter(terms.iterator, 0) != null
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
    out.println("\tterms field [prefix] [segment]\tDisplay terms of whole index or a segment.")
}
