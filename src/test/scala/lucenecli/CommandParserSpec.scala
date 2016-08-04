/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli

class CommandParserSpec extends UnitSpec {

  "parser" should "support name only" in {
    val parser = new CommandParser
    val (name, params, opts) = parser.parse("cmd")
    assert(name == "cmd")
    assert(params.isEmpty)
    assert(opts.isEmpty)
  }

  it should "support name and params" in {
    val parser = new CommandParser
    val (name, params, opts) = parser.parse("cmd param1")
    assert(name == "cmd")
    assert(params.size == 1)
    assert(params.head == "param1")
    assert(opts.isEmpty)
  }

  it should "support name params and options" in {
    val parser = new CommandParser
    val (name, params, opts) = parser.parse("cmd param1 {\"field\":1}")
    assert(name == "cmd")
    assert(params.size == 1)
    assert(params.head == "param1")
    assert(opts.size == 1)
    assert(opts.get("field").get === 1)
  }

  it should "support name and options" in {
    val parser = new CommandParser
    val (name, params, opts) = parser.parse("cmd {\"field\":1}")
    assert(name == "cmd")
    assert(params.isEmpty)
    assert(opts.size == 1)
    assert(opts.get("field").get === 1)
  }

  it should "support multiple params" in {
    val parser = new CommandParser
    val (_, params, _) = parser.parse("cmd param1 param2")
    assert(params.size == 2)
    assert(params.head == "param1")
    assert(params(1) == "param2")
  }

  it should "support params in quotes" in {
    val parser = new CommandParser
    val (_, params, _) = parser.parse("cmd \"a b c\" x")
    assert(params.size == 2)
    assert(params.head == "\"a b c\"")
    assert(params(1) == "x")
  }

  it should "throw IllegalArgumentException on missing start bracket" in {
    val parser = new CommandParser
    intercept[IllegalArgumentException] {
      parser.parse("cmd }")
    }
  }

  it should "throw IllegalArgumentException on missing end bracket" in {
    val parser = new CommandParser
    intercept[IllegalArgumentException] {
      parser.parse("cmd {")
    }
  }

}
