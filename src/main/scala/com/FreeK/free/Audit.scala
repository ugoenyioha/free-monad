package com.FreeK.free

import cats.{Id, ~>}

/**
  * Created by uenyioha on 2/11/17.
  */



// Audit dsl
object Audit {
  type UserId = String
  type Values = String
  type JobId = String

  sealed trait DSL[A]
  final case class UserAction(user: UserId, action: String, values: List[Values]) extends DSL[Unit]
  final case class SystemAction(job: JobId, action: String, values: List[Values]) extends DSL[Unit]
}

// Audit interpreter
object AuditInterpreter extends (Audit.DSL ~> Id) {
  import Audit._

  def apply[A](a: Audit.DSL[A]) = a match {
    case UserAction(user, action, values) =>
      println(s"[USER Action] - user $user called $action with values $values")
    case SystemAction(job, action, values) =>
      println(s"[SYSTEM Action] - $job called $action with values $values")
  }
}