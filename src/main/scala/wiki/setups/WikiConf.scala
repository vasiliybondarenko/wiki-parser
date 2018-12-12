package wiki.setups

import java.io.FileInputStream
import java.util.Properties


/**
  * Created by Bondarenko on 5/27/18.
  */
object WikiConf  {

  implicit object Conf extends Config {
    override def wikiPath: String = PropHelpers.get("source.path").getOrElse(???)

    override def targetFilePath: String = PropHelpers.get("target.path").getOrElse(???)
  }

}

object PropHelpers {
  def propsByPath(path: String) = {
    val props = new Properties()
    props.load(new FileInputStream(path))
    props
  }

  def get(key: String): Option[String] = Option(props.getProperty(key))

  private lazy val props = propsByPath("environment.properties")

}

trait Config {
  def wikiPath: String

  def targetFilePath: String


}