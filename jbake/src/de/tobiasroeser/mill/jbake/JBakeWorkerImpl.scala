package de.tobiasroeser.mill.jbake

import os.{Path, Shellable}

class JBakeWorkerImpl(classpath: Seq[Path]) extends JBakeWorker {

  /**
   * Run JBake main with the given args.
   * @param cwd The working directory of the process.
   * @param args The args given to the main program.
   */
  def runJbakeMain(cwd: Path, args: Shellable*): Unit = {
    val proc = os.proc(
      "java",
      "-cp", classpath.mkString(":"),
      "org.jbake.launcher.Main",
      args
    ).call(
        cwd = cwd,
        stdout = os.Inherit,
        stderr = os.Inherit
      )
  }

}
