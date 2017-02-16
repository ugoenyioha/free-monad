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

import cats.free.{ Free, Inject }
import cats.{ Id, ~> }

/**
 * Created by uenyioha on 2/11/17.
 */
object LoggingDSL {
  // Let's grow our application a bit. If we want to go to production, we need to add Logs to it.
  // But logging, that's a second language completely different than `Orders`!
  // Let's define it:
  sealed trait Log[A]
  case class Info(msg: String) extends Log[Unit]
  case class Error(msg: String) extends Log[Unit]

  // We repeat the process with Log. It's kind of *boilerplate*, another way to lift case class to Free
  class LogI[F[_]](implicit I: Inject[Log, F]) {
    def infoI(msg: String): Free[F, Unit] = Free.inject[Log, F](Info(msg))

    def errorI(msg: String): Free[F, Unit] = Free.inject[Log, F](Error(msg))
  }

  //another implicit necessary to convert to the proper instance
  implicit def logI[F[_]](implicit I: Inject[Log, F]): LogI[F] = new LogI[F]
}

object LoggingInterpreters {
  import LoggingDSL._

  // Ok, it builds! But who's interpreting Log? We didn't create any interpreter!
  // We can fix that. Let's build an interpreter from Log to Id, as before, that will use println for output
  def logPrinter: Log ~> Id =
    new (Log ~> Id) {
      def apply[A](fa: Log[A]): Id[A] = fa match {
        case Info(msg)  => println(s"[Info] - $msg")
        case Error(msg) => println(s"[Error] - $msg")
      }
    }
}
