import com.gensler.scalavro.types.AvroType

/**
  * Created by amanoberoi on 14/4/16.
  */
object Blah extends App {

  case class Person(name: String = null, age: Int)

  val personAvroType = AvroType[Person]
  val json = personAvroType.schema

  println(json)
}
