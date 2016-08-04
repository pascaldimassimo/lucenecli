/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context}

object CommandInfo extends Command("info") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    context.out.println("Index: " + wrapper.fileIndexPath)
    context.out.println("Num docs: " + wrapper.reader.numDocs)
    context.out.println("Num deleted docs: " + wrapper.reader.numDeletedDocs)
    context.out.println("Max docs: " + wrapper.reader.maxDoc)

    val sis = wrapper.sis
    context.out.println("Current generation: " + sis.getGeneration)
    context.out.println("Last succesfully read or written generation: " + sis.getLastGeneration)
    context.out.println("The segments_N filename in use by this segment infos: " + sis.getSegmentsFileName)
    context.out.println("Version number when this SegmentInfos was generated: " + sis.getVersion)
    context.out.println("Number of segments in current generation: " + sis.size)

    context.stateless
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tinfo\tDisplay info about the current Lucene index.")
}
