/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli

import lucenecli.cmd._

object CommandsList {

  val commands = List(
    CommandHelp,
    CommandDocuments,
    CommandExit,
    CommandFields,
    CommandInfo,
    CommandIt,
    CommandSearch,
    CommandSegments,
    CommandUse,
    CommandDocValues,
    CommandFacets,
    CommandTerms,
    CommandGroups,
    CommandVectors,
    CommandDocument
  )

  val commandsNames = commands.map(_.name)

  def findCommand(name: String) = commands.find(_.name == name)

}
