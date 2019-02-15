package de.tobiasroeser.mill.jbake

import mill._
import mill.define.{Discover, ExternalModule, Sources, Target, TaskModule, Worker}

trait JBakeModule extends Module with TaskModule {

  override def defaultCommandName(): String = "jbake"

  def jbakeVersion: T[String]

  def jbakeDistributionZip: T[PathRef] = T {
    os.proc(
      "wget",
      s"https://dl.bintray.com/jbake/binary/jbake-${jbakeVersion()}-bin.zip"
    ).call(
      cwd = T.ctx().dest,
      stdout = os.Inherit,
      stderr = os.Inherit
    )
    PathRef(T.ctx().dest / s"jbake-${jbakeVersion()}-bin.zip")
  }

  def jbakeDistributionDir: T[PathRef] = T {
    os.proc(
      "unzip",
      jbakeDistributionZip().path
    ).call(cwd = T.ctx().dest)
    PathRef(T.ctx().dest / s"jbake-${jbakeVersion()}")
  }

  def jbakeClasspath: T[Seq[PathRef]] = T {
    (Seq(jbakeDistributionDir().path / "jbake-core.jar") ++
      os.list(jbakeDistributionDir().path / 'lib)
      ).map(PathRef(_))
  }

  def sources: Sources = T.sources {
    millSourcePath / 'src
  }

  def jbake: T[PathRef] = T {
    val targetDir = T.ctx().dest

    // TODO: Use a worker

    val proc = os.proc(
      "java",
      "-cp", jbakeClasspath().map(_.path).mkString(":"),
      "org.jbake.launcher.Main",
      sources().head.path,
      targetDir
    ).call(
      cwd = targetDir,
      stdout = os.Inherit,
      stderr = os.Inherit
    )

    PathRef(targetDir)
  }

  def jbakeServe() = T.command {
    val jbakeDir = jbake().path

    val proc = os.proc(
      "java",
      "-cp", jbakeClasspath().map(_.path).mkString(":"),
      "org.jbake.launcher.Main",
      "-s", jbakeDir
    ).call(
      cwd = T.ctx().dest,
      stdout = os.Inherit,
      stderr = os.Inherit,
      stdin = os.Inherit
    )

  }

  /**
    * Initializes a JBake project.
    */
  def jbakeInit() = T.command {

  }

}
