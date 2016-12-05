package xke.akkastream.step1

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import xke.akkastream.step1.Step_1.{CategorizedEvent, EndEvent, NumberEvent, StartEvent}

import scala.concurrent.Future
import scala.util.{Failure, Random, Success}


object Step_1_Run extends App {
  // Run a step here
  new Step_1_0_Helloworld
}

object Step_1 {

  case class NumberEvent(value: Int)

  case class StartEvent(value: Int)
  case class EndEvent(value: Int)

  case class CategorizedEvent(category: Int, content: String)
}

class Step_1 {

  implicit val system = ActorSystem("step-1")
  implicit val materializer = ActorMaterializer()
}

class Step_1_0_Helloworld extends Step_1 {

  val source: Source[String, _] = Source.fromIterator(
    () => Iterator.continually().map(_ => s"Hello, world !")
  )
  val sink: Sink[String, _] = Sink.foreach(
    element => println(element)
  )

  source.runWith(sink)
}

class Step_1_1_Source extends Step_1 {

  // TODO: create a Source that will generate 'NumberEvent' with a random value
  // TODO: you can use 'scala.util.Random.nextInt()'
  val source: Source[NumberEvent, _] = Source.fromIterator(
    () => Iterator.continually().map(_ => NumberEvent(Random.nextInt()))
  )
  val sink: Sink[NumberEvent, _] = Sink.foreach(event => {
    Thread.sleep(50)
    println(event)
  })

  // TODO: run Step_1_1_Source, do you notice the back-pressure ?
  source.runWith(sink)
}

class Step_1_2_Map extends Step_1 {

  lazy val source: Source[StartEvent, _] = Source.fromIterator(
    () => Iterator.from(1).map(StartEvent)
  )

  lazy val sink: Sink[EndEvent, _] = Sink.foreach(event => {
    Thread.sleep(50)
    println(event)
  })

  // TODO: write a map function so EndEvent's value will be the double of StartEvent's value
  lazy val stream = source
    .map(start => EndEvent(start.value * 2))
    .runWith(sink)
}

class Step_1_3_Filter extends Step_1 {

  lazy val source: Source[CategorizedEvent, _] = Source.fromIterator(
      () => Iterator.continually().map(_ => CategorizedEvent(Random.nextInt(5), UUID.randomUUID().toString))
    )
  lazy val sink: Sink[CategorizedEvent, _] = Sink.foreach(event => {
    Thread.sleep(50)
    println(event)
  })

  // TODO: write a filter function so only CategorizedEvent with category equals 3 are logged by the Sink
  lazy val stream = source
    .filter(_.category == 3)
    .runWith(sink)
}

class Step_1_4_MaterializedValue extends Step_1 {
  import scala.concurrent.ExecutionContext.Implicits.global

  // TODO: create a Source that will generate exactly 10 'NumberEvent's with a random value
  lazy val source: Source[NumberEvent, _] =
    Source(Range(0, 10).map(_ => NumberEvent(Random.nextInt(100))))

  // TODO: create a Sink that will sum the 'NumberEvent's values on completion
  lazy val sink: Sink[NumberEvent, Future[Int]] =
    Sink.fold(0)((agg: Int, event: NumberEvent) => agg + event.value)

  lazy val result: Future[Int] = source.runWith(sink)
  result.onComplete {
    case Success(value) => println(s"Result: $value")
    case Failure(error) => println(s"Error: $error")
  }
}

