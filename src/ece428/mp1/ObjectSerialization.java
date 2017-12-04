package ece428.mp1;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectSerialization {

	private ConcurrentHashMap<NodeID, MembershipListEntry> listEntries;
	private String content;


	/**
	 * Constructs a serialization object based on a membership list. Marshals the membership list into a
	 * byte stream so it can be sent over the network.
	 *
	 * @param membershipList - The membership list that we want to encode.
	 */
	public ObjectSerialization(final MembershipList membershipList) {
		final StringBuilder builder = new StringBuilder();

		final Iterator it = membershipList.listEntries.entrySet().iterator();
		while (it.hasNext()) {
			final HashMap.Entry pair = (HashMap.Entry) it.next();
			final NodeID nodeID = (NodeID) pair.getKey();
			final MembershipListEntry entry = membershipList.listEntries.get(nodeID);

			builder
					.append(nodeID.getStartTime()).append(",")
					.append(nodeID.getIPAddress().getHostName()).append("|")
					.append(entry.getHeartBeatCounter()).append(",")
					.append(entry.getLocalTime()).append(",")
					.append(entry.getAlive()).append(",")
					.append(entry.getFailedTime()).append(",")
					.append(entry.isMaster()).append('`');

			this.content = builder.toString();
		}
	}


	/**
	 * Constructs a serialization object based on a string. Transforms the string into a
	 * membership list so we can use our methods and interact with it.
	 *
	 * @param content - Converts a string into a membership list.
	 * @throws IOException
	 */
	public ObjectSerialization(final String content) throws IOException {
		this.listEntries = new ConcurrentHashMap<NodeID, MembershipListEntry>();
		final String[] elements = content.split("`");

		for (final String str : elements) {
			if (str.trim().length() == 0) {
				break;
			}
			final String[] pair = str.split("\\|");
			final String nodeID = pair[0];
			final String entry = pair[1];

			final String[] nodeSplit = nodeID.split("\\,");
			final String[] entrySplit = entry.split("\\,");

			final String nodeStartTime = nodeSplit[0];
			final String IPAddress = nodeSplit[1];

			final String heartBeatCount = entrySplit[0];
			final String entryLocalTime = entrySplit[1];
			final String isAlive = entrySplit[2];
			final String failedTime = entrySplit[3];
			final String isMaster = entrySplit[4];

			final NodeID nodeIDKey = new NodeID(
					InetAddress.getByName(IPAddress),
					Long.parseLong(nodeStartTime)
			);
			final MembershipListEntry membershipListEntry = new MembershipListEntry(
					Integer.parseInt(heartBeatCount),
					Long.parseLong(entryLocalTime),
					Boolean.parseBoolean(isAlive),
					Long.parseLong(failedTime),
					Boolean.parseBoolean(isMaster)
			);

			this.listEntries.put(nodeIDKey, membershipListEntry);
		}

		final Iterator it = this.listEntries.entrySet().iterator();
		while (it.hasNext()) {
			final HashMap.Entry pair = (HashMap.Entry) it.next();
			final NodeID otherKey = (NodeID) pair.getKey();
			if (!this.listEntries.get(otherKey).getAlive()) {
				this.listEntries.remove(otherKey);
			}
		}
	}


	/**
	 * Gets the membership that we constructed.
	 *
	 * @return - The membership that's been converted from a byte stream.
	 */
	public MembershipList getMembershipList() {
		return new MembershipList(this.listEntries);
	}

	/**
	 * @return
	 */
	@Override
	public String toString() {
		return this.content;
	}
}