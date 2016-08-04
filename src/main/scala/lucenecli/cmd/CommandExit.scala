/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context}

object CommandExit extends Command("exit") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {
    // NOOP
    context
  }

  override def help(out: PrintWriter): Unit =
    out.println("\texit\tTo exit.")
}
