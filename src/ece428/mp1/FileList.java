package ece428.mp1;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class FileList {

	protected final ConcurrentHashMap<String, HashSet<InetAddress>> fileHashMap;

	public FileList() {
		this.fileHashMap = new ConcurrentHashMap<>();
	}


	public synchronized void addFileEntry(final String fileName, final InetAddress inetAddress) {
		if (this.fileHashMap.containsKey(fileName)) {
			this.fileHashMap.get(fileName).add(inetAddress);
		} else {
			final HashSet<InetAddress> hashSet = new HashSet<>();
			hashSet.add(inetAddress);
			this.fileHashMap.put(fileName, hashSet);
		}
	}


	public synchronized boolean containsNode(final InetAddress inetAddress) {
		return this.fileHashMap.containsValue(inetAddress);
	}


	public synchronized boolean containsFile(final String file) {
		return this.fileHashMap.containsKey(file);
	}


	public synchronized void clear() {
		this.fileHashMap.clear();
	}
}
