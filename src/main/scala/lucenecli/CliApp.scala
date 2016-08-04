/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli

object CliApp extends App {

  val cli = new Cli
  cli.run(args)

}
