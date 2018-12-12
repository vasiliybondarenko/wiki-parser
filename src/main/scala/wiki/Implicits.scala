package wiki

/**
  * Created by Bondarenko on 5/9/18.
  */
object Implicits {
  implicit class P(value: Any) {
    def show = println(value)
  }
}
