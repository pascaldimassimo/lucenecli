/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context, LuceneReaderWrapper}
import org.apache.lucene.search.grouping.{GroupingSearch, TopGroups}
import org.apache.lucene.search.{MatchAllDocsQuery, Query}
import org.apache.lucene.util.BytesRef

object CommandGroups extends Command("groups") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    if (params.size != 1) {
      context.out.println("Syntax error: command 'groups' requires a field")
      return context.stateless
    }

    val resume = context.commandToResume.contains(this)

    // TODO have 'nbToPrint' configurable
    val nbToPrint = 10
    val startIndex = if (resume)
      context.state.get("startIndex").get.asInstanceOf[Int]
    else 0

    val moreGroups = printGroups(params(0), startIndex, nbToPrint, wrapper, context.out)
    if (moreGroups) {
      context.out.println("Type \"it\" for more")
      val state = Map[String, Any](
        "startIndex" -> (startIndex + nbToPrint))
      new Context(context.out, context.wrapper, Some(this), state, params, opts)
    }
    else {
      context.stateless
    }
  }

  def printGroups(fieldname: String,
                  start: Int,
                  nb: Int,
                  wrapper: LuceneReaderWrapper,
                  out: PrintWriter): Boolean = {

    val gs: GroupingSearch = new GroupingSearch(fieldname)
    gs.setAllGroups(true)
    gs.setCachingInMB(10, true)
    gs.setGroupDocsLimit(100)
    val q: Query = new MatchAllDocsQuery
    val result: TopGroups[BytesRef] = gs.search(wrapper.searcher, q, start, nb)
    // TODO add sort
    result.groups.foreach(group => {
      val str = group.groupValue.utf8ToString
      val value = if (str.length > 0) str else "[EMPTY VALUE]"
      out.println(s"$value (${group.totalHits})")
    })

    start + nb < result.totalGroupCount
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tgroups [field]\tDisplay groups.")
}
