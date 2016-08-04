/*
 * This file is subject to the terms and conditions defined in
 * files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
 * source code package.
 */
package lucenecli

object Utils {

  def bytesToHex (bytes: Array[Byte], offset: Int, len: Int): String =
    bytes.drop(offset).take(len).map("%02X" format _).mkString

}
