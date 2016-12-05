package xke.akkastream.step4

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import xke.akkastream.step4.Step_4.{NumberEvent, TextEvent}

class NumberToText() extends GraphStage[FlowShape[NumberEvent, TextEvent]] {

  val in = Inlet[NumberEvent]("NumberToText.in")
  val out = Outlet[TextEvent]("NumberToText.out")

  def numberToText(event: NumberEvent): TextEvent = {
    Thread.sleep(100)
    TextEvent(s"value-${event.value}")
  }

  override def shape: FlowShape[NumberEvent, TextEvent] = FlowShape.of(in, out)

  @scala.throws[Exception](classOf[Exception])
  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      // TODO: Implement the 'elements' stream logic : onPush() is called when an element is pushed to the Inlet
      // TODO: use push(...) to send an element to the Outlet
      // TODO: use grab(...) to take an element from the Inlet
      // TODO: use numberToText to make the actual mapping
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          push(out, numberToText(grab(in)))
        }
      })

      // TODO: Implement the 'request' stream logic : onPull() is called when downstream sends demand to the Outlet
      // TODO: use pull(...) to require demand from the Inlet
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
  }

}
