package xke.akkastream.step4

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.testkit.scaladsl.TestSource
import akka.testkit.TestProbe
import org.scalatest.{Matchers, WordSpec}
import xke.akkastream.step4.Step_4.{NumberEvent, TextEvent}

class NumberToTextSpec extends WordSpec with Matchers {

  implicit val system = ActorSystem("step-4-NumberToText")
  implicit val materializer = ActorMaterializer()

  "A NumberToText" should {
    "map NumberEvent to TextEvent and send downstream on push" in {
      val testSink = TestProbe()
      val sink: Sink[TextEvent, _] = Sink.actorRef(testSink.ref, Done)
      val stream = TestSource.probe[NumberEvent]
        .via(new NumberToText())
        .toMat(sink)(Keep.left)
        .run()

      stream.unsafeSendNext(NumberEvent(0))
      testSink.expectMsg(TextEvent("value-0"))

      stream.unsafeSendNext(NumberEvent(1))
      testSink.expectMsg(TextEvent("value-1"))
    }

    "forward request upstream" in {
      val sink: Sink[TextEvent, _] = Sink.ignore
      val stream = TestSource.probe[NumberEvent]
        .via(new NumberToText())
        .toMat(sink)(Keep.left)
        .run()

      stream.expectRequest() should be (16) // Default buffers size
    }
  }
}
