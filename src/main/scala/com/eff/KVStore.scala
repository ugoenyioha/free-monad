package com.eff

import org.atnos.eff._, all._
/**
  * Created by uenyioha on 2/13/17.
  */
object KVStore extends App {

  sealed trait Interact[A]

  case class Ask(prompt: String) extends Interact[String]
  case class Tell(prompt: String) extends Interact[Unit]

  type _interact[R] = Interact |= R

  def askUser[R :_interact](prompt: String) : Eff[R, String]
    = send(Ask(prompt))

  def tellUser[R :_interact](message: String) : Eff[R, Unit]
    = send(Tell(message))


  sealed trait DataOp[A]

  type _dataOp[R] = DataOp |= R

  case class AddCat(a: String) extends DataOp[Unit]
  case class GetAllCats() extends DataOp[List[String]]

  def addCat[R: _dataOp](a: String) : Eff[R, Unit] =
    send(AddCat(a))

  def getAllCats[R: _dataOp] : Eff[R, List[String]] =
    send(GetAllCats())
}