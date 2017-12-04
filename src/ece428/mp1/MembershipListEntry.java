package ece428.mp1;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class MembershipListEntry {
    private boolean isMaster;
    private long failedTime;
    private int heartBeatCounter;
    private long localTime;
    private boolean isAlive;

    public MembershipListEntry() {
        this.heartBeatCounter = 0;
        this.localTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.failedTime = -1;
        this.isAlive = true;
        this.isMaster = false;
    }

    public MembershipListEntry(final int heartBeatCounter) {
        this.heartBeatCounter = heartBeatCounter;
        this.localTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.failedTime = -1;
        this.isAlive = true;
        this.isMaster = false;
    }

    public MembershipListEntry(final int heartBeatCounter, final boolean isMaster) {
        this.heartBeatCounter = heartBeatCounter;
        this.localTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.failedTime = -1;
        this.isAlive = true;
        this.isMaster = isMaster;
    }

    public MembershipListEntry(final int heartBeatCounter, final long localTime, final boolean isAlive, final long failedTime, final boolean isMaster) {
        this.heartBeatCounter = heartBeatCounter;
        this.localTime = localTime;
        this.isAlive = isAlive;
        this.isMaster = isMaster;
    }

    public boolean isMaster() {
        return this.isMaster;
    }

    public void setMaster(final boolean master) {
        this.isMaster = master;
    }

    public synchronized long getFailedTime() {
        return this.failedTime;
    }

    /**
     * Gets the heartbeat counter for this node.
     *
     * @return
     */
    public int getHeartBeatCounter() {
        return this.heartBeatCounter;
    }

    /**
     * Sets the heartbeat counter for this node.
     *
     * @param heartBeatCounter - The heartbeat counter.
     */
    public void setHeartBeatCounter(final int heartBeatCounter) {
        this.heartBeatCounter = heartBeatCounter;
    }

    /**
     * Gets the local time for this entry.
     *
     * @return Local time.
     */
    public long getLocalTime() {
        return this.localTime;
    }

    /**
     * Sets the local time for this entry.
     *
     * @param localTime - The local time.
     */
    public void setLocalTime(final long localTime) {
        this.localTime = localTime;
    }

    /**
     * Updates the local time to the current time since epoch in milliseconds.
     */
    public void updateLocalTime() {
        this.localTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Checks whether or not the node is alive.
     *
     * @return Boolean indicating if the node is alive or not.
     */
    public boolean getAlive() {
        return this.isAlive;
    }

    /**
     * Sets the living status of a node.
     *
     * @param alive - Boolean indicating status.
     */
    public void setAlive(final boolean alive) {
        this.isAlive = alive;
    }
}
