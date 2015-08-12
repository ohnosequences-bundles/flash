package ohnosequencesBundles.statika

import ohnosequences.statika._, bundles._, instructions._
import java.io.File

abstract class Flash(val version: String) extends Bundle(compressinglibs) { flash =>

  private def workingDir: String = (new File("")).getCanonicalPath().toString

  val tarball: String = s"FLASH-${flash.version}.tar.gz"
  val folder: String = s"FLASH-${flash.version}"
  val flashBin: String = "flash"

  lazy val getTarball = Seq(
    "wget",
    s"https://s3-eu-west-1.amazonaws.com/resources.ohnosequences.com/flash/${flash.version}/${flash.tarball}"
  )

  lazy val extractTarball = Seq(
    "tar",
    "-xvzf",
    s"${flash.tarball}"
  )

  lazy val compile = Seq(
    "make",
    "-C",
    s"${workingDir}/${flash.folder}"
  )

  lazy val linkBinaries = Seq(
    "ln",
    "-s",
    s"${workingDir}/${flash.folder}/${flash.flashBin}",
    s"/usr/bin/${flash.flashBin}"
  )

  final def install: Results =
    getTarball ->- extractTarball ->- compile ->- linkBinaries ->- success(s"${bundleName} installed")
}
