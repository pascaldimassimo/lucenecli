/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context}
import org.apache.lucene.index._

object CommandPoints extends Command("points") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    if (params.size != 1) {
      context.out.println("Syntax error: command 'points' requires a field name")
      return context.stateless
    }

    val fieldname = params(0)
    val optFi = wrapper.getFieldInfo(fieldname)
    if (optFi.isEmpty) {
      context.out.println(s"$fieldname is not a valid field")
      return context.stateless
    }

    context.out.println(s"Number of points: ${PointValues.size(wrapper.reader, fieldname)}")
    context.out.println(s"Number of documents: ${PointValues.getDocCount(wrapper.reader, fieldname)}")

    context.stateless
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tpoints [segment] [field]\tDisplay basic info about a points field across the whole index.")
}
