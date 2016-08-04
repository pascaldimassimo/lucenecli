/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context, LuceneReaderWrapper}
import org.apache.lucene.index.TermsEnum

object CommandVectors extends Command("vectors") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    if (params.size < 1 || params.size > 2) {
      context.out.println("Syntax error: command 'vectors' requires a field and an optional doc ID")
      return context.stateless
    }

    val resume = context.commandToResume.contains(this)

    val fieldname = params.head
    val optFi = wrapper.getFieldInfo(fieldname)
    if (optFi.isEmpty) {
      context.out.println(s"$fieldname is not a valid field")
      return context.stateless
    }

    if (!optFi.get.hasVectors) {
      context.out.println(s"$fieldname does not have terms vectors")
      return context.stateless
    }

    if (params.size == 2) {
      printSingleDocTermsVector(
        params(0), params(1).toInt, wrapper, context.out)
      return context.stateless
    }

    val nbToPrint = opts.getOrElse("rows", 10).asInstanceOf[Integer]
    val startIndex = if (resume)
      context.state.get("startIndex").get.asInstanceOf[Int]
    else 0

    val moreDocs = printTermsVectors(
      params(0), startIndex, nbToPrint, wrapper, context.out)
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

  def printSingleDocTermsVector(fieldname: String,
                                docID: Int,
                                wrapper: LuceneReaderWrapper,
                                out: PrintWriter): Unit = {

    wrapper.getTermVector(docID, fieldname).map(
      tv => {
        val te: TermsEnum = tv.iterator
        Stream.continually(te.next)
          .takeWhile(_ != null)
          .foreach(term => out.print(s"[${term.utf8ToString}] "))
      })

    out.println
  }

  def printTermsVectors(fieldname: String,
                        start: Int,
                        nbToPrint: Int,
                        wrapper: LuceneReaderWrapper,
                        out: PrintWriter): Boolean = {

    val end = Math.min(start + nbToPrint, wrapper.numDocs)
    (start until end).foreach(i => {
      out.print(s"$i: ")
      printSingleDocTermsVector(fieldname, i, wrapper, out)
    })

    end < wrapper.numDocs
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tvectors [field]\tDisplay terms vectors.")
}
