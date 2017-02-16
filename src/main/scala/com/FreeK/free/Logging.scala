package com.FreeK.free

/**
  * Created by uenyioha on 2/11/17.
  */

import cats.{Id, ~>}

// Log dsl
object Log {
  sealed trait DSL[A]
  final case class Info(msg: String) extends DSL[Unit]
  final case class Error(msg: String) extends DSL[Unit]
}

// Defining the interpreter for Log
object LogInterpreter extends (Log.DSL ~> Id) {
  import Log._

  def apply[A](a: Log.DSL[A]) = a match {
    case Info(msg) =>
      println(s"[Info] - $msg")
    case Error(msg) =>
      println(s"[Error] - $msg")
  }
}