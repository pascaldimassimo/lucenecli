/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{CommandsList, Command, Context}

object CommandHelp extends Command("help") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    context.out.println("Commands:")
    CommandsList.commands.foreach(_.help(context.out))
    context.stateless
  }

  override def help(out: PrintWriter): Unit =
    out.println("\thelp\tDisplay this help.")
}
