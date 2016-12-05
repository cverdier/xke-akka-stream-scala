package xke.akkastream.step4

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.testkit.TestSubscriber.Probe
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.{Matchers, WordSpec}
import xke.akkastream.step4.Step_4.NumberEvent

class NumberSourceActorSpec extends WordSpec with Matchers {

  implicit val system = ActorSystem("step-4-NumberSourceActor")
  implicit val materializer = ActorMaterializer()

  "A NumberSourceActor" should {
    "respond correctly to request" in {

      val source: Source[NumberEvent, _] = Source.actorPublisher(NumberSourceActor.props)
      val sink: Sink[NumberEvent, Probe[NumberEvent]] = TestSink.probe[NumberEvent]

      val stream = source.runWith(sink)

      val events = stream.request(4).expectNextN(4)
      events should contain theSameElementsInOrderAs(List(NumberEvent(0), NumberEvent(1), NumberEvent(2), NumberEvent(3)))

      stream.expectNoMsg()

      val moreEvents = stream.request(2).expectNextN(2)
      moreEvents should contain theSameElementsInOrderAs(List(NumberEvent(4), NumberEvent(5)))

      stream.expectNoMsg()
    }

    "stop on cancel" in {
      val source = Source.actorPublisher(NumberSourceActor.props)
      val sink = TestSink.probe

      val stream = source.runWith(sink)
      stream.cancel()

      stream.request(4)
      stream.expectNoMsg()
    }

  }
}
