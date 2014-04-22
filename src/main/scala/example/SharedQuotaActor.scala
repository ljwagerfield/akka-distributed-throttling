package example

import akka.cluster.{Member, Cluster}
import akka.cluster.ClusterEvent._
import akka.actor.{Address, ActorLogging, Actor}

/**
 * Maintains a synchronized partitioned quota across a cluster.
 * @param onNextLocalQuota Called on local quota updates.
 */
class SharedQuotaActor(onNextLocalQuota: Int => Unit) extends Actor with ActorLogging {

  /**
   * Quota to be allocated across all nodes.
   */
  final val SharedQuota: Int = 10

  /**
   * Decorates the underlying actor system interface with clustering extensions.
   */
  val cluster = Cluster(context.system)

  /**
   * Members consuming a partition of the quota.
   */
  val quotaMembers = collection.mutable.Set.empty[Address]

  /**
   * Indicates whether this node can see the leader (i.e. is in the healthy cluster during partition).
   */
  var hasLeader = false

  /**
   * Indicates whether this node is the leader.
   */
  var isLeader = false

  /**
   * Actor 'pre-start' hook.
   */
  override def preStart(): Unit = {
    // Replays current state as events, allowing us to reuse 'receive' method rather than having to interpret the
    // current state as a snapshot.
    val initialStateMode = InitialStateAsEvents

    // Subscribe to cluster events.
    cluster.subscribe(
      self,
      initialStateMode,
      classOf[MemberUp],
      classOf[ReachableMember],
      classOf[UnreachableMember],
      classOf[MemberRemoved],
      classOf[LeaderChanged])
  }

  /**
   * Actor 'post-stop' hook.
   */
  override def postStop(): Unit =
    cluster.unsubscribe(self)

  /**
   * Maintains the 'quota members': the set of all reachable members that have not been removed from the system.
   */
  def receive = {
    case MemberUp(member) =>
      onMemberAcquiredQuota(member)

    case ReachableMember(member) =>
      onMemberAcquiredQuota(member)

    case UnreachableMember(member) =>
      onMemberReleasedQuota(member)

    case MemberRemoved(member, previousStatus) =>
      onMemberReleasedQuota(member)

    case LeaderChanged(Some(leader)) =>
      onLeaderSeen(leader == cluster.selfAddress)

    case LeaderChanged(None) =>
      onLeaderLost()
  }

  /**
   * Acquires a partition of the shared quota for the specified member.
   * @note Idempotent operation.
   * @param member Member who is now consuming part of the shared quota.
   */
  private def onMemberAcquiredQuota(member: Member): Unit =
    if (!quotaMembers.contains(member.address)) {
      quotaMembers += member.address
      publishLocalQuota()
    }

  /**
   * Relinquishes the specified member's partition of the shared quota.
   * @note Idempotent operation.
   * @param member Member who is no longer consuming any of the shared quota.
   */
  private def onMemberReleasedQuota(member: Member): Unit =
    if (quotaMembers.contains(member.address)) {
      quotaMembers -= member.address
      publishLocalQuota()
    }

  private def onLeaderSeen(isSelf: Boolean): Unit = {
    hasLeader = true
    isLeader = isSelf
    publishLocalQuota()
  }

  /**
   * Zeroes the local quota in response to becoming isolated from the leader. This prevents the shared quota from
   * doubling when clusters split.
   */
  private def onLeaderLost(): Unit = {
    hasLeader = false
    publishLocalQuota()
  }

  /**
   * Publishes the local quota to observers.
   */
  private def publishLocalQuota(): Unit =
    onNextLocalQuota(localQuota)

  /**
   * Determines the size of the local quota partition.
   * @return Local quota size.
   */
  private def localQuota: Int =
    if (hasLeader && quotaMembers.contains(cluster.selfAddress))
      if (quotaMembers.size == 1)
        SharedQuota
      else {
        val majoritySize = SharedQuota / quotaMembers.size
        if (isLeader)
          SharedQuota - (majoritySize * (quotaMembers.size - 1))
        else
          majoritySize
      }
    else
      0
}
