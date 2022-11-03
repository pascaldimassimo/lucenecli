/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context, MetaDocumentForDocuments}
import org.apache.lucene.document.Document
import org.apache.lucene.index.{CompositeReader, LeafReaderContext, SegmentReader}

import scala.collection.JavaConverters._
import scala.util.control.Breaks._

object CommandDocuments extends Command("documents") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    if (params.size > 1) {
      context.out.println("Syntax error: command 'documents' only support 1 segment name")
      return context.stateless
    }

    val resume = context.commandToResume.contains(this)

    // TODO have 'nbToPrint' configurable
    val nbToPrint = 10
    val startIndex = if (resume)
      context.state.get("startIndex").get.asInstanceOf[Int]
    else 0

    val moreDocs = if (params.nonEmpty) {
      wrapper.getSegmentContext(params.head).map(segmentContext => {
        val count = printLeafDocuments(
          segmentContext, startIndex, nbToPrint, opts, context.out)
        startIndex + count < wrapper.reader.numDocs()
      }).getOrElse({
        context.out.println("Invalid segment name")
        return context.stateless
      })
    }
    else {
      printDocuments(
        startIndex, nbToPrint, opts, wrapper.reader, context.out)
    }

    if (moreDocs) {
      context.out.println("Type \"it\" for more")
      val state = Map[String, Any](
        "startIndex" -> (startIndex + nbToPrint))
      new Context(context.out, context.wrapper, Some(this), state, params, opts)
    }
    else {
      context.stateless
    }
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tdocuments [segment name]\tDisplay documents of a segment or the whole index.")

  /**
    * @param start start index at the top reader level
    * @param nb number of documents to print
    * @param options
    * @return if there is more documents to be printed
    */
  def printDocuments(start: Int,
                     nb: Int,
                     options: Map[String, Any],
                     reader: CompositeReader,
                     out: PrintWriter): Boolean = {

    var index: Int = start
    var remaining: Int = nb
    breakable {
      for (leaf: LeafReaderContext <- reader.leaves.asScala) {
        val leafReader = leaf.reader.asInstanceOf[SegmentReader]
        if (leafReader.numDocs > 0 && leaf.docBase + leafReader.numDocs >= start) {
          val contextStart: Int = index - leaf.docBase
          index += printLeafDocuments(leaf, contextStart, remaining, options, out)
          val count: Int = index - start
          if (count == nb) {
            break
          }
          else if (count > nb) {
            throw new IllegalStateException("count should not be higher than nb")
          }
          remaining = nb - count
        }
      }
    }
    index < reader.numDocs
  }

  private def printLeafDocuments(segmentContext: LeafReaderContext,
                                 start: Int,
                                 nb: Int,
                                 options: Map[String, Any],
                                 out: PrintWriter): Int = {
    var count: Int = 0
    val segmentReader: SegmentReader =
      segmentContext.reader.asInstanceOf[SegmentReader]

    Stream.from(start).takeWhile(
      _ < segmentReader.numDocs && count < nb).foreach(i => {

      val index = segmentContext.docBase + i
      val isLive = Option(segmentReader.getLiveDocs).map(
        _.get(i)).getOrElse(true)

      val fl = options.get("fl")
      val doc = if (isLive && fl.isDefined)
        segmentReader.document(i) else null

      val meta = new MetaDocumentForDocuments(
        index, doc, i, segmentReader.getSegmentName, !isLive)
      printMetaDocument(meta, fl, out)
      out.println("")
      count += 1
    })

    count
  }
}
