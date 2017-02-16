import sbt._

object Version {
  final val Scala = "2.11.8"
  final val ScalaCheck = "1.13.4"
  final val Cats = "0.9.0"
  final val ScalaTest = "3.0.1"
  final val catsScalatest = "2.2.0"
  final val FreeK = "0.6.6"
  final val scalaz = "7.3.0-M8"
  final val simulacrum = "0.10.0"
  final val monix = "2.2.1"
  final var eff = "2.0.0"
}

object Library {
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.ScalaCheck
  val scalaz_scalacheck = "org.scalaz" %% "scalaz-scalacheck-binding" % Version.scalaz
  val cats = "org.typelevel" %% "cats" % Version.Cats
  val cats_scalatest = "com.ironcorelabs" %% "cats-scalatest" % Version.catsScalatest
  val scalaTest = "org.scalatest" %% "scalatest" % Version.ScalaTest
  val freek = "com.projectseptember" %% "freek" % Version.FreeK
  val scalaz =  "org.scalaz" %% "scalaz-core" % Version.scalaz
  val scalaz_concurrent = "org.scalaz" %% "scalaz-concurrent" % Version.scalaz
  val simulacrum = "com.github.mpilquist" % "simulacrum_2.11" % Version.simulacrum
  val monixTasks = "io.monix" %% "monix" % Version.monix
  val monixCats = "io.monix" %% "monix-cats" % Version.monix
  val monixScalaz = "io.monix" %% "monix-scalaz-72" % Version.monix
  val eff = "org.atnos" %% "eff" % Version.eff
  val eff_scalaz = "org.atnos" %% "eff-scalaz" % "2.0.0"
  val eff_monix = "org.atnos" %% "eff-monix" % "2.0.0"
}
