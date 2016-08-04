/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context}

object CommandDocument extends Command("document") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    if (params.size != 1) {
      context.out.println("Syntax error: command 'document' only support a document ID")
      return context.stateless
    }

    val id = params.head.toInt
    val doc = wrapper.getDocument(id)
    // If no fl, return an empty list to show all fields
    val fl = Some(opts.get("fl").getOrElse(List()))
    printDocument(id, doc, fl, context.out)

    // TODO DRY all return statements
    context.stateless
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tdocument\tDisplay a document using Lucene internal ID.")

}
