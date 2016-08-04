/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context, MetaDocument, MetaDocumentForSearch}
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.search.{ScoreDoc, TopDocs}

object CommandSearch extends Command("search") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    if (params.size != 1) {
      context.out.println("Syntax error: command 'search' has exactly one parameter (the search query)")
      return context.stateless
    }

    val resume = context.commandToResume.contains(this)

    val df = opts.get("df")
      .map(v => v match {
        case s: String => List(s)
        case l: List[_] => l.map(_.toString)
      })
      .getOrElse(wrapper.getAllFields().map(_.name))
      .toArray[String]

    // TODO make analyzer configurable
    val parser = new MultiFieldQueryParser(df, new SimpleAnalyzer)
    val searcher = wrapper.searcher
    val query = parser.parse(params.head)

    // TODO make n configurable
    val n = 10
    val hits: TopDocs = if (resume) {
      val sd = context.state.get("lastResult").get.asInstanceOf[ScoreDoc]
      searcher.searchAfter(sd, query, n)
    } else {
      searcher.search(query, n)
    }

    if (!resume) {
      // Only print this on new search
      context.out.println(s"${hits.totalHits} results")
    }

    val fl = opts.get("fl")
    for (scoreDoc <- hits.scoreDocs) {
      val doc = if (fl.isDefined)
        searcher.doc(scoreDoc.doc) else null
      val meta: MetaDocument = new MetaDocumentForSearch(
        scoreDoc.doc, doc, scoreDoc.score)
      printMetaDocument(meta, fl, context.out)
      context.out.println("")
    }

    val nbResults = (if (resume)
      context.state.get("nbResults").get.asInstanceOf[Int]
    else 0) + hits.scoreDocs.length

    if (nbResults < hits.totalHits) {
      context.out.println("Type \"it\" for more")
      val state = Map[String, Any](
        "lastResult" -> hits.scoreDocs.last,
        "nbResults" -> nbResults)
      new Context(context.out, context.wrapper, Some(this), state, params, opts)
    }
    else {
      context.stateless
    }
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tsearch\tSearch documents.")
}
