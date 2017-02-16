/*
 * Copyright 2016 Pere Villega
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.free
import cats.{ Id, ~> }

/**
 * Created by uenyioha on 2/11/17.
 */
object MessagingDSL {

  import cats.free.Free
  import cats.free.Free._

  type ChannelId = String
  type MessageId = String
  type SourceId = String
  type Payload = String
  type Condition = String

  sealed trait Messaging[A]

  case class Publish(channelId: ChannelId, source: SourceId, messageId: MessageId, payload: Payload) extends Messaging[Response]
  case class Subscribe(channelId: ChannelId, filterBy: Condition) extends Messaging[Response]

  type MessagingF[A] = Free[Messaging, A]

  def publish(channelId: ChannelId, source: SourceId, messageId: MessageId, payload: Payload): MessagingF[Response] =
    liftF[Messaging, Response](Publish(channelId, source, messageId, payload))

  def subscribe(channelId: ChannelId, filterBy: Condition): MessagingF[Payload] =
    liftF[Messaging, Payload](Subscribe(channelId, filterBy))
}

object MessagingInterpreters {

  import MessagingDSL._

  def messagingPrinter: Messaging ~> Id =
    new (Messaging ~> Id) {
      def apply[A](fa: Messaging[A]): Id[A] =
        fa match {
          case Publish(channelId, source, messageId, payload) =>
            println(s"Publish [$channelId] From: [$source] Id: [$messageId] Payload: [$payload]")
            "ok"
          case Subscribe(channelId, filterBy) =>
            val payload = "Event fired"
            println(s"Received message from [$channelId](filter: [$filterBy]): [$payload]")
            payload
        }
    }

  def messagingFreePrinter: MessagingF ~> Id =
    new (MessagingF ~> Id) {
      def apply[A](fa: MessagingF[A]): Id[A] =
        fa.foldMap(messagingPrinter)
    }

}
