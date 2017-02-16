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

import cats.data.Coproduct
import cats.implicits._
import cats.~>
import com.fortysevendeg.free.DataDSL._
import com.fortysevendeg.free.InteractDSL._
import monix.eval.Task

import scala.collection.mutable.ListBuffer
import scala.language.higherKinds
import scala.util.Try

/**
 * Created by uenyioha on 2/11/17.
 */

@simulacrum.typeclass trait Capture[M[_]] {
  def capture[A](a: => A): M[A]
}

class Interpreters[M[_]: Capture] {

  def InteractInterpreter: Interact ~> M = new (Interact ~> M) {
    def apply[A](i: Interact[A]) = i match {
      case Ask(prompt) => Capture[M].capture({ scala.io.StdIn.readLine(prompt)})
      case Tell(msg)   => Capture[M].capture({ println(msg) })
    }
  }

  def InMemoryDataOpInterpreter: DataOp ~> M = new (DataOp ~> M) {
    private[this] val memDataSet = new ListBuffer[String]

    def apply[A](fa: DataOp[A]) = fa match {
      case AddCat(a)    => Capture[M].capture({ memDataSet.append(a); a })
      case GetAllCats() => Capture[M].capture(memDataSet.toList)
    }
  }

  type Application[A] = Coproduct[Interact, DataOp, A]

  def interpreter: Application ~> M =
    InteractInterpreter or InMemoryDataOpInterpreter

  implicit val taskCaptureInstance = new Capture[Task] {
    override def capture[A](a: => A): Task[A] = Task.evalOnce(a)
  }

}

object Interpreters {


  type Result[A] = Either[Throwable, A]

  implicit val xorCaptureInstance = new Capture[Result] {
    override def capture[A](a: => A): Result[A] = Either.catchNonFatal(a)
  }

  implicit val tryCaptureInstance = new Capture[Try] {
    override def capture[A](a: => A): Try[A] = Try(a)
  }

  implicit val taskCaptureInstance = new Capture[Task] {
    override def capture[A](a: => A): Task[A] = Task.evalOnce(a)
  }

  val xorInterpreter = new Interpreters[Result].interpreter

  val taskInterpreter = new Interpreters[Task].interpreter

  val tryInterpreter = new Interpreters[Try].interpreter
}
