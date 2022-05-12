import mill._
import mill.define._
// import $ivy.`de.tototec::de.tobiasroeser.mill.jbake:0.2.0`
import $exec.plugins
import de.tobiasroeser.mill.jbake._
import $ivy.`org.scalatest::scalatest:3.1.4`
import org.scalatest.Assertions

object site extends JBakeModule {

  override def jbakeVersion = "2.6.4"

  override def jbakeProcessMode = JBakeModule.SubProcess

}

def verify(): Command[Unit] = T.command {

  val A = new Assertions{}
  import A._

  site.jbakeInit()()
  val files = os.walk(site.millSourcePath / "src")

  assert(files.contains(site.millSourcePath / "src" / "templates"))

  ()
}
