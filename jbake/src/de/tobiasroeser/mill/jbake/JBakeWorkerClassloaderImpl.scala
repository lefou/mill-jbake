package de.tobiasroeser.mill.jbake

import java.net.URL
import java.security.Permission

import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader

import mill.api.Ctx.Log
import os.{Path, Shellable}

class JBakeWorkerClassloaderImpl(classpath: Seq[Path])(implicit ctx: Log) extends JBakeWorker {

  var jbakeClassLoader: Option[ClassLoader] = None

  /**
   * Run JBake main with the given args.
   *
   * @param cwd  The working directory of the process.
   * @param args The args given to the main program.
   */
  def runJbakeMain(cwd: Path, args: Shellable*): Unit = {

    val cl = jbakeClassLoader match {
      case Some(cl) => cl
      case None =>
        ctx.log.debug(s"Creating Classloader with classpath: [${classpath}]")
        val cl = new URLClassLoader(classpath.map(_.toNIO.toUri().toURL()).toArray[URL], null)
        jbakeClassLoader = Some(cl)
        cl
    }

    val mainClass = cl.loadClass("org.jbake.launcher.Main")
    val mainMethod = mainClass.getMethod("main", Seq(classOf[Array[String]]): _*)

    // We need a security manager to intercept the System.exit calls
    // We need a TCCL to fix an issue in JBake 2.6.4 and before finding their bundles properties files

    val prevSecManager = System.getSecurityManager()
    val securityManager = new SecurityManager() {
      override def checkPermission(perm: Permission): Unit = {
        Option(prevSecManager).foreach(_.checkPermission(perm))
      }

      override def checkPermission(perm: Permission, context: Any): Unit = {
        Option(prevSecManager).foreach(_.checkPermission(perm, context))
      }

      override def checkExit(status: Int): Unit = {
        throw new SecurityException("JBake exit " + status)
      }
    }

    val prevCl = Thread.currentThread().getContextClassLoader()
    System.setSecurityManager(securityManager)
    try {
      Thread.currentThread().setContextClassLoader(cl)

      // Calling JBake
      mainMethod.invoke(null, args.flatMap(_.value).toArray)

    } catch {
      case e: SecurityException if e.getMessage() == "JBake exit 0" => // JBake exited successfully
    } finally {
      System.setSecurityManager(prevSecManager)
      Thread.currentThread().setContextClassLoader(prevCl)
    }

  }

}
