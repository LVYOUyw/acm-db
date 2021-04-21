package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    private File f;
    private TupleDesc td;

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.f = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return this.f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
       return this.f.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        byte[] data = new byte[BufferPool.getPageSize()];
        Page ret = null;
        try {
            RandomAccessFile RAF = new RandomAccessFile(this.f, "r");
            RAF.seek(pid.pageNumber() * BufferPool.getPageSize());
            RAF.read(data, 0, BufferPool.getPageSize());
            ret = new HeapPage((HeapPageId)pid, data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return ret;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return (int)Math.ceil((double)this.f.length() / BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    private class HeapIterator implements DbFileIterator {
        private int pagepos = 0;
        private Iterator<Tuple> pageiterator;
        private TransactionId tid;

        public HeapIterator(TransactionId tid) {
            this.tid = tid;
        }

        @Override 
        public void open() throws DbException, TransactionAbortedException {
            if (tid == null) 
                throw new TransactionAbortedException();
            this.pagepos = 0;
            HeapPage page = (HeapPage)Database.getBufferPool().getPage(this.tid, new HeapPageId(getId(), pagepos), Permissions.READ_ONLY);
            this.pageiterator = page.iterator();

        }

        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException{
            if (pageiterator == null) 
                return false;
            if (tid == null) 
                throw new TransactionAbortedException();
            if (pageiterator.hasNext()) 
                return true;
            int num = numPages();
            int cnt = pagepos + 1;
            while (cnt < num) {
                HeapPage page = (HeapPage)Database.getBufferPool().getPage(this.tid, new HeapPageId(getId(), cnt), Permissions.READ_ONLY);
                Iterator<Tuple> iterator = page.iterator();
                if (iterator.hasNext()) 
                    return true;
                cnt++;
            }
            return false;
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if (!hasNext()) 
                throw new NoSuchElementException();
            if (pageiterator.hasNext())
                return pageiterator.next();
            int num = numPages();
            pagepos++;
            while (pagepos < num) {
                HeapPage page = (HeapPage)Database.getBufferPool().getPage(this.tid, new HeapPageId(getId(), pagepos), Permissions.READ_ONLY);
                pageiterator = page.iterator();
                if (pageiterator.hasNext()) 
                    return pageiterator.next();
                pagepos++;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            open();
        }

        @Override
        public void close() {
            pagepos = 0;
            pageiterator = null;
            tid = null;
        }

    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new HeapIterator(tid);
    }

}

