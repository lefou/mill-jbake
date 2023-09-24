// build.sc

import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest::0.6.0`
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.1.4`

import mill._
import mill.scalalib._
import mill.scalalib.publish._

import de.tobiasroeser.mill.integrationtest._
import de.tobiasroeser.mill.vcs.version._
import mill.define.{Target, Task}

trait Setup {
  val millPlatform: String
  val millVersion: String
  def scalaVersion = "2.13.11"
  def testMillVersions: Seq[String]
}

object Setup {
  object R010 extends Setup {
    override val millPlatform = "0.10"
    override val millVersion = "0.10.0"
    // we skip 0.10.4 tests, as these don't run under windows properly
    override val testMillVersions = Seq("0.10.12", millVersion)
  }
  object R09 extends Setup {
    override val millPlatform = "0.9"
    override val millVersion = "0.9.3"
    override val testMillVersions = Seq("0.9.12", millVersion)
  }
  object R07 extends Setup {
    override val millPlatform = "0.7"
    override val millVersion = "0.7.0"
    override val testMillVersions = Seq("0.8.0", "0.7.3", millVersion)
  }
  object R06 extends Setup {
    override val millPlatform = "0.6"
    override val millVersion = "0.6.0"
    override val scalaVersion = "2.12.15"
    override val testMillVersions = Seq("0.6.3", "0.6.2", "0.6.1", "0.6.0")
  }
}

val setups = Seq(Setup.R010, Setup.R09, Setup.R07, Setup.R06)

trait JbakeConfig extends CrossScalaModule with PublishModule {
  def millPlatform: String

  def setup = setups.find(_.millPlatform == millPlatform).get
  def millVersion = setup.millVersion
  def crossScalaVersion = setup.scalaVersion

  override def artifactSuffix: T[String] = T(s"_mill${millPlatform}_${artifactScalaVersion()}")

  def publishVersion = VcsVersion.vcsState().format()

  override def compileIvyDeps = Agg(
    ivy"com.lihaoyi::mill-main:${millVersion}",
    ivy"com.lihaoyi::mill-scalalib:${millVersion}",
    ivy"com.lihaoyi::os-lib:0.6.3"
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

object jbake extends Cross[JbakeCross](setups.map(_.millPlatform): _*)
class JbakeCross(override val millPlatform: String) extends CrossScalaModule with JbakeConfig

object itest extends Cross[ItestCross](setups.flatMap(_.testMillVersions):_*)
class ItestCross(itestVersion: String) extends MillIntegrationTestModule {
  override def millSourcePath: os.Path = super.millSourcePath / os.up

  def setup = setups.find(_.testMillVersions.exists(_ == itestVersion)).get

  def millTestVersion = T(itestVersion)
  def pluginsUnderTest = Seq(jbake(setup.millPlatform))

  override def testInvocations: Target[Seq[(PathRef, Seq[TestInvocation.Targets])]] = T {
    Seq(
      PathRef(millSourcePath / "src" / "01-simple-site") -> Seq(
        TestInvocation.Targets(Seq("verify")),
        TestInvocation.Targets(Seq("site.jbake"))
      )
    )
  }
}
