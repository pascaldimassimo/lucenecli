/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context, LuceneReaderWrapper}

import scala.util.Try

object CommandUse extends Command("use") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    if (params.isEmpty) {
      context.out.println("Missing parameter for 'use'")
      return context.stateless
    }

    context.wrapper.map(_.close)
    val wrapper = Try(new LuceneReaderWrapper(params.head))
    if (wrapper.isSuccess) {
      context.out.println("Using index " + wrapper.get.fileIndexPath)
      new Context(context.out, Some(wrapper.get))
    } else {
      context.out.println("Invalid index path")
      context.stateless
    }
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tuse\tSwitch to a Lucene index.")
}
