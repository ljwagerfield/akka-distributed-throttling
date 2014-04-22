package example

import akka.actor.{ActorSystem, ActorRef, Props}
import rx.lang.scala.{Subject, Observable}

/**
 * Strongly typed facade for 'shared quota' actor references.
 * @param localQuota Quota negotiated for this node.
 */
class SharedQuotaActorRef(val localQuota: Observable[Int], val innerRef: ActorRef, val system: ActorSystem)

/**
 * Factory object for 'shared quota' actor references.
 */
object SharedQuotaActorRef {

  /**
   * Creates a submissions HTTP service actor reference.
   * @param name Actor name.
   * @param system System to create actor within.
   * @return Actor reference.
   */
  def apply(name: String)(implicit system: ActorSystem): SharedQuotaActorRef = {
    val localQuota = Subject[Int]()
    val actorRef = system.actorOf(Props(new SharedQuotaActor(localQuota.onNext)), name = name)
    new SharedQuotaActorRef(localQuota, actorRef, system)
  }
}
