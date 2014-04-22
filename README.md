Akka Distributed Throttling
===========================

>   Throttles an action performed by a P2P system using a synchronized partitioned quota.

This project demonstrates how to use [Akka Clustering][akka-cluster-theory] to synchronize `Int` values between distributed
nodes that `sum` to a predefined value. These `Int` values can be used for local throttles to ensure an aggregate throttle
equal to the `sum`.

This is useful in the context of throttling a specific action performed by a distributed system, such as requests to a
legacy web service, without introducing a single point of failure or active/passive redundancy.

Approach
--------

1.  Identify the maximum number of times the action can be performed across the system (per second). This is the
    `shared quota`.

2.  Each node creates its own `ActorSystem` and joins the same [Akka Cluster][akka-cluster].

3.  Akka's `auto-down` feature is left `disabled` to ensure there is only one `leader` in the event of network partitions.

4.  Nodes receive leave/join events and adjust their `local quota` accordingly.

5.  The `leader` node is responsible for handling the remainder when the `shared quota` does not evenly divide.

6.  Nodes that cannot see the `leader` set their `local quota` to `0`.

Eventual consistency
--------------------

Synchronization of the `shared quota` is eventually consistent. Todo: determine consistency SLA. Consider first seed node,
timeouts from failure-detector, and perhaps latency between receiving CRDT payloads? The latter may be covered by
failure-detector.

[akka-cluster-theory]: http://doc.akka.io/docs/akka/snapshot/common/cluster.html#cluster "Akka Clustering Theory"
[akka-cluster]: http://doc.akka.io/docs/akka/snapshot/scala/cluster-usage.html "Akka Cluster"

