package simpledb;

import java.util.*;

/**
 * The Join operator implements the relational join operation.
 */
public class HashEquiJoin extends Operator {

    private static final long serialVersionUID = 1L;
    private JoinPredicate p;
    private DbIterator child1;
    private DbIterator child2;
    private TupleDesc merge;
    private ArrayList<Tuple> store;
    private Iterator<Tuple> it;

    /**
     * Constructor. Accepts to children to join and the predicate to join them
     * on
     * 
     * @param p
     *            The predicate to use to join the children
     * @param child1
     *            Iterator for the left(outer) relation to join
     * @param child2
     *            Iterator for the right(inner) relation to join
     */
    public HashEquiJoin(JoinPredicate p, DbIterator child1, DbIterator child2) {
        // some code goes here
        this.p = p;
        this.child1 = child1;
        this.child2 = child2;
        this.merge = TupleDesc.merge(child1.getTupleDesc(), child2.getTupleDesc());
        this.store = new ArrayList<Tuple>();
        this.it = null;
    }

    public JoinPredicate getJoinPredicate() {
        // some code goes here
        return this.p;
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return merge;
    }
    
    public String getJoinField1Name()
    {
        // some code goes here
	    return this.child1.getTupleDesc().getFieldName(p.getField1());
    }

    public String getJoinField2Name()
    {
        // some code goes here
        return this.child2.getTupleDesc().getFieldName(p.getField2());
    }
    
    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
            super.open();
            this.child1.open();
            this.child2.open();
            int index1 = this.p.getField1();
            int index2 = this.p.getField2();
            int len1 = this.child1.getTupleDesc().numFields();
            int len2 = this.child2.getTupleDesc().numFields();
            Tuple tmp1 = null;
            Tuple tmp2 = null;
            while (child1.hasNext()) {
                tmp1 = child1.next();
                child2.rewind();
                Field f1 = tmp1.getField(index1);
                while (child2.hasNext()) {
                    tmp2 = child2.next();
                    Field f2 = tmp2.getField(index2);
                    if (f1.compare(this.p.getOperator(), f2)) {
                        Tuple tmp = new Tuple(merge);
                        for (int i = 0; i < len1; i++) 
                            tmp.setField(i, tmp1.getField(i));
                        for (int i = 0; i < len2; i++)
                            tmp.setField(i + len1, tmp2.getField(i));
                        this.store.add(tmp);
                    }
                }
            }
            this.it = this.store.iterator();
        // some code goes here
    }

    public void close() {
        // some code goes here
        this.store.clear();
        this.child1.close();
        this.child2.close();
        super.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        this.close();
        this.open();
    }

    transient Iterator<Tuple> listIt = null;

    /**
     * Returns the next tuple generated by the join, or null if there are no
     * more tuples. Logically, this is the next tuple in r1 cross r2 that
     * satisfies the join predicate. There are many possible implementations;
     * the simplest is a nested loops join.
     * <p>
     * Note that the tuples returned from this particular implementation of Join
     * are simply the concatenation of joining tuples from the left and right
     * relation. Therefore, there will be two copies of the join attribute in
     * the results. (Removing such duplicate columns can be done with an
     * additional projection operator if needed.)
     * <p>
     * For example, if one tuple is {1,2,3} and the other tuple is {1,5,6},
     * joined on equality of the first column, then this returns {1,2,3,1,5,6}.
     * 
     * @return The next matching tuple.
     * @see JoinPredicate#filter
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        if (this.it.hasNext())
            return this.it.next();
        return null;
    }

    @Override
    public DbIterator[] getChildren() {
        // some code goes here
        DbIterator[] ret = new DbIterator[2];
        ret[0] = this.child1;
        ret[1] = this.child2;
        return ret;
    }

    @Override
    public void setChildren(DbIterator[] children) {
        // some code goes here
        this.child1 = children[0];
        this.child2 = children[1];
    }
    
}
