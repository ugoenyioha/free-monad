package com.FreeK.free

import java.util.UUID

import cats.~>

/**
  * Created by uenyioha on 2/12/17.
  */
object OrdersToMessagesInterpreter extends (Orders.DSL ~> Messaging.DSL) {
  import Orders._
  import Messaging._

  def apply[A](a: Orders.DSL[A]) = a match {
    case ListStocks() =>
      //TODO: this needs a bit more work to make it fully equivalent
      Publish("001", "Orders", UUID.randomUUID().toString, "Get Stocks List")
      Subscribe("001", "*")
    case Buy(stock, amount) =>
      Publish("001", "Orders", UUID.randomUUID().toString, s"Buy $stock $amount")
    case Sell(stock, amount) =>
      Publish("001", "Orders", UUID.randomUUID().toString, s"Sell $stock $amount")
  }
}