package de.tobiasroeser.mill.jbake

import os.{Path, Shellable}

trait JBakeWorker {
  def runJbakeMain(cwd: Path, args: Shellable*): Unit
}
