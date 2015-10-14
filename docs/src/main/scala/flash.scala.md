
```scala
package ohnosequencesBundles.statika

import ohnosequences.statika._, bundles._, instructions._
import java.io.File


abstract class Flash(val version: String) extends Bundle(compressinglibs, cdevel) { flash =>

  lazy val tarball: String = s"FLASH-${flash.version}.tar.gz"
  lazy val folder: String = s"FLASH-${flash.version}"
  lazy val flashBin: String = "flash"

  lazy val getTarball = cmd("wget")(
    s"https://s3-eu-west-1.amazonaws.com/resources.ohnosequences.com/flash/${flash.version}/${flash.tarball}"
  )

  lazy val extractTarball = cmd("tar")( "-xvzf", flash.tarball )

  lazy val compile = cmd("make")("-C", flash.folder)

  lazy val linkBinaries = cmd("ln")(
    "-s",
    new File(s"${flash.folder}/${flash.flashBin}").getCanonicalPath,
    s"/usr/bin/${flash.flashBin}"
  )

  def instructions: AnyInstructions = getTarball -&- extractTarball -&- compile -&- linkBinaries

}

```




[main/scala/flash.scala]: flash.scala.md