package de.tobiasroeser.mill.jbake

import scala.util.{Success, Try}

import mill._
import mill.api.{Ctx, IO}
import mill.define.{Sources, TaskModule}

trait JBakeModule extends Module with TaskModule {

  override def defaultCommandName(): String = "jbake"

  /**
   * The JBake version to be used.
   *
   * It will be used to automatically download the JBake distribution.
   */
  def jbakeVersion: T[String]

  /**
   * The JBake Binary Distribution ZIP archive.
   *
   * Defaults to downloading the distribution file from Bintray.
   */
  def jbakeDistributionZip: T[PathRef] = T {
    mill.modules.Util.download(
      s"https://dl.bintray.com/jbake/binary/jbake-${jbakeVersion()}-bin.zip",
      s"jbake-${jbakeVersion()}-bin.zip"
    )
  }

  /**
   * The unpacked JBake Distribution.
   *
   * Defaults to the unpacked content of the [[jbakeDistributionZip]].
   */
  def jbakeDistributionDir: T[PathRef] = T {
    parseVersion(jbakeVersion()) match {
      case Success(Array(2, 0 | 1 | 2 | 3 | 4 | 5, _)) =>
        PathRef(IO.unpackZip(jbakeDistributionZip().path).path / s"jbake-${jbakeVersion()}")
      case _ =>
        PathRef(IO.unpackZip(jbakeDistributionZip().path).path / s"jbake-${jbakeVersion()}-bin")
    }
  }

  /** Extract the major, minor and micro version parts of the given version string. */
  private def parseVersion(version: String): Try[Array[Int]] = Try {
    version
      .split("[-]", 2)(0)
      .split("[.]", 4)
      .take(3)
      .map(_.toInt)
  }

  /**
   * The classpath used to run the JBake site generator.
   */
  def jbakeClasspath: T[Seq[PathRef]] = T {
    parseVersion(jbakeVersion()) match {
      case Success(Array(2, 0 | 1 | 2 | 3 | 4 | 5, _)) =>
        (Seq(jbakeDistributionDir().path / "jbake-core.jar") ++
          os.list(jbakeDistributionDir().path / 'lib)).map(PathRef(_))
      case _ =>
        os.list(jbakeDistributionDir().path / 'lib).map(PathRef(_))
    }
  }

  def sources: Sources = T.sources {
    millSourcePath / 'src
  }

  /**
   * Bake the site with JBake.
   */
  def jbake: T[PathRef] = T {
    val targetDir = T.ctx().dest
    val log = T.ctx().log

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

  /**
   * Starts a local Webserver to serve the content created with [[jbake]].
   *
   * FIXME: This doesn't work for JBake versions since 2.6.2.
   */
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
        stderr = os.Inherit
      )

  }

  /**
   * Initialized the sources for a new project.
   */
  def jbakeInit() = T.command {
    if (!os.walk(sources().head.path).isEmpty) {
      throw new RuntimeException(s"Source directory ${sources().head.path} is not empty. Aborting initializing a fresh JBake project")
    } else {
      //      val baseZip = ???
      //      IO.unpackZip(baseZip, )
    }
  }

}
