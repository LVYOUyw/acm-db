package simpledb;
import java.util.*;


/**
 * Knows how to compute some aggregate over a set of StringFields.
 */

public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private int gbfield;
    private Type gbfieldtype;
    private int afield;
    private Op what;
    private HashMap<Field, Integer> hashmap;
    private TupleDesc td;
    private int cnt;

    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
        this.gbfield = gbfield;
        this.gbfieldtype = gbfieldtype;
        this.afield = afield;
        this.what = what;
        this.td = null;
        this.cnt = 0;
        this.hashmap = new HashMap<Field, Integer>();
        if (what != Op.COUNT) 
            throw new IllegalArgumentException("what != COUNT");
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        if (gbfield != NO_GROUPING) {
            Field f = tup.getField(gbfield);
            if (td == null) {
                Type[] type = new Type[2];
                String[] name = new String[2];
                type[0] = gbfieldtype;
                type[1] =Type.INT_TYPE;
                name[0] = tup.getTupleDesc().getFieldName(gbfield);
                name[1] = tup.getTupleDesc().getFieldName(afield);
                td = new TupleDesc(type, name);
            }
            if (!hashmap.containsKey(f))
                hashmap.put(f, 1);
            else 
                hashmap.put(f, hashmap.get(f) + 1);
        }
        else {
            if (td == null) {
                Type[] type = new Type[1];
                String[] name = new String[1];
                type[0] = Type.INT_TYPE;
                name[0] = tup.getTupleDesc().getFieldName(afield);
                td = new TupleDesc(type, name);
                this.cnt = this.cnt + 1;
            }
        }
    }

    /**
     * Create a DbIterator over group aggregate results.
     *
     * @return a DbIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    public DbIterator iterator() {
        // some code goes here
        ArrayList<Tuple> ret = new ArrayList<Tuple>();
        if (gbfield != NO_GROUPING) {
            for (Field f: hashmap.keySet()) {
                Tuple tmp = new Tuple(td);
                tmp.setField(0, f);
                tmp.setField(1, new IntField(hashmap.get(f)));
                ret.add(tmp);
            }
        }
        else {
            Tuple tmp = new Tuple(td);
            tmp.setField(0, new IntField(this.cnt));
            ret.add(tmp);
        }
        return new TupleIterator(td, ret);
    }

}
