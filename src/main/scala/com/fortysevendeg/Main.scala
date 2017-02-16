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

package com.fortysevendeg

import cats.{Id, ~>}
import cats.data.Coproduct
import com.fortysevendeg.free.InteractDSL._
import com.fortysevendeg.free.DataDSL._
import com.fortysevendeg.free.{InMemoryDataOpInterpreter, InteractInterpreter, Interpreters}

import scala.language.postfixOps

/**
 * Created by uenyioha on 2/11/17.
 */
object Main extends App {

  import cats.free.Free

  type Application[A] = Coproduct[Interact, DataOp, A]

  def program(implicit I: Interacts[Application], D: DataOps[Application]): Free[Application, Unit] = {

    import I._, D._

    for {
      cat <- ask("What's the kitty's name?")
      _ <- addCat(cat)
      cats <- getAllCats
      _ <- tell(cats.toString)
    } yield ()

  }

  import cats.implicits._
  import monix.cats._
  import monix.cats.monixToCatsMonad

  import Interpreters._

  val interpreter: Application ~> Id = InteractInterpreter or InMemoryDataOpInterpreter

  import monix.execution.Scheduler.Implicits.global
  import scala.concurrent.Await
  import scala.concurrent.duration._

  Await.result((program foldMap taskInterpreter).runAsync, 3 seconds)
}
