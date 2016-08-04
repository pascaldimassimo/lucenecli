/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context}
import org.apache.lucene.index.SegmentCommitInfo

import scala.collection.JavaConverters._

object CommandSegments extends Command("segments") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    val sis = wrapper.sis
    for (sci: SegmentCommitInfo <- sis.asScala) {
      context.out.println(s"Segment name: ${sci.info.name}")
      context.out.println(s"\tSegment codec: ${sci.info.getCodec}")
      context.out.println(s"\tSegment version: ${sci.info.getVersion}")
      context.out.println(s"\tSegment doc count (deletions are not considered): ${sci.info.maxDoc}")
      context.out.println(s"\tSegment coumpound format: ${sci.info.getUseCompoundFile}")
      context.out.println(s"\tSegment files: ${sci.info.files}")
    }

    context.stateless
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tsegments\tList all segments of the current generation.")
}
