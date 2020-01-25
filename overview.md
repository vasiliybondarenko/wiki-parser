1. Either это альтернатива Option. Главное назначение - хранить не только резульат вычисления,
но и ошибку в случае если вычисление зафейлилось.

Определение из Scala library:
```scala
sealed abstract class Either[+A, +B]
final case class Left[+A, +B](@deprecatedName('a, "2.12.0") value: A) extends Either[A, B]
final case class Right[+A, +B](@deprecatedName('b, "2.12.0") value: B) extends Either[A, B]
```

Как видно, Either абстрактный класс, у него есть ровно 2 наследника,
ввиду этого экземпляр Either это экземпляр класса  Left (в случае ошибки),
или класса Right (в случае если нет ошибок)


ПРИМЕР:
```scala
import scala.util.{ Failure, Success, Try }

def parse(x: String): Either[String, Int] = Try(x.toInt) match {
  case Failure(exception) => Left(s"Unable to parse [$x] to int")
  case Success(value) => Right(value)
}


parse("12") //Right(12)

parse("aaa") //Left(Unable to parse [aaa] to int)
```

2.
lazy val ... что означает и в чем ленивость ?
new BeanFactoryDefault {
	override lazy val httpClient: HttpClient = self.httpClient
}

 В данном примере создается экземпляр класса BeanFactoryDefault. Как и в java при вызове конструктора
 по умолчанию происходит инициализация его полей, то есть поле httpClient должны на момент создания данного
 экземпляра инициализроваться, но посколько оно объявленно как lazy то онициализация не происходит.
 Оно будет инициализированно при первом его вызове.

Scala is eager language.

ПРИМЕР:
```scala
class A {

  val f1 = {
    println(s"f1")
    12
  }

  lazy val f2 = {
    println("f2")
    128
  }

}

new A()
//f1
//res0: A = A@4a7c973f

new A().f2
//f1
//f2
//res1: Int = 128
```


By name parameters:

```scala
var isDebugEnabled = true

def debug(message: =>String) = {
  if(isDebugEnabled) println(message)
}
```
//если isDebugEnabled параметр НЕ message вычисляется при вызове функции debug

3. Можно ли использовать Optional из Java или у Scala есть свой схожий тип?

В Scala library есть свой тип - Option
Также как Either явлеятся абстрактным и имеет 2 наследника:
 	Some
	Empty

Из документации:
Represents optional values. Instances of Option are either an instance of scala.Some or the object None.
https://www.scala-lang.org/api/current/scala/Option.html


See also:
Pattern matching in scala
https://docs.scala-lang.org/tour/pattern-matching.html


Распространенный патерн в scala:
```scala
sealed abstact class Base{}
final case class A extends Base {}
final case class B extends Base {}
```

--------------------------------------------------
4. Как лучше возвращать результат выполнения метода, где размещать и как распознавать.
В Java используется зарезервированное слово return, а в Scala неясно.

В Java  возврат значения метода происходит через ключевое слово return,
 которое к тому же может быть в любом месте (не обязательно в конце)
В Scala тоже есть return, НО его использование не приветсвуется. Возвращаемое значение метода -
 это результат последнего выражения.
 В примере ниже оба метода возвращают сумму:

```scala
//правильно но не приветствуется
  def add(a: Int, b: Int): Int = {
   	return a + b
  }

//правильно
  def add(a: Int, b: Int) = {
      println(s"sum: ${a + b}")
      a + b
  }
 ```

В последнем примере тип возвращаемого значения не указан, его компилятор сам выводит


--------------------------------------------------
5. Встречаю следующую конструкцию:
val fileio:FileLoader = new NoopLoader with FileSystemLoader with ClasspathLoader{}
Что означает with в этой конструкции?

with это аналог implements в Java.
trait  - это аналог интерфейсов, может содержать абстрактные и неабстрактные члены,
но не содержит конструктор и если типизированный не поддерживает context bound.
При наследовании используется линерализация:
https://www.trivento.io/trait-linearization/

В данном примере создается экземпляр класса NoopLoader,
который наследует 2 трейта FileSystemLoader и ClasspathLoader.

То есть это то же самое что определить класс NoopLoader с данными трейтами:
class NoopLoader extends FileSystemLoader with ClasspathLoader {  ... }

 а затем создать экземпляр:
 ```scala  
 val fileio:FileLoader = new NoopLoader()
 ```
