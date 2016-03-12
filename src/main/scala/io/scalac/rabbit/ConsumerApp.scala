package io.scalac.rabbit

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.typesafe.scalalogging.slf4j.LazyLogging
import io.scalac.amqp.{Queue, Connection, Message}
import io.scalac.rabbit.RabbitRegistry._

import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure


object ConsumerApp extends App with FlowFactory with LazyLogging {

  implicit val actorSystem = ActorSystem("rabbit-akka-stream")

  import actorSystem.dispatcher

  implicit val materializer = FlowMaterializer()

  val connection = Connection()

  setupRabbit() onComplete {
    case Success(_) =>
      logger.info("Exchanges, queues and bindings declared successfully.")

      //val rabbitConsumer = Source(connection.consume(sensorQueue.name))

      val dummyAnnounce = "{ \"teamId\": \"starterkit\", \"accessCode\": null, \"timestamp\": " + new Date().getTime() + " }" :: Nil
      Source(dummyAnnounce).
        map(msg => Message(ByteString(msg))).
        runWith(Sink(connection.publishDirectly(announceQueue.name)))
        logger.info("bla")
      val dummyPower = "{ \"p\": 255, \"teamId\": \"starterkit\", \"accessCode\": null, \"timeStamp\": " + new Date().getTime() + " }" :: Nil
      for(x <- 1 until 10) {
        Source(dummyPower).
          map(msg => Message(ByteString(msg))).
          runWith(Sink(connection.publishDirectly(powerQueue.name)))
      }

    case Failure(ex) =>
      logger.error("Failed to declare RabbitMQ infrastructure.", ex)
  }

  def setupRabbit(): Future[List[Queue.DeclareOk]] =
    Future.sequence(List(

      /* declare and bind inbound exchange and queue */
      Future.sequence {
        connection.queueDeclare(sensorQueue) :: Nil
      } /** flatMap { _ =>
        Future.sequence {
          connection.queueBind(sensorQueue.name, "", sensorQueue.name) :: Nil
        }
      }**/,

      /* declare and bind outbound exchange and queues */
      Future.sequence {
        connection.queueDeclare(announceQueue) ::
          connection.queueDeclare(powerQueue) :: Nil
      } /** flatMap { _ =>
        Future.sequence {
          connection.queueBind(announceQueue.name, "", announceQueue.name) ::
            connection.queueBind(powerQueue.name, "", powerQueue.name) :: Nil
        }
      } **/
    )).map {
      _.flatten
    }
}