// build.sc
import mill._
import mill.scalalib._
import mill.scalalib.publish._

import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest:0.1.0`, de.tobiasroeser.mill.integrationtest._
//import $ivy.`de.tototec::de.tobiasroeser.mill.integrationtest:0.1.1-SNAPSHOT`, de.tobiasroeser.mill.integrationtest._

object jbake extends ScalaModule with PublishModule {

  val millVersion = "0.3.6"

  def scalaVersion = "2.12.8"

  def publishVersion = "0.2.1-SNAPSHOT"

  override def compileIvyDeps = Agg(
    ivy"com.lihaoyi::mill-main:${millVersion}",
    ivy"com.lihaoyi::mill-scalalib:${millVersion}",
    ivy"com.lihaoyi::os-lib:0.2.6"
  )

  override def artifactName = T {
    "de.tobiasroeser.mill.jbake"
  }

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

object it extends MillIntegrationTestModule {

  def millTestVersion = T { jbake.millVersion }

  def pluginsUnderTest = Seq(jbake)

}
