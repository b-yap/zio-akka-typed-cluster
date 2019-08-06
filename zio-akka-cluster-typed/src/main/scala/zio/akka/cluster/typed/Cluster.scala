package zio.akka.cluster.typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.typed.{Join, Leave, Subscribe}
import zio.Queue

import scala.concurrent.{ExecutionContext, Future}
//import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.typed.{Cluster, JoinSeedNodes}
import zio.{Task, ZIO, Runtime}
import zio.Exit._

import scala.concurrent.duration._
import akka.util.Timeout

abstract class Cluster[T] {
  val mainBehavior:Behavior[String] = Behaviors.setup{ context =>
    println("behavior:")
    Behaviors.empty[String]
  }
  //val c = Cluster( ActorSystem(mainBehavior,"startme"))


   val cluster: ZIO[ActorSystem[T], Throwable, akka.cluster.typed.Cluster] =
     for {
       actorSystem <- ZIO.environment[ActorSystem[T]]
       c <- Task(Cluster(actorSystem))
     } yield c


  val clusterState: ZIO[ActorSystem[T], Throwable, CurrentClusterState] =
    for {
      c <- cluster
      state <- Task(c.state)
    } yield state


  def join(joinSeedNodes: JoinSeedNodes) : ZIO[ActorSystem[T], Throwable, Unit] =
    for {
      c <- cluster
      _ <- Task(joinSeedNodes.seedNodes.map(c.manager ! Join(_)))

    } yield ()


  def leave: ZIO[ActorSystem[T], Throwable, Unit] =
    for {
      c <- cluster
      _ <- Task(c.manager ! Leave(c.selfMember.address))
    } yield ()


  def clusterEvents(initialStateAsEvents: Boolean = false): ZIO[ActorSystem[T], Throwable, Queue[ClusterDomainEvent]] ={
    Queue.unbounded[ClusterDomainEvent].tap(clusterEventsWith(_,initialStateAsEvents))
    //Queue.unbounded[ClusterDomainEvent].tap()
  }

  def clusterEventsWith(queue: Queue[ClusterDomainEvent],
                        initialStateAsEvents: Boolean = false): ZIO[ActorSystem[T], Throwable, Unit] =
    for{
      rts <- ZIO.runtime[Any]
      c <- cluster
      sys <- ZIO.environment
      _ <- ZIO.fromFuture(implicit ec => createSubscriberActor(rts, queue, sys, c))
    } yield ()

    def createSubscriberActor(rts: Runtime[Any],
                              queue: Queue[ClusterDomainEvent],
                              system: ActorSystem[T],
                              cluster: akka.cluster.typed.Cluster
                             )(implicit ec:ExecutionContext): Future[ActorRef[ClusterDomainEvent]] = {
      val subscriberActor = Behaviors.setup { context =>
        val subscriptions = cluster.subscriptions

        subscriptions ! Subscribe(context.self, classOf[ClusterDomainEvent])
        Behaviors.receiveMessage[ClusterDomainEvent] { ev =>
          rts.unsafeRunAsync(queue.offer(ev)) {
            case Success(_) => Behaviors.same
            case Failure(cause) => if(cause.interrupted) Behaviors.stopped
          }
          Behaviors.same
        }
      }
      system.systemActorOf(subscriberActor, "subscriber-actor")(Timeout(30 seconds))
    }
  }
