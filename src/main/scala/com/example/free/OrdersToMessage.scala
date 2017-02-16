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

import java.util.UUID

/**
 * Created by uenyioha on 2/11/17.
 */
object OrdersToMessageInterpreter {
  import OrdersDSL._
  import MessagingDSL._
  import cats.~>

  def orderToMessageInterpreter: Orders ~> MessagingF =
    new (Orders ~> MessagingF) {
      def apply[A](fa: Orders[A]): MessagingF[A] = {
        fa match {
          case ListStocks() =>
            for {
              _ <- publish("001", "Orders", UUID.randomUUID().toString, "Get Stocks List")
              payload <- subscribe("001", "*")
            } yield List(payload)
          case Buy(stock, amount) =>
            publish("001", "Orders", UUID.randomUUID().toString, s"Buy $stock $amount")
          case Sell(stock, amount) =>
            publish("001", "Orders", UUID.randomUUID().toString, s"Sell $stock $amount")
        }
      }
    }
}
