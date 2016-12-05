package xke.akkastream.step5

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestProbe
import org.scalatest.WordSpec
import xke.akkastream.step5.Step_5.NumberEvent

class Step_5_Spec extends WordSpec {

  implicit val system = ActorSystem("test-step-5")

  "Step5_1_Merge_Broadcast" should {
    "merge map and broadcast" in {

      val firstSinkResult = TestProbe()
      val secondSinkResult = TestProbe()

      val step = new Step_5_1_Merge_Broadcast {
        override lazy val smallNumberSource: Source[NumberEvent, _] = Source(List(NumberEvent(2), NumberEvent(4), NumberEvent(6)))
        override lazy val mediumNumberSource: Source[NumberEvent, _] = Source(List(NumberEvent(10), NumberEvent(30), NumberEvent(50)))
        override lazy val largeNumberSource: Source[NumberEvent, _] = Source(List(NumberEvent(300), NumberEvent(900)))
        override lazy val firstSink: Sink[NumberEvent, _] = Sink.actorRef(firstSinkResult.ref, Done)
        override lazy val secondSink: Sink[NumberEvent, _] = Sink.actorRef(secondSinkResult.ref, Done)
      }

      firstSinkResult.expectMsgAllOf(NumberEvent(4), NumberEvent(8), NumberEvent(12), NumberEvent(20), NumberEvent(60), NumberEvent(100), NumberEvent(600), NumberEvent(1800))
      secondSinkResult.expectMsgAllOf(NumberEvent(4), NumberEvent(8), NumberEvent(12), NumberEvent(20), NumberEvent(60), NumberEvent(100), NumberEvent(600), NumberEvent(1800))
      firstSinkResult.expectMsg(Done)
      secondSinkResult.expectMsg(Done)
    }
  }

}
