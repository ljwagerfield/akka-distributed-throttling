package example

import akka.actor.{Address, ActorSystem}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

/**
 * Demonstrates how to use Akka Clustering to synchronize `Int` values between distributed nodes that `sum` to a
 * predefined value.
 */
object Application {

  final val SystemName = "shared-quota"

  final val LocalHost = "127.0.0.1"

  /**
   * Application entry point.
   * @param args Arguments. Expects none.
   */
  def main(args: Array[String]): Unit = {

    val numberOfNodes = 3
    val numberOfSeeds = 1
    assert(numberOfSeeds <= numberOfNodes)

    val firstPort = 8001
    val lastPort = (firstPort + numberOfNodes) - 1

    val nodes = (firstPort to lastPort).map(port => (port, createNode(port)))
    val actorRefs = nodes.map(_._2)
    val seeds = nodes.take(numberOfSeeds).map(node => Address("akka.tcp", SystemName, LocalHost, node._1))

    nodes.foreach(n => n._2.localQuota.subscribe(localQuota => println(s"${System.currentTimeMillis} node ${n._1} has quota $localQuota")))

    actorRefs.foreach(actorRef => Cluster(actorRef.system).joinSeedNodes(seeds))
  }

  /**
   * Creates a node listening on the specified port with a single actor.
   * @param port Port to listen on.
   * @return Actor reference.
   */
  private def createNode(port: Int): SharedQuotaActorRef = {
    val config = ConfigFactory
      .parseString("akka.remote.netty.tcp.port=" + port)
      .withFallback(ConfigFactory.load)
    implicit val system = ActorSystem(SystemName, config)
    SharedQuotaActorRef("quota-consumer")
  }
}
