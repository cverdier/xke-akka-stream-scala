package xke.akkastream.step4

import akka.actor.{ActorLogging, Props}
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnError, OnNext}
import akka.stream.actor.{ActorSubscriber, WatermarkRequestStrategy}
import xke.akkastream.step4.Step_4.TextEvent

object TextSinkActor {

  def props(lowWaterMark: Int, highWaterMark: Int): Props = Props(new TextSinkActor(lowWaterMark, highWaterMark))
}

class TextSinkActor(lowWaterMark: Int, highWaterMark: Int) extends ActorSubscriber with ActorLogging {

  // The request strategy decides when to send request again
  val requestStrategy = new WatermarkRequestStrategy(highWaterMark, lowWaterMark)

  // TODO : Implement the Sink logic
  def receive = {
    // TODO : Element : call eventSuccess(...) with the element received from the Stream
    // NB : Request will be sent automatically at the and of this method by the RequestStrategy
    case OnNext(event: TextEvent) =>
      eventSuccess(event)

    // TODO : Failure (fatal error) received from the Stream : call onFailure(...) and stop the actor
    case OnError(error: Exception) =>
      onFailure(error)
      context.stop(self)

    // TODO : Completion received from the Stream : call onComplete() and stop the actor
    case OnComplete =>
      onComplete()
      context.stop(self)
  }

  // Provided method to be called for each received Order
  def eventSuccess(event: TextEvent) = {
    Thread.sleep(50)
    log.info(s"Received event: $event")
  }

  def onFailure(error: Exception): Unit = {
    log.error(s"Failure in the Stream : $error")
  }

  def onComplete(): Unit = {
    log.info("Stream has completed")
  }
}
