package com.FreeK.free

import cats.{Id, ~>}
import cats.implicits._
import cats.catsInstancesForId._
import cats.syntax.list._


/**
  * Created by uenyioha on 2/11/17.
  */
// Messaging dsl
object Messaging {
  type ChannelId = String
  type SourceId = String
  type MessageId = String
  type Payload = String
  type Condition = String
  type Response = String

  sealed trait DSL[A]
  final case class Publish(channelId: ChannelId, source: SourceId, messageId: MessageId, message: String) extends DSL[Response]
  final case class Subscribe(channelId: ChannelId, filterBy: Condition) extends DSL[List[Payload]]
}

// Messaging interpreter
object MessagingInterpreter extends (Messaging.DSL ~> Id) {
  import Messaging._

  def apply[A](a: Messaging.DSL[A]) = a match {
    case Publish(channelId, source, messageId, message) =>
      println(s"Publish [$channelId] From: [$source] Id: [$messageId] Payload: [$message]")
      "ok"
    case Subscribe(channelId, filterBy) =>
      val payload = "Event fired"
      println(s"Received message from [$channelId] (filter: [$filterBy]): [$payload]")
      List(payload)
  }
}