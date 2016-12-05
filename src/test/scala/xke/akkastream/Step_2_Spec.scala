package xke.akkastream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.testkit.TestSubscriber.Probe
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.{Matchers, WordSpec}
import xke.akkastream.Step_2.{NumberEvent, TextEvent}

import scala.concurrent.duration._

class Step_2_Spec extends WordSpec with Matchers {

  implicit val system = ActorSystem("test-step-2")
  implicit val materializer = ActorMaterializer()

  "Step_2_1_MapAsync" should {
    "use parallelism" in {

      val stream = new Step_2_1_MapAsync {
        override lazy val sink: Sink[TextEvent, _] = TestSink.probe
      }.stream.asInstanceOf[Probe[TextEvent]]

      val deadline = (3 seconds).fromNow
      stream.request(8).expectNextN(8)

      assert(! deadline.isOverdue())
    }
  }

  "Step_2_2_MapAsync" should {
    "use parallelism without blocking on some values" in {

      val stream = new Step_2_2_MapAsync {
        override lazy val source: Source[NumberEvent, _] = Source.fromIterator(() => Range(1, 11).map(NumberEvent).iterator)
        override lazy val sink: Sink[TextEvent, _] = TestSink.probe
      }.stream.asInstanceOf[Probe[TextEvent]]

      val deadline = (3 seconds).fromNow
      stream.request(10).expectNextN(7)

      assert(! deadline.isOverdue())
    }
  }

  "Step_2_3_Grouped" should {
    "group elements" in {

      val stream = new Step_2_3_Grouped {
        override lazy val batchSink: Sink[Seq[TextEvent], _] = TestSink.probe
      }.stream.asInstanceOf[Probe[Seq[TextEvent]]]

      stream.request(1).expectNext(7 seconds)
    }
  }

  "Step_2_4_Grouped_Ungrouped" should {
    "group, process and ungroup elements" in {

      val stream = new Step_2_4_Grouped_Ungrouped {
        override lazy val sink: Sink[TextEvent, _] = TestSink.probe
      }.stream.asInstanceOf[Probe[Seq[TextEvent]]]

      stream.request(10).expectNext(7 seconds)
      stream.expectNextN(4)
    }
  }
}
