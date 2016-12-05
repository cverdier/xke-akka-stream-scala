package xke.akkastream.step5

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape}
import xke.akkastream.step5.Step_5.NumberEvent

import scala.util.Random

object Step_5_Run extends App {
  new Step_5_1_Merge_Broadcast
}

object Step_5 {

  case class NumberEvent(value: Int)
}

class Step_5 {

  implicit val system = ActorSystem("step-3")
  implicit val materializer = ActorMaterializer()
}

class Step_5_0_Broadcast extends Step_5 {

  lazy val source: Source[NumberEvent, _] = Source.fromIterator(
    () => Iterator.continually(NumberEvent(Random.nextInt(1000)))
  )

  lazy val firstSink: Sink[NumberEvent, _] = Sink.foreach(event => {
    Thread.sleep(50)
    println(s"First: $event")
  })

  lazy val secondSink: Sink[NumberEvent, _] = Sink.foreach(event => {
    Thread.sleep(200)
    println(s"Second: $event")
  })

  lazy val thirdSink: Sink[NumberEvent, _] = Sink.foreach(event => {
    Thread.sleep(100)
    println(s"Third: $event")
  })

  lazy val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val broadcast = b.add(Broadcast[NumberEvent](3))

    source ~> broadcast ~> firstSink
              broadcast ~> secondSink
              broadcast ~> thirdSink

    ClosedShape
  })
  graph.run()
}

class Step_5_1_Merge_Broadcast extends Step_5 {

  lazy val smallNumberSource: Source[NumberEvent, _] = Source.fromIterator(
    () => Iterator.continually(NumberEvent(Random.nextInt(10)))
  )

  lazy val mediumNumberSource: Source[NumberEvent, _] = Source.fromIterator(
    () => Iterator.continually(NumberEvent(Random.nextInt(10) * 10))
  )

  lazy val largeNumberSource: Source[NumberEvent, _] = Source.fromIterator(
    () => Iterator.continually(NumberEvent(Random.nextInt(10) * 100))
  )

  lazy val firstSink: Sink[NumberEvent, _] = Sink.foreach(event => {
    Thread.sleep(50)
    println(s"First: $event")
  })

  lazy val secondSink: Sink[NumberEvent, _] = Sink.foreach(event => {
    Thread.sleep(100)
    println(s"Second: $event")
  })

  lazy val double = Flow[NumberEvent].map(event => NumberEvent(event.value * 2))

  lazy val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    // TODO: merge all the Sources
    // TODO: double the NumberEvent's values via 'double'
    // TODO: broadcast to the two Sinks

    val merge = b.add(Merge[NumberEvent](3))
    val broadcast = b.add(Broadcast[NumberEvent](2))

    smallNumberSource  ~> merge ~> double ~> broadcast ~> firstSink
    mediumNumberSource ~> merge ;            broadcast ~> secondSink
    largeNumberSource  ~> merge

    ClosedShape
  })
  graph.run()
}