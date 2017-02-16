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
object AuditDSL {

  type UserId = String
  type JobId = String
  type Values = String

  // 2 languages is nice. But what if we want more than just 2 languages? Let's see how it would work.
  // Let's say our api, being related to stocks, needs auditing. Not logging, auditing, a new language.
  // So we define a new language (the details themselves are irrelevant, we care about mixing them)
  sealed trait Audit[A]

  case class UserActionAudit(user: UserId, action: String, values: List[Values]) extends Audit[Unit]
  case class SystemActionAudit(job: JobId, action: String, values: List[Values]) extends Audit[Unit]

  // We build another class to lift to a Monad
  class AuditI[F[_]](implicit I: Inject[Audit, F]) {
    def userAction(user: UserId, action: String, values: List[Values]): Free[F, Unit] = Free.inject[Audit, F](UserActionAudit(user, action, values))

    def systemAction(job: JobId, action: String, values: List[Values]): Free[F, Unit] = Free.inject[Audit, F](SystemActionAudit(job, action, values))
  }
  // implicit still necessary
  implicit def auditI[F[_]](implicit I: Inject[Audit, F]): AuditI[F] = new AuditI[F]
}

object AuditInterpreters {

  import AuditDSL._

  // And we add a basic interpreter to Id that prints to console, as usual
  def auditPrinter: Audit ~> Id =
    new (Audit ~> Id) {
      def apply[A](fa: Audit[A]): Id[A] = fa match {
        case UserActionAudit(user, action, values)  => println(s"[USER Action] - user $user called $action with values $values")
        case SystemActionAudit(job, action, values) => println(s"[SYSTEM Action] - $job called $action with values $values")
      }
    }

}
