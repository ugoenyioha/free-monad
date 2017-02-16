package com.FreeK.free

import cats.{Id, ~>}

/**
  * Created by uenyioha on 2/11/17.
  */
object Orders {
  type Symbol = String
  type Response = String

  sealed trait DSL[A]
  final case class ListStocks() extends DSL[List[Symbol]]
  final case class Sell(stock: Symbol, amount: Int) extends DSL[Response]
  final case class Buy(stock: Symbol, amount: Int) extends DSL[Response]
}

// Defining the interpreter for Order, slightly differently
object OrderInterpreter extends (Orders.DSL ~> Id) {
  import Orders._

  def apply[A](a: Orders.DSL[A]) = a match {
    case ListStocks() =>
      println(s"Getting list of stocks: FB, TWTR")
      List("FB", "TWTR")
    case Buy(stock, amount) =>
      println(s"Buying $amount of $stock")
      "ok"
    case Sell(stock, amount) =>
      println(s"Selling $amount of $stock")
      "ok"
  }
}