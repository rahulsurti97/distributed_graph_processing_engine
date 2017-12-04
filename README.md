# ECE428 - MP4

This is our implementation of a distributed graph processing engine.
### How to Start
- Clone the repo using `git clone`
- `cd` into the scripts folder
- You can run the server by typing `./update.sh`

### Description
This program will create a network of nodes and handle failure detectors. The failure detection will be spread across all nodes in the network quickly and everyone _should_ be updated quickly on the status of the entire network using the gossiping algorithm and membership lists.

This system also implements a distributed file system which can handle up to 2 failures. Replication is done to ensure files are reachable even if nodes fail and we have a group of masters to ensure that coordination can still be done even if master nodes fail.

The system has a distributed graph processing engine which can take in general graphs and process some computation on them. For example, you can run:

- `sssp <filename> <source_vertex>`
- `pagerank <filename>`

The first example runs single-source shortest path on a graph, and the second command runs the pagerank algorithm.

## Authors
- Rahul Surti
- Shashank Saxena
