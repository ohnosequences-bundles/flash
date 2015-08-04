package ohnosequencesBundles.statika.test

import java.io.File
import org.scalatest.FunSuite
import ohnosequencesBundles.statika._, flashAPI._

class FlashAPITest extends FunSuite {

  test("Flash API command generation") {

    assert(
      Seq(
        "flash",
        "--minOverlap",
        "3",
        "--maxOverlap",
        "100",
        (new File("reads.1.fastq")).getCanonicalPath.toString,
        (new File("reads.2.fastq")).getCanonicalPath.toString
      )
        ===
      flashCmd(
        options = List(
          minOverlap(3),
          maxOverlap(100)
        ),
        input = PairedEndFiles(
          r1 = new File("reads.1.fastq"),
          r2 = new File("reads.2.fastq")
        )
      ).toSeq
    )
  }
}
