Akka Distributed Throttling
===========================

Throttles an action performed by a P2P system using a synchronized partitioned quota.

Approach
--------

1.  Identify the maximum number of times the action can be performed across the system (per second). This is the
    `shared quota`.

2.  Each node creates its own `ActorSystem` and joins the same [Akka Cluster][akka-cluster].

3.  Akka's `auto-down` feature is left `disabled` to ensure there is only one `leader` in the event of network partitions.

4.  Nodes receive leave/join events and adjust their `local quota` accordingly.

5.  The `leader` node is responsible for handling the remainder when the `shared quota` does not evenly divide.

6.  Nodes that cannot see the `leader` set their `local quota` to `0`.

[akka-cluster]: http://doc.akka.io/docs/akka/snapshot/scala/cluster-usage.html "Akka Cluster"
