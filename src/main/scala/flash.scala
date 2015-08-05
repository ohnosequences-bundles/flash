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

object flashAPI {

  trait FlashOption { def toSeq: Seq[String] }

  case class minOverlap(val length: Int)          extends FlashOption { def toSeq = Seq("--minOverlap", s"${length}") }
  case class maxOverlap(val length: Int)          extends FlashOption { def toSeq = Seq("--maxOverlap", s"${length}") }
  case class threads(val number: Int)             extends FlashOption { def toSeq = Seq("--threads", s"${number}") }
  case class readLen(val length: Float)           extends FlashOption { def toSeq = Seq("--readLen", s"${length}") }
  case class fragmentLen(val length: Float)       extends FlashOption { def toSeq = Seq("--fragment-len", s"${length}") }
  case class fragmentLenStddev(val length: Float) extends FlashOption { def toSeq = Seq("--fragment-len-stddev", s"${length}") }
  object toStdout                                 extends FlashOption { def toSeq: Seq[String] = Seq("--to-stdout") }
  object compress                                 extends FlashOption { def toSeq: Seq[String] = Seq("--compress") }

  trait FlashInputFile { def toSeq: Seq[String] }

  case class PairedEndFiles(val r1: File, val r2: File) extends FlashInputFile {
    def toSeq: Seq[String] = Seq(r1.getCanonicalPath, r2.getCanonicalPath)
  }

  case class flashCmd(val input: FlashInputFile, val options: List[FlashOption]) {

    def toSeq: Seq[String] = Seq("flash") ++ (options map { x => x.toSeq }).flatten ++ input.toSeq
  }

}
