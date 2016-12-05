package xke.akkastream.step1

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.testkit.TestSubscriber.Probe
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalatest.{Matchers, WordSpec}
import xke.akkastream.step1.Step_1.{CategorizedEvent, EndEvent, NumberEvent}

import scala.concurrent.Await
import scala.concurrent.duration._

class Step_1_Spec extends WordSpec with Matchers {

  implicit val system = ActorSystem("test-step-1")
  implicit val materializer = ActorMaterializer()

  "Step_1_1_Source" should {
    "have a Source that generates NumberEvents" in {

      val source = (new Step_1_1_Source).source
      val stream = source.runWith(TestSink.probe)

      val event1 = stream.request(1).expectNext()
      val event2 = stream.request(1).expectNext()

      event1.value should not equal(event2.value)
    }
  }

  "Step_1_2_Map" should {
    "map StartEvent to EndEvent with a doubled value" in {

      val stream = new Step_1_2_Map {
        override lazy val sink: Sink[EndEvent, _] = TestSink.probe
      }.stream.asInstanceOf[Probe[EndEvent]]

      val events = stream.request(3).expectNextN(3)

      events should contain theSameElementsInOrderAs(Seq(EndEvent(2), EndEvent(4), EndEvent(6)))
    }
  }

  "Step_1_3_Filter" should {
    "filter CategorizedEvents with a category of 3" in {

      val sourceEvents = List(CategorizedEvent(1, "a"), CategorizedEvent(3, "b"), CategorizedEvent(4, "c"),
        CategorizedEvent(3, "d"), CategorizedEvent(2, "e"))

      val stream = new Step_1_3_Filter {
        override lazy val source: Source[CategorizedEvent, _] = Source(sourceEvents)
        override lazy val sink: Sink[CategorizedEvent, _] = TestSink.probe
      }.stream.asInstanceOf[Probe[CategorizedEvent]]

      val events = stream.request(5).expectNextN(2)

      events should contain theSameElementsInOrderAs(Seq(CategorizedEvent(3, "b"), CategorizedEvent(3, "d")))
    }
  }

  "Step_1_4_MaterializedValue" should {
    "have a Source that generates exactly 10 NumberEvents" in {

      val source = (new Step_1_4_MaterializedValue).source
      val stream = source.runWith(TestSink.probe)

      stream.request(10).expectNextN(10)
      stream.expectComplete()
    }

    "have a Sink that sums the NumberEvent's values on completion" in {
      val sink = (new Step_1_4_MaterializedValue).sink
      val (stream, result) = TestSource.probe[NumberEvent]
        .toMat(sink)(Keep.both)
        .run()

      Range(0, 10).foreach(i => stream.unsafeSendNext(NumberEvent(i)))
      stream.sendComplete()

      Await.result(result, 2 seconds) should be (Range(0, 10).sum)
    }
  }
}
