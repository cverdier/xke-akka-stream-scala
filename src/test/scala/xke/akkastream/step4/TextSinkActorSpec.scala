package xke.akkastream.step4

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import akka.stream.testkit.scaladsl.TestSource
import akka.testkit.EventFilter
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}
import xke.akkastream.step4.Step_4.{NumberEvent, TextEvent}

class TextSinkActorSpec extends WordSpec with Matchers {

  implicit val system = ActorSystem("step-4-TextSinkActor", ConfigFactory.parseString(
    """
      |akka.loggers = ["akka.testkit.TestEventListener"]
      |akka.loglevel = DEBUG
      |""".stripMargin
  ))
  implicit val materializer = ActorMaterializer()

  "A TextSinkActor" should {
    "call eventSuccess when it receives a message" in {

      val sink: Sink[TextEvent, _] = Sink.actorSubscriber(TextSinkActor.props(2, 4))
      val stream = TestSource.probe[TextEvent]
        .toMat(sink)(Keep.left)
        .run()

      EventFilter.info(start = "Received event: TextEvent(", occurrences = 2) intercept {
        stream.unsafeSendNext(TextEvent("a"))
        stream.unsafeSendNext(TextEvent("b"))
      }
    }

    "send back request when highWaterMark is reached" in {
      val sink: Sink[TextEvent, _] = Sink.actorSubscriber(TextSinkActor.props(2, 4))
      val stream = TestSource.probe[TextEvent]
        .toMat(sink)(Keep.left)
        .run()

      stream.expectRequest() should be (4)

      stream.unsafeSendNext(TextEvent("a"))
      stream.unsafeSendNext(TextEvent("b"))

      stream.expectNoMsg()

      stream.unsafeSendNext(TextEvent("c"))
      stream.unsafeSendNext(TextEvent("d"))

      stream.expectRequest().toInt should be > 2
    }

    "call onComplete on completion" in {

      val sink: Sink[TextEvent, _] = Sink.actorSubscriber(TextSinkActor.props(2, 4))
      val stream = TestSource.probe[TextEvent]
        .toMat(sink)(Keep.left)
        .run()

      EventFilter.info(start = "Stream has completed", occurrences = 1) intercept {
        stream.sendComplete()
      }
    }

    "call onFailure when a fatal error is received" in {

      val sink: Sink[TextEvent, _] = Sink.actorSubscriber(TextSinkActor.props(2, 4))
      val stream = TestSource.probe[TextEvent]
        .toMat(sink)(Keep.left)
        .run()

      EventFilter.error(start = "Failure in the Stream : ", occurrences = 1) intercept {
        stream.sendError(SomeError("boom"))
      }
    }
  }
}

case class SomeError(message: String) extends Exception(message)