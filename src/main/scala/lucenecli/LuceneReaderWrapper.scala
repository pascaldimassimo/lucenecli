/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli

import java.io.{IOException, File}

import org.apache.lucene.index._
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory

import scala.collection.JavaConverters._

class LuceneReaderWrapper(indexPath: String) {

  val fileIndexPath = if (indexPath.startsWith("~" + File.separator)) {
    new File(System.getProperty("user.home") + indexPath.substring(1))
  } else {
    new File(indexPath)
  }

  if (!fileIndexPath.exists || !fileIndexPath.isDirectory) {
    throw new IllegalArgumentException(indexPath + " is not a valid index path!")
  }

  private val dir = FSDirectory.open(fileIndexPath.toPath)
  val reader = DirectoryReader.open(dir)

  def close = {
    reader.close
    dir.close
  }

  def searcher = new IndexSearcher(reader)

  def getDocument(id: Int) = searcher.doc(id)

  def getSegmentContext(segmentName: String): Option[LeafReaderContext] =
    leaves.find(_.reader.asInstanceOf[SegmentReader].getSegmentName == segmentName)

  def leaves = reader.leaves.asScala.toList

  def sis = SegmentInfos.readLatestCommit(dir)

  def getAllFields(): List[FieldInfo] = {
    val m = scala.collection.mutable.Map[String, FieldInfo]()
    for (leaf <- leaves) {
      for (info <- leaf.reader().getFieldInfos.asScala) {
        m.put(info.name, info)
      }
    }
    m.values.toList.sortWith(_.name < _.name)
  }

  def getFieldInfo(fieldname: String): Option[FieldInfo] =
    getAllFields.find(_.name == fieldname)

  def numDocs = reader.numDocs

  def getTermVector(docID: Int, fieldname: String): Option[Terms] =
    Option(reader.getTermVector(docID, fieldname))

}
