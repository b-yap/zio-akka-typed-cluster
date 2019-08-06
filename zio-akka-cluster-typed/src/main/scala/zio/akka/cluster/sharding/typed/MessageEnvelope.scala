package zio.akka.cluster.sharding.typed

import zio.akka.cluster.sharding.typed.MessageEnvelope.Payload

case class MessageEnvelope(entityId: String, data: Payload)

object MessageEnvelope {

  sealed trait Payload
  case object PoisonPillPayload            extends Payload
  case class MessagePayload[Msg](msg: Msg) extends Payload

}
