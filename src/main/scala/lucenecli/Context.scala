/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli

import java.io.PrintWriter

class Context(val out: PrintWriter,
              val wrapper: Option[LuceneReaderWrapper] = None,
              val commandToResume: Option[Command] = None,
              val state: Map[String, Any] = Map(),
              val params: List[String] = List(),
              val opts: Map[String, Any] = Map()) {

  def stateless: Context = new Context(out, wrapper)

}
