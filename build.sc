// build.sc
import mill._, mill.scalalib._, mill.scalalib.publish._

object jbake extends ScalaModule with PublishModule {

  val millVersion = "0.3.6"

  def scalaVersion = "2.12.8"

  def publishVersion = "0.1.0-SNAPSHOT"

  def compileIvyDeps = Agg(
    ivy"com.lihaoyi::mill-main:${millVersion}",
    ivy"com.lihaoyi::mill-scalalib:${millVersion}",
    ivy"com.lihaoyi::os-lib:0.2.6"
  )

  def artifactName = T{ "de.tobiasroeser.mill.jbake" }

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

}
