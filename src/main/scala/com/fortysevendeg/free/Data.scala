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

package com.fortysevendeg.free

/**
 * Created by uenyioha on 2/11/17.
 */
object DataDSL {
  import cats.free.Free
  import cats.free.Inject

  sealed trait DataOp[A]
  case class AddCat(a: String) extends DataOp[String]
  case class GetAllCats() extends DataOp[List[String]]

  class DataOps[F[_]](implicit I: Inject[DataOp, F]) {
    def addCat(a: String): Free[F, String] = Free.inject[DataOp, F](AddCat(a))
    def getAllCats: Free[F, List[String]] = Free.inject[DataOp, F](GetAllCats())
  }

  implicit def dataOps[F[_]](implicit I: Inject[DataOp, F]): DataOps[F] = new DataOps[F]

}

import DataDSL._
import cats.{ Id, ~> }
import scala.collection.mutable.ListBuffer

object InMemoryDataOpInterpreter extends ~>[DataOp, Id] {
  private[this] val memDataSet = new ListBuffer[String]

  def apply[A](fa: DataOp[A]) = fa match {
    case AddCat(a)    =>
      memDataSet.append(a); a
    case GetAllCats() => memDataSet.toList
  }
}
