package com.free_applicatives

import cats.{Monad, ~>}
import cats.free.{Free, FreeApplicative}
import cats.arrow.FunctionK

import scala.language.higherKinds

// needs

// addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")
// addCompilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full)
// addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)


object SeqPar {

  //type SeqPar f a = Free (FreeAp f) a

  type SeqPar[F[_], A] = Free[FreeApplicative[F, ?], A]

  object SeqPar {

    // liftFA :: forall f a. f a -> SeqPar f a
    // liftFA = pure >>> pure

    def liftFA[F[_], A](fa: F[A]): SeqPar[F, A] =
      Free.liftF[FreeApplicative[F, ?], A](FreeApplicative.lift[F[?], A](fa))

    // liftSeq :: forall f a. Free f a -> SeqPar f a
    // liftSeq fa = foldFree fa liftFA

    def liftSeq[F[_], A](free: Free[F, A]): SeqPar[F, A] = {
      val fxn : F ~> FreeApplicative[F, ?] =
        new ~>[F, FreeApplicative[F, ?]] {
          override def apply[C](fa: F[C]): FreeApplicative[F, C] = FreeApplicative.lift(fa)
        }

      free.compile[FreeApplicative[F, ?]](fxn)
    }

    def headOption[A](list: List[A]): Option[A] = list.headOption
    val lifted: FunctionK[List, Option] = FunctionK.lift(headOption)

    def functionA[F[_], C](fa: F[C]): FreeApplicative[F, C] = FreeApplicative.lift(fa)
    val lifted2 = FunctionK.lift(functionA)

    // free.compile(FunctionK.lift[F, FreeApplicative[F, ?]](FreeApplicative.lift(fa))))
    // free.foldMap(λ[(Free[F, ?] ~> SeqPar[F, ?])](liftFA))
    // final def compile[T[_]](f: FunctionK[S, T]): Free[T, A]
    // def pure[S[_], A](a: A): Free[S, A] = Pure(a)

    // liftPar :: forall f a. FreeAp f a -> SeqPar f a
    // liftPar = pure

    def liftPar[F[_], A](freeap: FreeApplicative[F, A]): SeqPar[F, A] =
      Free.liftF[FreeApplicative[F, ?], A](freeap)
  }

  // -- Interprets a parallel fragment `f` into `g`:
  // type ParInterpreter f g = FreeAp f ~> g

  type ParInterpreter[F[_], G[_]] = FreeApplicative[F, ?] ~> G

  // -- Optimizes a parallel fragment `f` into a sequential series of parallel program fragments in `g`:
  // type ParOptimizer f g = ParInterpreter f (SeqPar g)

  type ParOptimizer[F[_], G[_]] = ParInterpreter[F, SeqPar[G, ?]]


  implicit class SeqParOps[F[_], A](seqpar: SeqPar[F, A]) {

    // -- Applies the most general optimization from a parallel program fragment in `f` to a sequential
    // -- series of parallel program fragments in `g`:
    // optimize :: forall f g a. (FreeAp f ~> SeqPar g) -> SeqPar f a -> SeqPar g a
    // optimize = foldFree

    // Free[FreeApplicative[F, ?], A]

    def optimize[G[_]](opt: FreeApplicative[F, ?] ~> SeqPar[G, ?]): SeqPar[G, A] =
      seqpar.foldMap[SeqPar[G, ?]](opt)

    // -- Applies a parallel-to-parallel optimization:
    // parOptimize :: forall f g a. (FreeAp f ~> FreeAp g) -> SeqPar f a -> SeqPar g a
    // parOoptimize opt = optimize (opt >>> liftPar)

    def parOptimize[G[_]](opt: FreeApplicative[F, ?] ~> FreeApplicative[G, ?]): SeqPar[G, A] =
      seqpar.compile[FreeApplicative[G, ?]](opt)
    // optimize(f andThen FunctionK.lift(SeqPar.liftPar))
    // optimize(opt andThen λ[(FreeApplicative[G, ?] ~> SeqPar[G, ?])](SeqPar.liftPar(_)))

    // -- Runs a seq/par program by converting each parallel fragment in `f` into an `IO`:
    // run :: forall f a. (FreeAp f ~> IO) -> SeqPar f a -> IO a
    // run = foldFree

    def run[H[_]: Monad](f: FreeApplicative[F, ?] ~> H): H[A] =
      seqpar.foldMap(f)
  }

}