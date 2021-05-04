package simpledb;
import java.util.*;
import java.lang.Math.*;
/**
 * Knows how to compute some aggregate over a set of IntFields.
 */
public class IntegerAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private int gbfield;
    private Type gbfieldtype;
    private int afield;
    private int sum;
    private int cnt;
    private int ret;
    private Op what;
    private HashMap<Field, Integer> hashmap;
    private HashMap<Field, Integer> count;
    private TupleDesc td;

    /**
     * Aggregate constructor
     * 
     * @param gbfield
     *            the 0-based index of the group-by field in the tuple, or
     *            NO_GROUPING if there is no grouping
     * @param gbfieldtype
     *            the type of the group by field (e.g., Type.INT_TYPE), or null
     *            if there is no grouping
     * @param afield
     *            the 0-based index of the aggregate field in the tuple
     * @param what
     *            the aggregation operator
     */

    public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
        this.gbfield = gbfield;
        this.gbfieldtype = gbfieldtype;
        this.afield = afield;
        this.what = what;
        this.hashmap = new HashMap<Field, Integer>();
        this.count = new HashMap<Field, Integer>();
        this.td = null;
        this.sum = 0;
        this.cnt = 0;
        if (what == Op.MAX)
            this.ret = -2000000000;
        else if (what == Op.MIN)
            this.ret = 2000000000;
        else 
            this.ret = 0;
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     * 
     * @param tup
     *            the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        int val = ((IntField)tup.getField(afield)).getValue();
        this.sum = this.sum + val;
        this.cnt = this.cnt + 1;
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
            if (!hashmap.containsKey(f)) {
                if (what != Op.COUNT) 
                    hashmap.put(f, val);
                else 
                    hashmap.put(f, 1);
                count.put(f, 1);
            }
            else {
                int tmp = hashmap.get(f);
                if (what == Op.COUNT) 
                    hashmap.put(f, tmp + 1);
                else if (what == Op.MIN) 
                    hashmap.put(f, Math.min(tmp, val));
                else if (what == Op.MAX) 
                    hashmap.put(f, Math.max(tmp, val));
                else if (what == Op.SUM) 
                    hashmap.put(f, tmp + val);
                else if (what == Op.AVG)
                    hashmap.put(f, tmp + val);
                count.put(f, count.get(f) + 1);
            }
        }
        else {
            if (td == null) {
                Type[] type = new Type[1];
                String[] name = new String[1];
                type[0] =Type.INT_TYPE;
                name[0] = tup.getTupleDesc().getFieldName(afield);
                td = new TupleDesc(type, name);
            }
            if (what == Op.COUNT)
                ret = ret + 1;
            else if (what == Op.MIN)
                ret = Math.min(ret, val);
            else if (what == Op.MAX)
                ret = Math.max(ret, val);
            else if (what == Op.SUM)
                ret = ret + val;
            else if (what == Op.AVG)
                ret = sum / cnt;
        }
    }

    /**
     * Create a DbIterator over group aggregate results.
     * 
     * @return a DbIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */
    public DbIterator iterator() {
        // some code goes here
        ArrayList<Tuple> tplist = new ArrayList<Tuple>();
        if (gbfield != NO_GROUPING) {
            for (Field f: hashmap.keySet()) {
                Tuple tmp = new Tuple(td);
                tmp.setField(0, f);
                if (what == Op.AVG)
                    tmp.setField(1, new IntField(hashmap.get(f) / count.get(f)));
                else 
                    tmp.setField(1, new IntField(hashmap.get(f)));
                tplist.add(tmp);
            }
            
        }
        else {
            Tuple tmp = new Tuple(td);
            tmp.setField(0, new IntField(ret));
            tplist.add(tmp);
        }
        return new TupleIterator(this.td, tplist);
    }

}
