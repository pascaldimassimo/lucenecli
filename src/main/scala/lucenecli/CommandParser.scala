/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

class CommandParser {

  /**
    * groups: name, params, options
    */
  val patternArgs =
    """(\S+)\s?+([^\{\}]*)(\{.*\})?""".r

  /**
    * http://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
    */
  val patternParameters =
    """[^\s\"']+|\"([^\"]*)\"|'([^']*)'""".r

  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  def parse(line: String): (String, List[String], Map[String, AnyRef]) = line match {
    case patternArgs(name, params, opts) =>
      (name, parseParameters(params), parseJson(opts))
    case _ => throw new IllegalArgumentException("syntax is invalid")
  }

  private def parseParameters(parameters: String) =
    (patternParameters findAllIn parameters).toList

  private def parseJson(json: String) =
      Option(json).map(mapper.readValue[Map[String, Object]](_))
        .getOrElse(Map[String, Object]())

}
