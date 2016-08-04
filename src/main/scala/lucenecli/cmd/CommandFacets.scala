/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli.cmd

import java.io.PrintWriter

import lucenecli.{Command, Context}
import org.apache.lucene.facet.range.{LongRange, LongRangeFacetCounts}
import org.apache.lucene.facet.sortedset.{DefaultSortedSetDocValuesReaderState, SortedSetDocValuesFacetCounts}
import org.apache.lucene.facet.{FacetResult, FacetsCollector, LabelAndValue}
import org.apache.lucene.index.{DocValuesType, IndexReader}
import org.apache.lucene.search.{IndexSearcher, MatchAllDocsQuery}

import scala.collection.JavaConverters._

object CommandFacets extends Command("facets") {

  override def process(params: List[String],
                       opts: Map[String, Any],
                       context: Context): Context = {

    val wrapper = context.wrapper.getOrElse({
      context.out.println("Not connected to any index. Please use the 'use' command.")
      return context.stateless
    })

    if (params.size < 1 || params.size > 2) {
      context.out.println("Syntax error: command 'facets' requires a field name and an optional limit")
      return context.stateless
    }

    val limit = if (params.size == 2) params(1).toInt else 10

    val fieldname = params(0)
    val optFi = wrapper.getFieldInfo(fieldname)
    if (optFi.isEmpty) {
      context.out.println(s"$fieldname is not a valid field")
      return context.stateless
    }

    val optFacets = optFi.get.getDocValuesType match {
      case DocValuesType.NUMERIC => getLongRangesFacets(
        wrapper.reader, fieldname)
      case DocValuesType.SORTED_SET => getSortedSetFacets(
        wrapper.reader, fieldname, limit)
      case _ => None
    }

    if (optFacets.isEmpty) {
      context.out.println("invalid field for facets")
      return context.stateless
    }

    0 until Math.min(limit, optFacets.get.childCount) foreach { i =>
      val lv: LabelAndValue = optFacets.get.labelValues(i)
      context.out.println(s"${lv.label} (${lv.value})")
    }

    return context.stateless
  }

  def getSortedSetFacets(reader: IndexReader,
                         fieldname: String,
                         limit: Int): Option[FacetResult] = {

    val state = new DefaultSortedSetDocValuesReaderState(reader, fieldname)
    val fc = new FacetsCollector
    val searcher = new IndexSearcher(reader)
    val query = new MatchAllDocsQuery
    FacetsCollector.search(searcher, query, 1, fc)
    val facets = new SortedSetDocValuesFacetCounts(state, fc)
    val results = facets.getAllDims(limit).asScala

    // Here, we assume that we only have a single dimension per field. Is
    // that ok?
    results.lift(0)
  }

  def getLongRangesFacets(reader: IndexReader,
                          fieldname: String): Option[FacetResult] = {

    val searcher = new IndexSearcher(reader)
    val query = new MatchAllDocsQuery
    val fc = new FacetsCollector
    FacetsCollector.search(searcher, query, 1, fc)

    // TODO allow user to define ranges limits
    val limits = List(10L, 100L, 1000L, 100000L, 1000000L, Long.MaxValue)
    val ranges = limits.foldLeft(List[LongRange]()) { (ranges, higher) =>
      val lower = if (ranges.isEmpty) 0 else ranges.last.max
      val label =
        if (higher != Long.MaxValue) s"$lower - $higher"
        else s"$lower - MAX"
      ranges :+ new LongRange(label, lower, true, higher, false)
    }

    val facets = new LongRangeFacetCounts(fieldname, fc, ranges:_*)
    Some(facets.getTopChildren(0, fieldname))
  }

  override def help(out: PrintWriter): Unit =
    out.println("\tfacets [field] [limit]\tDisplay facets on a field.")
}
