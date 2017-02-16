package com.FreeK

import cats.Id
import cats.free.Free
import com.FreeK.free._
import freek._

/**
  * Created by uenyioha on 2/11/17.
  */
object Main extends App {
  import com.FreeK.free.Audit
  import com.FreeK.free.Audit._

  import com.FreeK.free.Log
  import com.FreeK.free.Log._

  import com.FreeK.free.Orders
  import com.FreeK.free.Orders._

  type PRG = Log.DSL :|: Audit.DSL :|: Orders.DSL :|: NilDSL
  val PRG = DSL.Make[PRG]

  type Response = String

  val program: Free[PRG.Cop, Response] = for {
    _ <- Info("I'm going to trade smartly").freek[PRG]
    _ <- UserAction("ID102", "buy", List("APPL", "200")).freek[PRG]
    _ <- Buy("APPL", 200).freek[PRG]
    _ <- Info("I'm going to trade even more smartly").freek[PRG]
    _ <- UserAction("ID102", "buy", List("MSFT", "100")).freek[PRG]
    _ <- Buy("MSFT", 100).freek[PRG]
    _ <- UserAction("ID102", "sell", List("GOOG", "300")).freek[PRG]
    rsp <- Sell("GOOG", 300).freek[PRG]
    _ <- SystemAction("BACKOFFICE", "tradesCheck", List("ID102", "lastTrades")).freek[PRG]
    _ <- Error("Wait, what?!").freek[PRG]
  } yield rsp

  import cats.syntax.list._
  import cats.implicits._
  import cats.syntax.traverse._

  val programWithList : Free[PRG.Cop, Response] = for {
    st <- ListStocks().freek[PRG]
    _ <- st.traverseU_(Buy(_, 100).freek[PRG])
    rsp <- Sell("GOOG", 100).freek[PRG]
  } yield rsp

  val interpreter = LogInterpreter :&: AuditInterpreter :&: OrderInterpreter

  program.interpret(interpreter)

  println()

  programWithList.interpret(interpreter)

  println()

  val interpreterWithMessaging =
    LogInterpreter :&: AuditInterpreter :&: (MessagingInterpreter compose OrdersToMessagesInterpreter)

  program.interpret(interpreterWithMessaging)

}
