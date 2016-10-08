/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context}
import org.apache.lucene.index._

import scala.collection.JavaConverters._

object CommandFields extends Command("fields") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    val fis = if (!params.isEmpty) {
      val segmentContext =
        wrapper.getSegmentContext(params.head).getOrElse({
          context.out.println("Invalid segment name")
          return context.stateless
        })
      segmentContext.reader.getFieldInfos.asScala.toList
    } else {
      wrapper.getAllFields()
    }
    printFieldInfos(fis, context.out)

    context.stateless
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tfields [segment name]\tDisplay fields of the index or of a single segment.")

  def printFieldInfos(fis: List[FieldInfo], out: PrintWriter) {
    for (fi <- fis) {
      out.println(s"${fi.name}:")
      if (fi.getIndexOptions ne IndexOptions.NONE) {
        out.println(s"\tIndexed (${fi.getIndexOptions})")
      }
      if (fi.hasVectors) {
        out.println("\tVectors")
      }
      if (fi.hasPayloads) {
        out.println("\tPayloads")
      }
      if (fi.hasNorms) {
        out.println("\tNorms")
      }
      if (fi.getDocValuesType ne DocValuesType.NONE) {
        out.println(s"\tDocValues (${fi.getDocValuesType})")
      }
    }
  }
}
