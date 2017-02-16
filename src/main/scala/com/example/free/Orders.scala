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
object OrdersDSL {
  type Symbol = String

  sealed trait Orders[A]
  case class Buy(stock: Symbol, amount: Int) extends Orders[Response]
  case class Sell(stock: Symbol, amount: Int) extends Orders[Response]
  case class ListStocks() extends Orders[List[Symbol]]

  // Wait, this doesn't build! Why? Ah, we have 2 different monads, which flatMap doesn't like.
  // Remember the signature of flatMap:  def flatMap(a: F[A])(f: A => F[B]): F[B]
  // We can't change our Type F in the middle of flatMap
  // But we want to use both Free in our code, otherwise it won't be too useful. Let's fix that

  // We use some smart constructors to create a monad that can be combined with others
  // This is an alternative to the `old way` of using `liftF` to lift the case class into Free.
  // Both can coexist together, no problem or you can use only one (depends on scenario)
  class OrderI[F[_]](implicit I: Inject[Orders, F]) {
    def buyI(stock: Symbol, amount: Int): Free[F, Response] = Free.inject[Orders, F](Buy(stock, amount))
    def sellI(stock: Symbol, amount: Int): Free[F, Response] = Free.inject[Orders, F](Sell(stock, amount))
  }

  // We need this implicit to convert to the proper instance when required
  implicit def orderI[F[_]](implicit I: Inject[Orders, F]): OrderI[F] = new OrderI[F]
}

object OrdersInterpreters {
  import OrdersDSL._
  // But this does nothing, by itself. We compile a for-comprehension, but it still has no logic associated
  // How to do something with it?
  // We need an interpreter that tells the code what to do. An interpreter is a natural transformation to Monad.
  // Ignore `natural transformation`, it's not relevant right now, assume it's magic.
  // What is the simplest Monad we know of? Id Monad. So we'll use it.
  // Note: on each case we need to return an element of the type we expect as 'result', so `Response` or equivalent.
  def orderPrinter: Orders ~> Id =
    new (Orders ~> Id) {
      def apply[A](fa: Orders[A]): Id[A] = fa match {
        case ListStocks() => // don't worry about this case by now, keep reading!
          println(s"Getting list of stocks: FB, TWTR")
          // Warning: if we use NIL here, the for comprehension will fall onto the failure side!
          // Same with Xor.Left and other values that may `fail fast` in a for-comprehension. Be careful!
          List("FB", "TWTR")
        case Buy(stock, amount) =>
          println(s"Buying $amount of $stock")
          "ok"
        case Sell(stock, amount) =>
          println(s"Selling $amount of $stock")
          "ok"
      }
    }
}
