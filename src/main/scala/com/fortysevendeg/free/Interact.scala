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

package com.fortysevendeg.free

import scala.language.higherKinds

/**
 * Created by uenyioha on 2/11/17.
 */
object InteractDSL {

  import cats.free.Free
  import cats.free.Inject

  sealed trait Interact[A]
  case class Ask(prompt: String) extends Interact[String]
  case class Tell(msg: String) extends Interact[Unit]

  class Interacts[F[_]](implicit I: Inject[Interact, F]) {
    def tell(msg: String): Free[F, Unit] = Free.inject[Interact, F](Tell(msg))
    def ask(prompt: String): Free[F, String] = Free.inject[Interact, F](Ask(prompt))
  }

  implicit def interacts[F[_]](implicit I: Inject[Interact, F]): Interacts[F] = new Interacts[F]

}

import InteractDSL._
import cats.{ Id, ~> }

object InteractInterpreter extends ~>[Interact, Id] {
  def apply[A](i: Interact[A]) = i match {
    case Ask(prompt) => scala.io.StdIn.readLine(prompt)
    case Tell(msg)   => println(msg)
  }
}
