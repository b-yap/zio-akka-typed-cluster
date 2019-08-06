package zio.akka.cluster

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.MemberLeft
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}
import zio.akka.cluster.typed.Cluster
import zio.{DefaultRuntime, Managed, Task, ZIO}

class ClusterSpec extends FlatSpec with Matchers with DefaultRuntime {
  val config: Config = ConfigFactory.parseString(
    s"""
       |akka {
       |  actor {
       |    provider = "cluster"
       |  }
       |  remote {
       |    netty.tcp {
       |      hostname = "127.0.0.1"
       |      port = 2551
       |    }
       |  }
       |  cluster {
       |    seed-nodes = ["akka.tcp://Test@127.0.0.1:2551"]
       |  }
       |}
           """.stripMargin)


  def testBehavior = Behaviors.setup {
    context =>
      println("Test me out")
      Behaviors.empty
  }

  def initialize[T](guardian: Behavior[T]): Task[ActorSystem[T]] =
    Task(ActorSystem(guardian, "weeee"))

  val actorSystem: Managed[Throwable, ActorSystem[Nothing]] =
    Managed.make(Task(ActorSystem(testBehavior,"weee",config)))(sys => Task(sys.terminate()).ignore)

  "ClusterSpec" should "receive cluster events" in {
    object ZIOCluster extends Cluster[Nothing]

    unsafeRun(
      actorSystem.use(
        actorSystem =>
          (for {
            queue <- ZIOCluster.clusterEvents()
            _     <- ZIOCluster.leave
            item  <- queue.take
          } yield item).provide(actorSystem)
      )
    ) shouldBe a[MemberLeft]
  }






}
