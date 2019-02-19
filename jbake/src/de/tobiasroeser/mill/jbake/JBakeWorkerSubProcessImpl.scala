package de.tobiasroeser.mill.jbake

import mill.api.Ctx.Log
import os.{Path, Shellable}

class JBakeWorkerSubProcessImpl(classpath: Seq[Path])(implicit ctx: Log) extends JBakeWorker {

  /**
   * Run JBake main with the given args.
   *
   * @param cwd  The working directory of the process.
   * @param args The args given to the main program.
   */
  def runJbakeMain(cwd: Path, args: Shellable*): Unit = {
    val proc = os.proc(
      "java",
      "-cp", classpath.mkString(":"),
      "org.jbake.launcher.Main",
      args
    )
    ctx.log.debug(s"Executing process: ${proc.command.flatMap(_.value)}")

    proc.call(
      cwd = cwd,
      stdout = os.Inherit,
      stderr = os.Inherit
    )
  }

}
