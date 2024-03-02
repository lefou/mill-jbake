// build.sc

import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest::0.7.1`
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`

import mill._
import mill.scalalib._
import mill.scalalib.publish._

import de.tobiasroeser.mill.integrationtest._
import de.tobiasroeser.mill.vcs.version._
import mill.define.{Cross, Target, Task}

trait Setup {
  val millPlatform: String
  val millVersion: String
  def scalaVersion = "2.13.13"
  def testMillVersions: Seq[String]
  def osLibVersion: String
}

object Setup {
  object R011 extends Setup {
    override val millPlatform = "0.11"
    override val millVersion = "0.11.0" // scala-steward:off
    override val testMillVersions = Seq("0.11.6", millVersion)
    override val osLibVersion = "0.9.1" // scala-steward:off
  }
  object R010 extends Setup {
    override val millPlatform = "0.10"
    override val millVersion = "0.10.0" // scala-steward:off
    // we skip 0.10.4 tests, as these don't run under windows properly
    override val testMillVersions = Seq("0.10.13", millVersion)
    override val osLibVersion = "0.8.0" // scala-steward:off
  }
  object R09 extends Setup {
    override val millPlatform = "0.9"
    override val millVersion = "0.9.3" // scala-steward:off
    override val testMillVersions = Seq("0.9.12", millVersion)
    override val osLibVersion = "0.7.1" // scala-steward:off
  }
  object R07 extends Setup {
    override val millPlatform = "0.7"
    override val millVersion = "0.7.0" // scala-steward:off
    override val testMillVersions = Seq("0.8.0", "0.7.3", millVersion)
    override val osLibVersion = "0.7.0" // scala-steward:off
  }
  object R06 extends Setup {
    override val millPlatform = "0.6"
    override val millVersion = "0.6.0" // scala-steward:off
    override val scalaVersion = "2.12.19"
    override val testMillVersions = Seq("0.6.3", "0.6.2", "0.6.1", "0.6.0")
    override val osLibVersion = "0.6.3" // scala-steward:off
  }
}

val setups = Seq(Setup.R011, Setup.R010, Setup.R09, Setup.R07, Setup.R06)

object jbake extends Cross[JbakeCross](setups.map(_.millPlatform))
trait JbakeCross extends ScalaModule with PublishModule with Cross.Module[String] {
  def millPlatform = crossValue

  def setup = setups.find(_.millPlatform == millPlatform).get
  def millVersion = setup.millVersion
  def scalaVersion = setup.scalaVersion

  override def artifactSuffix: T[String] = T(s"_mill${millPlatform}_${artifactScalaVersion()}")

  def publishVersion = VcsVersion.vcsState().format()

  override def compileIvyDeps = Agg(
    ivy"com.lihaoyi::mill-main:${millVersion}",
    ivy"com.lihaoyi::mill-scalalib:${millVersion}",
    ivy"com.lihaoyi::os-lib:${setup.osLibVersion}"
  )
  override def artifactName = T("de.tobiasroeser.mill.jbake")

  def pomSettings = T {
    PomSettings(
      description = "Mill module to generate sites with JBake",
      organization = "de.tototec",
      url = "https://github.com/lefou/mill-jbake",
      licenses = Seq(License.`Apache-2.0`),
      versionControl = VersionControl.github("lefou", "mill-jbake"),
      developers = Seq(Developer("lefou", "Tobias Roeser", "https.//github.com/lefou"))
    )
  }

  override def resources = T.sources {
    super.resources() ++ Seq(
      PathRef(millSourcePath / os.up / "LICENSE"),
      PathRef(millSourcePath / os.up / "README.adoc")
    )
  }
}

object itest extends Cross[ItestCross](setups.flatMap(_.testMillVersions))
trait ItestCross extends MillIntegrationTestModule with Cross.Module[String] {
  def itestVersion = crossValue

  def setup = setups.find(_.testMillVersions.exists(_ == itestVersion)).get

  def millTestVersion = T(itestVersion)
  def pluginsUnderTest = Seq(jbake(setup.millPlatform))

  override def testInvocations: Target[Seq[(PathRef, Seq[TestInvocation.Targets])]] = T {
    Seq(
      PathRef(millSourcePath / "src" / "01-simple-site") -> Seq(
        TestInvocation.Targets(Seq("verifyInit")),
        TestInvocation.Targets(Seq("verifyBake"))
      )
    )
  }
}
