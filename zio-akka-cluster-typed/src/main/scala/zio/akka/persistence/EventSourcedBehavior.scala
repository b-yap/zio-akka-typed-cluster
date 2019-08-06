package zio.akka.persistence


import akka.actor.typed.{ActorSystem, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import zio.{Task, ZIO, Runtime}

object EventSourcedBehavior {

  def apply[Command, Event, State](persistenceId: PersistenceId,
                                   emptyState: State,
                                   commandHandler: (State, Command) => ZIO[Any, Throwable, Effect[Event,State]],
                                   eventHandler: (State,Event) => State)(implicit rts: Runtime[Any]):
  ZIO[Any, Throwable, EventSourcedBehavior[Command,Event,State]]= {
    val commandH = ZIO.effect((s: State, c: Command) => rts.unsafeRun(commandHandler(s,c)))
    for{
      comH <- commandH
      evSBehaviorImpl <-
        Task(akka.persistence.typed.scaladsl.EventSourcedBehavior(persistenceId,
        emptyState,comH,eventHandler))
    }yield evSBehaviorImpl
  }
}

class ActorSystem[T] {

  def apply(zioBehavior: Task[Behavior[T]],name:String): ZIO[Any,Throwable,akka.actor.typed.ActorSystem[T]] = {
   for {
      guardianBehavior <- zioBehavior
      actorSystem <- Task(ActorSystem(guardianBehavior, name))
    } yield actorSystem
  }

}






