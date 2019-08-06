package zio.akka.cluster.demo

import zio._
import _root_.akka.actor.typed.scaladsl.Behaviors
import _root_.akka.actor.typed.{ActorSystem, Behavior}

object MainApp extends App {

  sealed trait ChatMessage
  case class Message(name: String, msg: String) extends ChatMessage
  case class Join(name: String)                 extends ChatMessage
  case class Leave(name: String)                extends ChatMessage


  def testBehavior = Behaviors.setup {
    context =>
      println("Test me out")
      Behaviors.empty
  }


  override def run(args: List[String]): ZIO[Environment, Nothing,Int] =
    Managed.make(Task(ActorSystem(testBehavior,"weeee")))(sys => Task(sys.terminate()).ignore)


}
