package simpledb;

import java.io.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool checks that the transaction has the appropriate
 * locks to read/write the page.
 * 
 * @Threadsafe, all fields are final
 */
public class BufferPool {
    /** Bytes per page, including header. */
    private static final int PAGE_SIZE = 4096;

    private static int pageSize = PAGE_SIZE;
    
    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;

    private  final int numPages;
    private final Page[] pageArray;
    private final HashMap<PageId, Integer> pageIndexHashMap;
    private final LinkedList<PageId> FIFO;
    private final Queue<Integer> queue;
    private final LockManager Lock_Manage = new LockManager();

    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
        // some code goes here
        this.numPages = numPages;
        this.pageArray = new Page[numPages];
        this.pageIndexHashMap = new HashMap<PageId, Integer>();
        this.FIFO = new LinkedList<PageId>();
        this.queue = new LinkedList<Integer>();
        for (int i = 0; i < numPages; i++)
            queue.add(i);
    }
    
    public static int getPageSize() {
      return pageSize;
    }

    // THIS FUNCTION SHOULD ONLY BE USED FOR TESTING!!
    public static void setPageSize(int pageSize) {
        BufferPool.pageSize = pageSize;
    }
    
    // THIS FUNCTION SHOULD ONLY BE USED FOR TESTING!!
    public static void resetPageSize() {
        BufferPool.pageSize = PAGE_SIZE;
    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid the ID of the transaction requesting the page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public Page getPage(TransactionId tid, PageId pid, Permissions perm)
        throws TransactionAbortedException, DbException {
        // some code goes here

        if (perm == Permissions.READ_ONLY) {
            Lock_Manage.acquireLock(tid, pid, 0);
        }
        else if (perm == Permissions.READ_WRITE) {
            Lock_Manage.acquireLock(tid, pid, 1);
        }
        else 
            throw new DbException("Perm not true");

        synchronized (this) {
            Page targetPage;
            if (!this.pageIndexHashMap.containsKey(pid))
            {
                targetPage = Database.getCatalog().getDatabaseFile(pid.getTableId()).readPage(pid);
                while (queue.size() == 0)
                    this.evictPage();
                int index = queue.poll();
                this.pageIndexHashMap.put(pid, index);
                this.FIFO.addLast(pid);
                this.pageArray[index] = targetPage;
            }
            else
            {
                int pageArrayId = this.pageIndexHashMap.get(pid);
                targetPage = this.pageArray[pageArrayId];
            }

            return targetPage;
        }
    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param pid the ID of the page to unlock
     */
    public  void releasePage(TransactionId tid, PageId pid) {
        // some code goes here
        // not necessary for lab1|lab2
        Lock_Manage.releaseLock(tid, pid);
    }

    /**
     * Release all locks associated with a given transaction.
     *
     * @param tid the ID of the transaction requesting the unlock
     */
    public void transactionComplete(TransactionId tid) throws IOException {
        // some code goes here
        // not necessary for lab1|lab2
        transactionComplete(tid, true);
    }

    /** Return true if the specified transaction has a lock on the specified page */
    public boolean holdsLock(TransactionId tid, PageId p) {
        // some code goes here
        // not necessary for lab1|lab2
        return Lock_Manage.holdsLock(tid, p);
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public synchronized  void transactionComplete(TransactionId tid, boolean commit)
        throws IOException {
        // some code goes here
        // not necessary for lab1|lab2
        if (commit == true) {
            this.flushPages(tid);
        }
        else {
            if (this.Lock_Manage.holdsLock(tid)) {
                ArrayList<PageId> pages = this.Lock_Manage.holdsPage(tid);
                for (PageId pid: pages) 
                    this.discardPage(pid);
            }
        }
        this.Lock_Manage.releaseAllLock(tid);
    }

    /**
     * Add a tuple to the specified table on behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to and any other 
     * pages that are updated (Lock acquisition is not needed for lab2). 
     * May block if the lock(s) cannot be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and adds versions of any pages that have 
     * been dirtied to the cache (replacing any existing versions of those pages) so 
     * that future requests see up-to-date pages. 
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public void insertTuple(TransactionId tid, int tableId, Tuple t)
        throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        DbFile file = Database.getCatalog().getDatabaseFile(tableId);
        ArrayList<Page> dirtyPageList = file.insertTuple(tid, t);
        for (Page dirtyPage : dirtyPageList) {
            dirtyPage.markDirty(true, tid);
            PageId pid = dirtyPage.getId();
            if (pageIndexHashMap.containsKey(pid)) {
                int index = pageIndexHashMap.get(pid);
                this.pageArray[index] = dirtyPage;
            }
            else {
                while (queue.size() == 0)
                    this.evictPage();
                int index = queue.poll();
                this.pageIndexHashMap.put(pid, index);
                this.FIFO.addLast(pid);
                this.pageArray[index] = dirtyPage;
            }
        }
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from and any
     * other pages that are updated. May block if the lock(s) cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and adds versions of any pages that have 
     * been dirtied to the cache (replacing any existing versions of those pages) so 
     * that future requests see up-to-date pages. 
     *
     * @param tid the transaction deleting the tuple.
     * @param t the tuple to delete
     */
    public void deleteTuple(TransactionId tid, Tuple t)
        throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        DbFile file = Database.getCatalog().getDatabaseFile(t.getRecordId().getPageId().getTableId());
        ArrayList<Page> dirtyPageList = file.deleteTuple(tid, t);
        for (Page dirtyPage : dirtyPageList) {
            dirtyPage.markDirty(true, tid);
            PageId pid = dirtyPage.getId();
            if (pageIndexHashMap.containsKey(pid)) {
                int index = pageIndexHashMap.get(pid);
                this.pageArray[index] = dirtyPage;
            }
            else {
                while (queue.size() == 0)
                    this.evictPage();
                int index = queue.poll();
                this.pageIndexHashMap.put(pid, index);
                this.FIFO.addLast(pid);
                this.pageArray[index] = dirtyPage;
            }
        }

    }
    

    /**
     * Flush all dirty pages to disk.
     * NB: Be careful using this routine -- it writes dirty data to disk so will
     *     break simpledb if running in NO STEAL mode.
     */
    public synchronized void flushAllPages() throws IOException {
        // some code goes here
        // not necessary for lab1
        for (PageId pid : pageIndexHashMap.keySet())
        {
            flushPage(pid);
        }
    }

    /** Remove the specific page id from the buffer pool.
        Needed by the recovery manager to ensure that the
        buffer pool doesn't keep a rolled back page in its
        cache.
        
        Also used by B+ tree files to ensure that deleted pages
        are removed from the cache so they can be reused safely
    */
    public synchronized void discardPage(PageId pid) {
        // some code goes here
        // not necessary for lab1
        if (pageIndexHashMap.containsKey(pid))
        {
            this.FIFO.remove(pid);
            this.queue.add(pageIndexHashMap.get(pid));
            this.pageIndexHashMap.remove(pid);

        }
    }

    /**
     * Flushes a certain page to disk
     * @param pid an ID indicating the page to flush
     */
    private synchronized  void flushPage(PageId pid) throws IOException {
        // some code goes here
        // not necessary for lab1
        if (this.pageIndexHashMap.containsKey(pid))
        {
            int pageArrayIndex = pageIndexHashMap.get(pid);
            Database.getCatalog().getDatabaseFile(pid.getTableId()).writePage(pageArray[pageArrayIndex]);
        }
    }

    /** Write all pages of the specified transaction to disk.
     */
    public synchronized  void flushPages(TransactionId tid) throws IOException {
        // some code goes here
        // not necessary for lab1|lab2
        ArrayList<PageId> pages = this.Lock_Manage.holdsPage(tid);
        for (PageId pid: pages) {
            this.flushPage(pid);
        }
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized  void evictPage() throws DbException {
        // some code goes here
        // not necessary for lab1
        PageId evictPid = null;
        ArrayList<PageId> A = new ArrayList<PageId>();
        int pageArrayId = 0;
        while (!FIFO.isEmpty()) {
            evictPid = FIFO.pollFirst();  
            try {
                pageArrayId = pageIndexHashMap.get(evictPid);
            }
            catch (Exception e) {
                throw new DbException("all dirty pages!");
            }
            if (this.pageArray[pageArrayId].isDirty() == null)
                break;
            A.add(evictPid);
        }

        if (evictPid == null)
            throw new DbException("all dirty pages in BufferPool!");

        for (PageId p: A) 
            FIFO.addLast(p);
        
        try  {
            flushPage(evictPid);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException happens in evictPage()\n");
        }
        this.pageIndexHashMap.remove(evictPid);
        this.queue.add(pageArrayId);
    }

}