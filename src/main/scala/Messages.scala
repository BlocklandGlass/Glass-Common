package blocklandglass.messages

import akka.actor._
import akka.util._

import blocklandglass._

trait Message {
	def serialize: Seq[String]
	def serializeToString: String = serialize.mkString("\t")+"\r\n"
	def serializeToByteString: ByteString = ByteString(serializeToString, "UTF-8")
}

object Message {
	def processMessages[T <: Message](actor: ActorRef, reader: messages.MessageReader[T]) = IO repeat {
		for {
			message <- reader.read
		} yield (actor ! ParsedMessage(message))
	}
}

case class ParsedMessage[T <: Message](message: Option[T])

trait MessageReader[T <: Message] {
	def read: IO.Iteratee[Option[T]] = for {
		message <- IO takeUntil ByteString("\r\n", "UTF-8")
	} yield parse(message.decodeString("UTF-8").split("\t"))
	protected def parse(header: Seq[String]): Option[T]
}