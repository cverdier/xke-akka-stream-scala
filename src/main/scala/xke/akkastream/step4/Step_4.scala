package xke.akkastream.step4

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import xke.akkastream.step4.Step_4.{NumberEvent, TextEvent}

object Step_4_Run extends App {
  new Step_4
}

object Step_4 {

  case class NumberEvent(value: Int)
  case class TextEvent(text: String)
}

class Step_4 {

  implicit val system = ActorSystem("step-4")
  implicit val materializer = ActorMaterializer()

  // TODO: First, implement the NumberSourceActor
  // TODO: Create a Source from the NumberSourceActor
  lazy val source: Source[NumberEvent, _] = Source.actorPublisher(NumberSourceActor.props)

  // TODO: First, implement the TextSinkActor
  // TODO: Create a Sink from the TextSinkActor
  lazy val sink: Sink[TextEvent, _] = Sink.actorSubscriber(TextSinkActor.props(2, 4))

  // TODO: First, implement the custom stage in NumberToText
  // TODO: Create the stage from NumberToText
  lazy val numberToText = Flow[NumberEvent].via(new NumberToText)

  // TODO: Finally, wire all the elements and run the stream
  val stream = source
    .via(numberToText)
    .runWith(sink)
}

