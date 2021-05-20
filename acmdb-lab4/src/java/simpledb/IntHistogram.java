package simpledb;
import static java.lang.Math.min;
import static java.lang.Math.max;
/** A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {

    private int buckets;
    private int minv = 1000000;
    private int maxv = -1000000;
    private int[] cnt;
    private int len;
    private int totalnum;
    /**
     * Create a new IntHistogram.
     * 
     * This IntHistogram should maintain a histogram of integer values that it receives.
     * It should split the histogram into "buckets" buckets.
     * 
     * The values that are being histogrammed will be provided one-at-a-time through the "addValue()" function.
     * 
     * Your implementation should use space and have execution time that are both
     * constant with respect to the number of values being histogrammed.  For example, you shouldn't 
     * simply store every value that you see in a sorted list.
     * 
     * @param buckets The number of buckets to split the input value into.
     * @param min The minimum integer value that will ever be passed to this class for histogramming
     * @param max The maximum integer value that will ever be passed to this class for histogramming
     */
    public IntHistogram(int buckets, int min, int max) {
    	// some code goes here
        this.buckets = buckets;
        this.minv = min;
        this.maxv = max;
        cnt = new int[buckets];
        len = max((maxv - minv + 1) / buckets, 1); 
    }

    public IntHistogram(int buckets) {
        this.buckets = buckets;
    }

    public void setminv(int v) {
        this.minv = min(minv, v);
    }

    public void setmaxv(int v) {
        this.maxv = max(maxv, v);
    }

    public void load() {
        cnt = new int[buckets];
        len = max((maxv - minv + 1) / buckets, 1); 
    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
    	// some code goes here
        int index = min((v - minv) / len, buckets - 1);
        cnt[index]++;
        totalnum++;
    }

    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     * 
     * For example, if "op" is "GREATER_THAN" and "v" is 5, 
     * return your estimate of the fraction of elements that are greater than 5.
     * 
     * @param op Operator
     * @param v Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, int v) {

    	// some code goes here
        int index = min((v - minv) / len, buckets - 1);
        String ops = op.toString();
        if (ops.equals("=")) {
            if (v > maxv || v < minv)
                return 0;
            return (double)cnt[index] / getLen(index) / totalnum;
        }
        else if (ops.equals(">")) {
            if (v > maxv ) 
                return 0;
            if (v < minv)
                return 1;
            double ret = (double)(getRight(index) - v) * cnt[index] / getLen(index) / totalnum;
            for (int i = index + 1; i < buckets; i++)
                ret += (double)cnt[i] / getLen(i) / totalnum;
            return ret;

        }
        else if (ops.equals("<")) {
            if (v > maxv)
                return 1;
            if (v < minv)
                return 0;
            double ret = (double)(v - getLeft(index)) * cnt[index] / getLen(index) / totalnum;
            for (int i = index - 1; i >= 0; i--)
                ret += (double)cnt[i] / getLen(i) / totalnum;
            return ret;
        }
        else if (ops.equals("<=")) {
            if (v > maxv)
                return 1;
            if (v < minv)
                return 0;
            double ret = (double)(v - getLeft(index) + 1) * cnt[index] / getLen(index) / totalnum;
            for (int i = index - 1; i >= 0; i--)
                ret += (double)cnt[i] / getLen(i) / totalnum;
            return ret;
        }
        else if (ops.equals(">=")) {
            if (v > maxv ) 
                return 0;
            if (v < minv)
                return 1;
            double ret = (double)(getRight(index) - v + 1) * cnt[index] / getLen(index) / totalnum;
            for (int i = index + 1; i < buckets; i++)
                ret += (double)cnt[i] / getLen(i) / totalnum;
            return ret;
        }
        else if (ops.equals("<>")) {
            if (v > maxv || v < minv)
                return 1;
            return 1.0 - (double)cnt[index] / getLen(index) / totalnum;
        }
        return -1.0;
    }
    
    /**
     * @return
     *     the average selectivity of this histogram.
     *     
     *     This is not an indispensable method to implement the basic
     *     join optimization. It may be needed if you want to
     *     implement a more efficient optimization
     * */
    public double avgSelectivity()
    {
        // some code goes here
        return 1.0;
    }
    
    /**
     * @return A string describing this histogram, for debugging purposes
     */
    public String toString() {
        // some code goes here
        StringBuilder s  = new StringBuilder();
        for (int i = 0; i < buckets; i++)
            s.append(cnt[i]).append(' ');
        s.append("\n");
        return s.toString();
    }

    public int getLeft(int i) {
        return minv + i * len;
    }

    public int getRight(int i) {
        return i == buckets - 1 ? maxv : minv + (i + 1) * len - 1;
    }

    public int getLen(int i) {
        return i == buckets - 1 ? maxv - getLeft(i) + 1 : len;
    }
}
