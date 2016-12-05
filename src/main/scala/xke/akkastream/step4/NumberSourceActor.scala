package xke.akkastream.step4

import akka.actor.{ActorLogging, Props}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import xke.akkastream.step4.Step_4.NumberEvent

object NumberSourceActor {

  def props: Props = Props(new NumberSourceActor)
}

class NumberSourceActor extends ActorPublisher[NumberEvent] with ActorLogging {
  var index: Int = 0

  // TODO Implement the Source logic
  //  - Send elements to the Stream by calling onNext(element) method
  //  - Elements should only be sent when there is Request, and exactly match the Request count

  override def receive: Receive = {
    // TODO: The Stream sent down a Request message : emit elements to the Stream by calling 'onNext(...)'
    // TODO: You must send as mush as requested
    case Request(count) =>
      log.info(s"Received Request ($count)")
      Range(0, count.toInt).foreach(_ => onNext(generateNextEvent()))

    // TODO: The Stream sent down a cancel message : stop the actor
    case Cancel =>
      log.info("Cancel Message Received")
      context.stop(self)
  }

  // Provided method to generate an Event
  def generateNextEvent(): NumberEvent = {
    // Next event with incremented value
    val event = NumberEvent(index)
    index += 1
    event
  }
}
