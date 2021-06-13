package simpledb;

import java.util.*;

public class Lock {
	private int type; // 0 for shared 1 for exclusive
	private PageId pid; // the pid to be locked
	private Set<TransactionId> havelock;

	public Lock(int type, PageId pid) {
		this.type = type;
		this.pid = pid;
		this.havelock = new HashSet<TransactionId>();
	}

	public int getLockType() {
		return this.type;
	}

	public PageId getLockPid() {
		return this.pid;
	}

	public void upgrade() {
		this.type = 1;
	}

	public void downgrade() {
		this.type = 0;
	}

	public boolean empty() {
		return havelock.isEmpty();
	}

	public void addtid(TransactionId tid) {
		havelock.add(tid);
	}

	public void removetid(TransactionId tid) {
		havelock.remove(tid);
	}

	public boolean contains(TransactionId tid) {
		return havelock.contains(tid);
	}

	public boolean onlyone(TransactionId tid) {
		return (this.havelock.size() == 1 && this.havelock.contains(tid)); 
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Lock l = (Lock) o;
		return l.type == this.type && l.pid.equals(this.pid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, pid);
	}

}