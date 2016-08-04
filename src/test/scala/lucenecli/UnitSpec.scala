/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli

import org.scalatest._

abstract class UnitSpec extends FlatSpec with Matchers with
OptionValues with Inside with Inspectors
