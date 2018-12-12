package wiki

/**
  * Created by Bondarenko on 5/10/18.
  */

trait WikiExtractor{

  val patterns = "\\[\\[File:.*\\]\\]".r :: Nil


  def extract(text: String): String = {
    ???
  }

  


}