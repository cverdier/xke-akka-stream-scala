package xke.akkastream.step2

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import xke.akkastream.step2.Step_2.{NumberEvent, TextEvent}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

object Step_2_Run extends App {
  new Step_2_2_MapAsync
}

object Step_2 {

  case class NumberEvent(value: Int)
  case class TextEvent(text: String)
}

class Step_2 {

  implicit val system = ActorSystem("step-2")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

  lazy val source: Source[NumberEvent, _] = Source.fromIterator(
    () => Iterator.continually(NumberEvent(Random.nextInt(1000)))
  )

  lazy val sink: Sink[TextEvent, _] = Sink.foreach(event => {
    println(event)
  })
}

class Step_2_0_SlowMap extends Step_2 {

  def slowConvert(numberEvent: NumberEvent): TextEvent = {
    Thread.sleep((2 seconds).toMillis)
    TextEvent(s"number ${numberEvent.value}")
  }

  source
    .map(slowConvert)
    .runWith(sink)
}

class Step_2_1_MapAsync extends Step_2 {

  def slowConvert(numberEvent: NumberEvent): TextEvent = {
    Thread.sleep((2 seconds).toMillis)
    TextEvent(s"number ${numberEvent.value}")
  }

  // TODO: Wrap slowConvert in a Future
  def convertAsync(numberEvent: NumberEvent): Future[TextEvent] = ???

  val parallelism = 4

  // TODO: Use parallesim with the mapAsync function
  lazy val stream = source
    .map(slowConvert)
    .runWith(sink)
  // TODO: Not working ? Try to increase the parallelism
}

class Step_2_2_MapAsync extends Step_2 {

  // Number divisible by 5 will take longer to process
  final def slowConvert(numberEvent: NumberEvent): TextEvent = {
    if (numberEvent.value % 5 == 0) Thread.sleep((5 seconds).toMillis)
    else Thread.sleep((2 seconds).toMillis)
    TextEvent(s"number ${numberEvent.value}")
  }

  // TODO: Reuse the implementation
  def convertAsync(numberEvent: NumberEvent): Future[TextEvent] = ???

  val parallelism = 8

  lazy val stream = source
    .map(slowConvert)
    .runWith(sink)
  // TODO: One operation blocks the flow... is there another mapAsync in the API ?
}

class Step_2_3_Grouped extends Step_2 {

  def slowBatchConvert(events: Seq[NumberEvent]): Seq[TextEvent] = {
    Thread.sleep((5 seconds).toMillis)
    events.map(event => TextEvent(s"number ${event.value}"))
  }

  // TODO: Wrap slowBatchConvert in a Future
  def batchConvertAsync(events: Seq[NumberEvent]): Future[Seq[TextEvent]] = ???

  // We will use a sink that accepts batched events
  lazy val batchSink: Sink[Seq[TextEvent], _] = Sink.foreach(events => {
    events.foreach(println)
  })

  val batchSize = 5
  val parallelism = 2

  // TODO: Implement a stream which uses the bacthConvertAsync function
  // TODO: find the correct functions in the Akka Stream API
  lazy val stream = //source.
    ???
//    .runWith(batchSink)

  // TODO: Hard ? Maybe there is an easier function in the Akka Stream API
}

class Step_2_4_Grouped_Ungrouped extends Step_2 {

  def slowBatchConvert(events: Seq[NumberEvent]): List[TextEvent] = {
    Thread.sleep((5 seconds).toMillis)
    events.map(event => TextEvent(s"number ${event.value}")).toList
  }

  // TODO: Reuse the previous implementation
  def batchConvertAsync(events: Seq[NumberEvent]): Future[List[TextEvent]] = //???
    Future(slowBatchConvert(events))

  val batchSize = 5
  val parallelism = 2

  // TODO: We want to use the previous sink (for TextEvent, not Seq[TextEvent])
  // TODO: Is there an 'unbatch' function in Akka Stream API ?
  lazy val stream = // source.
    // start like Step_2_3
    // unbatch here :
    ???
    //.runWith(sink)
}


