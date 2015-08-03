package ohnosequencesBundles.statika

import ohnosequences.statika._, bundles._, instructions._
import java.io.File

abstract class Flash(val version: String) extends Bundle(compressinglibs) { flash =>

  private def workingDir: String = (new File("")).getCanonicalPath().toString

  val tarball: String = s"FLASH-${flash.version}.tar.gz"
  val folder: String = s"FLASH-${flash.version}"
  val flashBin: String = "flash"

  final def install: Results = {

    Seq(
      "wget",
      s"http://s3-eu-west-1.amazonaws.com/resources.ohnosequences.com/flash/${flash.version}/${flash.tarball}"
    ) ->-
    Seq(
      "make",
      "-C",
      s"${workingDir}/${flash.folder}"
    ) ->-
    Seq(
      "ln",
      "-s",
      s"${workingDir}/${flash.folder}/${flash.flashBin}",
      s"/usr/bin/${flash.flashBin}"
    ) ->-
    success(s"${bundleName} is installed")
  }

}
