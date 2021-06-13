package simpledb;

import java.util.*;

public class LockManager {
	private Hashtable<PageId, Lock> pidtolock;
	private Hashtable<TransactionId, HashSet<Lock>> tidtolock;
	public static final Random rg = new Random();
	

	public LockManager() {
		this.tidtolock = new Hashtable<TransactionId, HashSet<Lock>>();
        this.pidtolock = new Hashtable<PageId, Lock>();
	}

	public synchronized void acquireLock(TransactionId tid, PageId pid, int type) throws TransactionAbortedException {
		Lock tmplock;
		if (type == 0) {   // shared lock
			if (!pidtolock.keySet().contains(pid)) {
				tmplock = new Lock(type, pid);
				tmplock.addtid(tid);
				pidtolock.put(pid, tmplock);
				if (tidtolock.keySet().contains(tid)) 
					tidtolock.get(tid).add(tmplock);
				else {
					HashSet<Lock> H = new HashSet<Lock>();
					H.add(tmplock);
					tidtolock.put(tid, H);
				}
			}
			else {
				tmplock = pidtolock.get(pid);
				if (tmplock.getLockType() == 0) {
					tmplock.addtid(tid);
					if (tidtolock.keySet().contains(tid))
						tidtolock.get(tid).add(tmplock);
					else {
						HashSet<Lock> H = new HashSet<Lock>();
						H.add(tmplock);
						tidtolock.put(tid, H);
					}	
				}
				else {
					long startTime = System.currentTimeMillis();
					while (pidtolock.keySet().contains(pid) && pidtolock.get(pid).getLockType() == 1) {
						Lock lock = pidtolock.get(pid);
						if (lock.onlyone(tid)) {
							//lock.downgrade();
							break;
						}
						long waittime = 500 + rg.nextInt(500);
						if (System.currentTimeMillis() - startTime > waittime) {
							throw new TransactionAbortedException();
						}
						try {
							wait(50);
						}
						catch (Exception e) {
							throw new TransactionAbortedException();
						}
					}
					if (!pidtolock.keySet().contains(pid)) {
						tmplock = new Lock(type, pid);
						tmplock.addtid(tid);
						pidtolock.put(pid, tmplock);
						if (tidtolock.keySet().contains(tid)) 
							tidtolock.get(tid).add(tmplock);
						else {
							HashSet<Lock> H = new HashSet<Lock>();
							H.add(tmplock);
							tidtolock.put(tid, H);
						}
					}
					else {
						tmplock = pidtolock.get(pid);
						tmplock.addtid(tid);
						pidtolock.put(pid, tmplock);
						if (tidtolock.keySet().contains(tid)) 
							tidtolock.get(tid).add(tmplock);
						else {
							HashSet<Lock> H = new HashSet<Lock>();
							H.add(tmplock);
							tidtolock.put(tid, H);
				        }
					}
				}
			}
		}
		else {        // exclusive lock
			if (!pidtolock.keySet().contains(pid)) {
				//assert(false);
				tmplock = new Lock(type, pid);
				tmplock.addtid(tid);
				pidtolock.put(pid, tmplock);
				if (tidtolock.keySet().contains(tid)) 
					tidtolock.get(tid).add(tmplock);
				else {
					HashSet<Lock> H = new HashSet<Lock>();
					H.add(tmplock);
					tidtolock.put(tid, H);
				}
			}
			else {
				long startTime = System.currentTimeMillis();
				long waittime = 500 + rg.nextInt(500);
				while (pidtolock.keySet().contains(pid)) {	
					Lock lock = pidtolock.get(pid);
				//	assert(!lock.empty());
					if (lock.onlyone(tid)) {
						lock.upgrade();
						//assert(false);
						return;
					}
					if (System.currentTimeMillis() - startTime > waittime) {
						throw new TransactionAbortedException();
					}
					try {
						wait(50);
					}
					catch (Exception e) {
						throw new TransactionAbortedException();
					}
				}
				tmplock = new Lock(type, pid);
				tmplock.addtid(tid);
				pidtolock.put(pid, tmplock);
				if (tidtolock.keySet().contains(tid)) 
					tidtolock.get(tid).add(tmplock);
				else {
					HashSet<Lock> H = new HashSet<Lock>();
					H.add(tmplock);
					tidtolock.put(tid, H);
				}
			}
		}
	}

	public synchronized void releaseLock(TransactionId tid, PageId pid) {
		if (this.tidtolock.keySet().contains(tid) && this.pidtolock.keySet().contains(pid)) {
			Lock tmplock = this.pidtolock.get(pid);
			this.tidtolock.get(tid).remove(tmplock);
			tmplock.removetid(tid);
			if (tmplock.empty()) 
				this.pidtolock.remove(pid);
		}
		notifyAll();
	}

	public synchronized void releaseAllLock(TransactionId tid) {
		if (tidtolock.keySet().contains(tid)) {
	        HashSet<Lock> H = tidtolock.get(tid);
	        ArrayList<Lock> A = new ArrayList<Lock>();
	        for (Lock lock: H) 
	        	A.add(lock);
	        for (Lock lock: A) 
	        	releaseLock(tid, lock.getLockPid());
	        notifyAll();
		}
	}

	public synchronized boolean holdsLock(TransactionId tid, PageId pid) {
		if (!this.pidtolock.keySet().contains(pid))
			return false;
		if (!this.tidtolock.keySet().contains(tid)) 
			return false;
		Lock lock = this.pidtolock.get(pid);
		return lock.contains(tid);
	}

	public synchronized boolean holdsLock(TransactionId tid) {
		return this.tidtolock.keySet().contains(tid);
	}

	public synchronized ArrayList<PageId> holdsPage(TransactionId tid) {
		ArrayList<PageId> tmp = new ArrayList<PageId>();
		if (!this.tidtolock.keySet().contains(tid))
			return tmp;
		HashSet<Lock> H = tidtolock.get(tid);
		for (Lock lock: H) {
			if (lock.getLockType() == 1)
				tmp.add(lock.getLockPid());
		}
		return tmp; 
	}
}