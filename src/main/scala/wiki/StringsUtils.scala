package wiki

/**
  * Created by Bondarenko on 5/3/18.
  */
trait StringsUtils {
  def convertToCorrectFileName(s: String) = s.replaceAll("/", "_").limit(100)

  implicit class Trimmer(s: String) {
    def limit(size: Int) = if(s.length > size) s.substring(0, size) else s
  }

}
