package io.scalac.rabbit

import io.scalac.amqp.{Direct, Exchange, Queue}


object RabbitRegistry {
  val sensorQueue = Queue("/topic/pilots/starterkit/sensor", durable = true)

  val announceQueue = Queue("/app/pilots/announce", durable = true)
  val powerQueue = Queue("/app/pilots/power", durable = true)
}
