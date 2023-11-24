import mill._
import mill.define._
import $file.plugins
import de.tobiasroeser.mill.jbake._
import $ivy.`org.scalatest::scalatest:3.1.4`
import org.scalatest.Assertions

object siteSp extends JBakeModule {
  override def jbakeVersion = "2.6.4"

  override def jbakeProcessMode = JBakeModule.SubProcess
}

object siteCl extends JBakeModule {
  override def jbakeVersion = "2.6.4"

  override def jbakeProcessMode = JBakeModule.ClassLoader
}

val A = new Assertions {}

import A._

val sites = Seq(siteSp, siteCl)

def verifyInit(site: JBakeModule) = T.task {

  site.jbakeInit()()

  val expected = Seq(
    os.sub / "jbake.properties",
    os.sub / "templates" / "archive.ftl",
    os.sub / "templates" / "feed.ftl",
    os.sub / "templates" / "footer.ftl",
    os.sub / "templates" / "header.ftl",
    os.sub / "templates" / "index.ftl",
    os.sub / "templates" / "menu.ftl",
    os.sub / "templates" / "page.ftl",
    os.sub / "templates" / "post.ftl",
    os.sub / "templates" / "sitemap.ftl",
    os.sub / "templates" / "tags.ftl",
    os.sub / "content" / "about.html",
  )

  val dir = site.millSourcePath / "src"
  val files = os.walk(dir)

  for {
    file <- expected
  } assert(files.contains(dir / file))
}

def verifyBake(site: JBakeModule) = T.task {
  site.jbake()

  val expected = Seq(
    os.sub / "index.html",
    os.sub / "favicon.ico",
    os.sub / "feed.xml",
    os.sub / "archive.html"

  )

  val dir = T.workspace / "out" / site.toString() / "jbake.dest"
  val files = os.walk(dir)

  for {
    file <- expected
  } assert(files.contains(dir / file))
}

def verifyInit(): Command[Unit] = T.command {
  T.traverse(sites)(s => verifyInit(s))()
  ()
}

def verifyBake(): Command[Unit] = T.command {
  // Classloader mode does not work anymore with newer Java
  T.traverse(Seq(siteSp))(s => verifyBake(s))()
  ()
}
