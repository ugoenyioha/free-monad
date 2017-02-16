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

package com.example

import cats.data.Coproduct
import cats.free.Free
import cats.{ Id, ~> }
import com.example.free.Response

/**
 * Created by uenyioha on 2/11/17.
 */
object Main extends App {

  import com.example.free.AuditDSL._
  import com.example.free.LoggingDSL._
  import com.example.free.OrdersDSL._

  import com.example.free.OrdersInterpreters._
  import com.example.free.LoggingInterpreters._
  import com.example.free.AuditInterpreters._
  import com.example.free.MessagingInterpreters._

  import com.example.free.OrdersToMessageInterpreter._

  type TradeApp[A] = Coproduct[Orders, Log, A]
  type AuditableTradeApp[A] = Coproduct[Audit, TradeApp, A]

  // Now we have 2 interpreters, both from a Monad to Id.
  // We can compose the interpreters we want to use so the program knows what to do according to the Monad found
  // As we defined our composite Free Monad as a CoProduct (TradeApp) we declare a Natural Transformation
  // from TradeApp ~> Id.
  // The implementation can use Natural Transformation `or` method to compose our existing printers
  // Note that although we are creating a new interpreter, we are reusing existing ones to do so!
  def composedInterpreter: TradeApp ~> Id = orderPrinter or logPrinter

  // We do the same with the natural transformation, we chain our new `auditPrinter` and our previous `composedInterpreter`
  // (that resolves the TradeApp CoProduct).
  // Order matters due to `or` types (see its implementation to understand why)
  def auditableInterpreter: AuditableTradeApp ~> Id = auditPrinter or composedInterpreter

  def smartTradeWithAuditsAndLogs(implicit O: OrderI[AuditableTradeApp], L: LogI[AuditableTradeApp], A: AuditI[AuditableTradeApp]): Free[AuditableTradeApp, Response] = {
    import A._
    import L._
    import O._

    for {
      _ <- infoI("I'm going to trade smartly")
      _ <- userAction("ID102", "buy", List("APPL", "100"))
      _ <- buyI("APPL", 200)
      _ <- infoI("I'm going to trade even more smartly")
      _ <- userAction("ID102", "buy", List("MSFT", "100"))
      _ <- buyI("MSFT", 100)
      _ <- userAction("ID102", "sell", List("GOOG", "100"))
      rsp <- sellI("GOOG", 300)
      _ <- systemAction("BACKOFFICE", "tradesCheck", List("ID102", "lastTrades"))
      _ <- errorI("Wait, what?!")
    } yield rsp
  }

  //
  //  // If you run it you'll see this works and we see the expected output.
  //  println(s"> Smart trade - smartTradeWithAuditsAndLogs - ${smartTradeWithAuditsAndLogs.foldMap(auditableInterpreter)}")
  //  println()

  def ordersToTerminalViaMessage: Orders ~> Id =
    orderToMessageInterpreter andThen messagingFreePrinter

  def composedViaMessageInterpreter: TradeApp ~> Id =
    ordersToTerminalViaMessage or logPrinter

  def auditableToTerminalViaMessage: AuditableTradeApp ~> Id =
    auditPrinter or composedViaMessageInterpreter

  smartTradeWithAuditsAndLogs.foldMap(auditableToTerminalViaMessage)

}
