package xke.akkastream.step3

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import xke.akkastream.step3.Step_3.{ComputedEvent, LetterEvent, NumberEvent}

import scala.concurrent.Future
import scala.util.{Failure, Random, Success, Try}

object Step_3_Run extends App {
  new Step_3_0_UnhandledFailure
}

object Step_3 {

  case class LetterEvent(value: String)
  case class NumberEvent(value: Int)
  case class ComputedEvent(value: Int)
}

class Step_3 {

  implicit val system = ActorSystem("step-3")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

  val possibleChars: List[String] = Range(0, 10).map(_.toString).toList :+ "a"
  lazy val source: Source[LetterEvent, _] = Source.fromIterator(
    () => Iterator.continually(LetterEvent(possibleChars(Random.nextInt(possibleChars.size))))
  )

  // Failable conversion
  def letterToNumber(event: LetterEvent): NumberEvent = {
    Thread.sleep(500)
    NumberEvent(event.value.toInt)
  }

  def computeFromNumber(event: NumberEvent): ComputedEvent = {
    Thread.sleep(500)
    ComputedEvent(100 / event.value)
  }
}

class Step_3_0_UnhandledFailure extends Step_3 {

  def letterToNumberAsync(event: LetterEvent): Future[NumberEvent] = Future(letterToNumber(event))

  lazy val sink: Sink[NumberEvent, Future[Done]] = Sink.foreach(event => {
    println(event)
  })

  lazy val stream = source
    .mapAsync(4)(letterToNumberAsync)
    .runWith(sink)

  stream.onFailure {
    case error: Throwable => println(error)
  }
}

class Step_3_1_Handle_Error extends Step_3 {

  // TODO: Write a function to return a Try to the stream
  def safeLetterToNumberAsync(event: LetterEvent): Future[Try[NumberEvent]] = Future {
    Try(letterToNumber(event))
  }

  lazy val sink: Sink[Try[NumberEvent], Future[Done]] = Sink.foreach({
    case Success(event) => println(s"Success : $event")
    case Failure(error) => println(s"Error : $error")
  })

  lazy val stream = source
    .mapAsyncUnordered(4)(safeLetterToNumberAsync)
    .runWith(sink)

  stream.onFailure {
    case error: Throwable => println(error)
  }
}

class Step_3_2_Chain_Handle_Error extends Step_3 {

  // TODO: Reuse previous implementation
  def safeLetterToNumberAsync(event: LetterEvent): Future[Try[NumberEvent]] = Future {
    Try(letterToNumber(event))
  }

  // TODO: Write a function to return a Try to the stream
  def safeComputeFromNumberAsync(previous: Try[NumberEvent]): Future[Try[ComputedEvent]] = Future {
    previous.flatMap(event => Try(computeFromNumber(event)))
  }

  lazy val sink: Sink[Try[ComputedEvent], Future[Done]] = Sink.foreach({
    case Success(event) => println(s"Success : $event")
    case Failure(error) => println(s"Error : $error")
  })

  lazy val stream = source
    .mapAsyncUnordered(4)(safeLetterToNumberAsync)
    .mapAsyncUnordered(4)(safeComputeFromNumberAsync)
    .runWith(sink)

  stream.onFailure {
    case error: Throwable => println(error)
  }
}

class Step_3_3_Materialized_Error_Failure extends Step_3 {

  // TODO: Reuse previous implementation
  def safeLetterToNumberAsync(event: LetterEvent): Future[Try[NumberEvent]] = Future {
    Try(letterToNumber(event))
  }

  // TODO: Reuse previous implementation
  def safeComputeFromNumberAsync(previous: Try[NumberEvent]): Future[Try[ComputedEvent]] = Future {
    previous.flatMap(event => Try(computeFromNumber(event)))
  }

  lazy val textSource = Source("423567a5608a550a42".map(c => LetterEvent(c.toString)))


  lazy val stream = textSource
    .mapAsyncUnordered(4)(safeLetterToNumberAsync)
    .mapAsyncUnordered(4)(safeComputeFromNumberAsync)
  // TODO: Replace 'runWith(Sink.ignore)' by an implementation returning a result
  // TODO: The result will be a concatenated String where Error values are replace with "_"
    .runFold("")((result, element) => result + element.map(_.value).toOption.getOrElse("_"))

  stream.onComplete {
    case Success(value) => println(s"Result: $value")
    case Failure(error) => println(s"Failure: $error")
  }
}
