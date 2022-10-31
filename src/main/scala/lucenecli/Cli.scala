/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli

import java.io.{File, PrintWriter}

import jline.TerminalFactory
import jline.console.ConsoleReader
import jline.console.completer.StringsCompleter
import jline.console.history.FileHistory
import lucenecli.cmd.{CommandExit, CommandIt, CommandUse}

import scala.annotation.tailrec
import scala.collection.JavaConverters._

class Cli {

  val parser = new CommandParser

  val historyFilename = s"${System.getProperty("user.home")}/.lucenecli"
  val history = new FileHistory(new File(historyFilename))

  val reader = new ConsoleReader
  reader.setPrompt("> ")
  reader.setHistory(history)

  val completer = new StringsCompleter(CommandsList.commandsNames.asJava)
  reader.addCompleter(completer)

  val out = new PrintWriter(reader.getOutput, true)
  out.println("Lucene CLI Version 1.1.0")

  def run(args: Array[String]) = {

    val context = if (args.length > 0) {
      CommandUse.process(args.toList, Map(), new Context(out))
    } else new Context(out)

    // Main loop
    loop(reader, context)

    out.println("Exiting...")

    // TODO needed?
    TerminalFactory.get.restore

    context.wrapper.map(_.close)

    history.flush()
  }

  @tailrec
  final def loop(reader: ConsoleReader, context: Context): Unit = {
    val line = Option(reader.readLine)
    if (line.isDefined) {
      if (line.get.trim.isEmpty) {
        loop(reader, context)
      } else {
        val (name, params, opts) = parser.parse(line.get)
        val newContext = CommandsList.findCommand(name) match {
          case Some(CommandExit) => return
          case Some(CommandIt) => {
            context.commandToResume.map(cmd => {
              cmd.process(context.params, context.opts, context)
            }).getOrElse({
              context.out.println("No cursor.")
              context.stateless
            })
          }
          case Some(cmd: Command) => cmd.process(params, opts, context.stateless)
          case None => out.println("Invalid command"); context
        }

        loop(reader, newContext)
      }
    }
  }

}
