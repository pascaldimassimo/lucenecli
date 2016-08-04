/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context, LuceneReaderWrapper, Utils}
import org.apache.lucene.index._
import org.apache.lucene.util.BytesRef

import scala.annotation.tailrec

object CommandDocValues extends Command("docvalues") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    if (params.size < 1 || params.size > 2) {
      context.out.println("Syntax error: command 'docvalues' needs a docvalue name and an optional segment")
      return context.stateless
    }

    val resume = context.commandToResume.contains(this)
    val dvName = params.head
    val segmentNameOpt = params.lift(1)

    // TODO have that value configurable
    val nbToPrint: Int = 10
    val startIndex = if (resume)
      context.state.get("startIndex").get.asInstanceOf[Int]
    else 0

    val moreDocs = segmentNameOpt.map(name => {
      val segmentContext = wrapper.getSegmentContext(name)
      printSegmentDocValues(
        segmentContext, dvName, startIndex, nbToPrint, opts, context.out)
    }).getOrElse(
      printCombinedDocValues(
        wrapper, dvName, startIndex, nbToPrint, opts, context.out)
    )

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

  def printSegmentDocValues(contextOpt: Option[LeafReaderContext],
                            dvName: String,
                            start: Int,
                            nb: Int,
                            opts: Map[String, Any],
                            out: PrintWriter): Boolean = {

    contextOpt.map(context => {
      val count = printDocValues(context, dvName, start, nb, opts, out)
      start + count < context.reader.numDocs
    }).getOrElse({
      out.println("Invalid segment name")
      false
    })
  }

  def printCombinedDocValues(wrapper: LuceneReaderWrapper,
                             dvName: String,
                             start: Int,
                             nb: Int,
                             opts: Map[String, Any],
                             out: PrintWriter): Boolean = {

    @tailrec
    def printSegment(leaves: Iterator[LeafReaderContext],
                     index: Int,
                     remaining: Int): Int = {
      if (remaining > 0 && leaves.hasNext) {
        val leaf = leaves.next()
        val contextStart = index - leaf.docBase
        val count = printDocValues(
          leaf, dvName, contextStart, remaining, opts, out)
        printSegment(leaves, index + count, remaining - count)
      } else {
        index
      }
    }

    printSegment(wrapper.leaves.iterator, start, nb) < wrapper.numDocs
  }

  def printDocValues(context: LeafReaderContext,
                     dvName: String,
                     start: Int,
                     nb: Int,
                     opts: Map[String, Any],
                     out: PrintWriter): Int = {

    val segmentReader = context.reader.asInstanceOf[SegmentReader]
    val end = start + Math.min(segmentReader.numDocs - start, nb)
    val fi: FieldInfo = segmentReader.getFieldInfos.fieldInfo(dvName)
    if (fi != null) {
      start until end foreach { i =>
        val value = fi.getDocValuesType match {
          case DocValuesType.BINARY =>
            getBinaryDocValue(dvName, segmentReader, i)
          case DocValuesType.NUMERIC =>
            getNumericDocValue(dvName, segmentReader, i)
          case DocValuesType.SORTED =>
            getSortedDocValue(dvName, segmentReader, i)
          case DocValuesType.SORTED_SET =>
            getSortedSetDocValue(dvName, segmentReader, i)
          case _ =>
            throw new IllegalStateException("TODO implement other DV type!")
        }
        out.println((context.docBase + i + ": " + value))
      }
    }
    end - start
  }

  def getBinaryDocValue(dvName: String,
                        segmentReader: SegmentReader,
                        docId: Int): String = {
    val bdv: BinaryDocValues = segmentReader.getBinaryDocValues(dvName)
    val bytes: BytesRef = bdv.get(docId)
    Utils.bytesToHex(bytes.bytes, bytes.offset, bytes.length)
  }

  def getNumericDocValue(dvName: String,
                         segmentReader: SegmentReader,
                         docId: Int): Long = {
    val ndv: NumericDocValues = segmentReader.getNumericDocValues(dvName)
    ndv.get(docId)
  }

  def getSortedDocValue(dvName: String,
                        segmentReader: SegmentReader,
                        docId: Int): String = {
    val sdv: SortedDocValues = segmentReader.getSortedDocValues(dvName)
    val bytes: BytesRef = sdv.get(docId)
    bytes.utf8ToString
  }

  def getSortedSetDocValue(dvName: String,
                           segmentReader: SegmentReader,
                           docId: Int): List[String] = {

    val ssdv: SortedSetDocValues = segmentReader.getSortedSetDocValues(dvName)
    ssdv.setDocument(docId)

    Stream.continually(ssdv.nextOrd)
      .takeWhile(_ != SortedSetDocValues.NO_MORE_ORDS)
      .map(ssdv.lookupOrd(_).utf8ToString)
      .toList
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tdocvalues [segment name]\tDisplay DocValues of the index or of a single segment.")
}
