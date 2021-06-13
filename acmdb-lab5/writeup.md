I design a lock manager to control the transaction. 

A lock file stores three things: the page id it locked, the tids which own this lock and the lock type.

Here are the documents of the method I add.

Class Lock:

int getLockType(): return the type of this lock, shared or exclusive

PageId getLockpid(): return the pid of this lock locked

void upgrade(): set this lock as an exclusive lock

void downgrade(): set this lock as  a shared lock

boolean empty(): return true if no transactions own this lock

void addtid(TransactionId tid): add a tid to own this lock

void removetid(TransactionId tid): remove the tid which have not owned this lock

boolean contains(Transaction tid): return true if tid owns this lock

boolean onlyone(Transaction tid): return true if tid is the only transaction owns this lock

Class LockManager:

void acquireLock(): use the simple timeout policy, wait at most 1 second and if the transaction doesn't get the lock, then abort.

void releaseLock(TransactionId, PageId): release the lock 

void releaseAllLock(TransactionId tid): release all the lock which tid owns.

boolean holdsLock(TransactionId tid, PageId pid): return whether the tid owns the specific lock of pid.

ArrayList\<PageId\> holdsPage(TransactionId tid):  return all the page which the tid holds an exclusive lock of it.



I spend about two days to finish this lab. 