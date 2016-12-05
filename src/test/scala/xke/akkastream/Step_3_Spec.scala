package xke.akkastream

import akka.Done
import akka.stream.scaladsl.Source
import org.scalatest.{Matchers, WordSpec}
import xke.akkastream.Step_3.LetterEvent

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Success, Try}

class Step_3_Spec extends WordSpec with Matchers {

  "Step_3_1_Handle_Error" should {
    "handle errors without breaking the stream" in {

      val stream = new Step_3_1_Handle_Error {
        override lazy val source: Source[LetterEvent, _] = Source(
          List(LetterEvent("a"), LetterEvent("1"), LetterEvent("a"), LetterEvent("2"), LetterEvent("a"))
        )
      }.stream

      Try(Await.result(stream, 2 seconds)) should be (Success(Done))
    }
  }

  "Step_3_2_Chain_Handle_Error" should {
    "handle errors in multiple operations without breaking the stream" in {

      val stream = new Step_3_2_Chain_Handle_Error {
        override lazy val source: Source[LetterEvent, _] = Source(
          List(LetterEvent("a"), LetterEvent("1"), LetterEvent("0"), LetterEvent("0"), LetterEvent("a"), LetterEvent("3"))
        )
      }.stream

      Try(Await.result(stream, 2 seconds)) should be (Success(Done))
    }
  }
}
