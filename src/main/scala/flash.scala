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

  import ohnosequences.cosas.properties._
  import ohnosequences.cosas.typeSets._

  sealed trait AnyFlashCommand {

    val name: String
  }
  abstract class FlashCommand(val name: String) extends AnyFlashCommand
  object flash extends FlashCommand("flash"); type flash = flash.type

  trait AnyFlashOption {

    type Commands <: AnyTypeSet.Of[AnyFlashCommand]
    val commands: Commands

    def toSeq: Seq[String]
  }
  abstract class FlashOption[Cmmnds <: AnyTypeSet.Of[AnyFlashCommand]](val commands: Cmmnds) extends AnyFlashOption {

    type Commands = Cmmnds
  }
  class DefaultFlashOption(val toSeq: Seq[String]) extends FlashOption(flash :~: ∅)

  case class minOverlap(val length: Int)          extends DefaultFlashOption( Seq("--minOverlap", s"${length}") )
  case class maxOverlap(val length: Int)          extends DefaultFlashOption( Seq("--maxOverlap", s"${length}") )
  case class threads(val number: Int)             extends DefaultFlashOption( Seq("--threads", s"${number}") )
  case class readLen(val length: Float)           extends DefaultFlashOption( Seq("--readLen", s"${length}") )
  case class fragmentLen(val length: Float)       extends DefaultFlashOption( Seq("--fragment-len", s"${length}") )
  case class fragmentLenStddev(val length: Float) extends DefaultFlashOption( Seq("--fragment-len-stddev", s"${length}") )
  object compress                                 extends DefaultFlashOption( Seq("--compress") )
  object toStdout                                 extends DefaultFlashOption( Seq("--to-stdout") )

  trait FlashInputFile { def toSeq: Seq[String] }

  case class PairedEndFiles(val r1: File, val r2: File) extends FlashInputFile {
    def toSeq: Seq[String] = Seq(r1.getCanonicalPath, r2.getCanonicalPath)
  }

  trait OptionFor[C <: AnyFlashCommand] extends TypePredicate[AnyFlashOption] {

    type Condition[O <: AnyFlashOption] = C isIn O#Commands
  }

  sealed trait AnyFlashCommandArguments {

    type Commands <: AnyTypeSet.Of[AnyFlashCommand]
    val commands: Commands

    val toSeq: Seq[String]
  }

  abstract class FlashCommandArguments[Cmmnds <: AnyTypeSet.Of[AnyFlashCommand]](val commands: Cmmnds)
  extends AnyFlashCommandArguments { type Commands = Cmmnds }

  // FLASH requires input files and just that? nope, more complicated than that.
  /*
    FLASh outputs **5** files:

    - `out.extendedFrags.fastq`      The merged reads.
    - `out.notCombined_1.fastq`      Read 1 of mate pairs that were not merged.
    - `out.notCombined_2.fastq`      Read 2 of mate pairs that were not merged.
    - `out.hist`                     Numeric histogram of merged read lengths.
    - `out.histogram`               Visual histogram of merged read lengths.

    The `out` suffix is configurable. So we should configure the output folder. In the return type we should have either an execution error _or_ the corresponding output files.
  */

  import ohnosequences.cosas.ops.typeSets.{ CheckForAll, ToList }

  case class FlashExec[
    Cmd <: AnyFlashCommand,
    Opts <: AnyTypeSet.Of[AnyFlashOption]
  ](
    val command: Cmd,
    // TODO arguments
    val options: Opts
  )(implicit
    val ev: CheckForAll[Opts, OptionFor[Cmd]],
    val toListEv: ToListOf[Opts, AnyFlashOption]
  )
  {

    def toSeq: Seq[String] =  Seq(command.name) ++
                              ( (options.toListOf[AnyFlashOption]) flatMap { _.toSeq } )
  }

  // case class Cmd(val input: FlashInputFile, val options: List[FlashOption]) {
  //
  //   def toSeq: Seq[String] = Seq("flash") ++ (options flatMap { _.toSeq } ) ++ input.toSeq
  // }

}
