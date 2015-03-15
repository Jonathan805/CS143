package simpledb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TableStats represents statistics (e.g., histograms) about base tables in a
 * query. 
 * 
 * This class is not needed in implementing lab1 and lab2.
 */
public class TableStats {
    private int tableid;
    private int cost;
    private HeapFile hF;
    private int min[];
    private int max[];
    private int size;
    private int tupnum;
    private IntHistogram iHist[];
    private StringHistogram jHist[];
    private static final ConcurrentHashMap<String, TableStats> statsMap = new ConcurrentHashMap<String, TableStats>();

    static final int IOCOSTPERPAGE = 1000;

    public static TableStats getTableStats(String tablename) {
        return statsMap.get(tablename);
    }

    public static void setTableStats(String tablename, TableStats stats) {
        statsMap.put(tablename, stats);
    }
    
    public static void setStatsMap(HashMap<String,TableStats> s)
    {
        try {
            java.lang.reflect.Field statsMapF = TableStats.class.getDeclaredField("statsMap");
            statsMapF.setAccessible(true);
            statsMapF.set(null, s);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public static Map<String, TableStats> getStatsMap() {
        return statsMap;
    }

    public static void computeStatistics() {
        Iterator<Integer> tableIt = Database.getCatalog().tableIdIterator();

        System.out.println("Computing table stats.");
        while (tableIt.hasNext()) {
            int tableid = tableIt.next();
            TableStats s = new TableStats(tableid, IOCOSTPERPAGE);
            setTableStats(Database.getCatalog().getTableName(tableid), s);
        }
        System.out.println("Done.");
    }

    /**
     * Number of bins for the histogram. Feel free to increase this value over
     * 100, though our tests assume that you have at least 100 bins in your
     * histograms.
     */
    static final int NUM_HIST_BINS = 100;
    
    /**
     * Create a new TableStats object, that keeps track of statistics on each
     * column of a table
     * 
     * @param tableid
     *            The table over which to compute statistics
     * @param ioCostPerPage
     *            The cost per page of IO. This doesn't differentiate between
     *            sequential-scan IO and disk seeks.
     */
    public TableStats(int tableid, int ioCostPerPage) {
        // For this function, you'll have to get the
        // DbFile for the table in question,
        // then scan through its tuples and calculate
        // the values that you need.
        // You should try to do this reasonably efficiently, but you don't
        // necessarily have to (for example) do everything
        // in a single scan of the table.
        // some code goes here
        this.cost = ioCostPerPage;
        this.tableid = tableid;
        this.tupnum = 0;
        //get dbfile
        hF = (HeapFile) (Database.getCatalog()).getDatabaseFile(tableid);

        //this.size = hF.getTupleDesc().numFields();

        TupleDesc td = hF.getTupleDesc();
        int size = td.numFields();
        //scan through tuples
        // throws an exception...
        try{
            TransactionId tempid = new TransactionId();
            //create iterator
            DbFileIterator temp;
            temp = (hF.iterator(tempid));
            temp.open();
            //get statistics like min, max,
            while (temp.hasNext()){
                tupnum = tupnum + 1;
                Tuple tup = temp.next();
                if (this.max == null){//create array
                    //System.out.println("arrays created");
                    this.max = new int[size];
                    this.min = new int[size];
                }
                //for each element
               // System.out.println("before adding values");
                for (int i = 0; i < size; i++){
                    Field f = tup.getField(i);
                    if (f.getType() == Type.INT_TYPE){
                        int val = ((IntField)f).getValue();
                        //compare this value iwth min[i] max [i]
                        if ( val < min[i]) min[i] = val;
                        if (val > max[i]) max[i] = val;
                    }
                }//now we have exact mins and maxs



            }//end while
            //System.out.println(tupnum);

            //create histograms
            iHist = new IntHistogram[size];
            jHist = new StringHistogram[size];
           // System.out.println("size is" + size);
            //now initialize the histogram values
            for (int i = 0; i < size; i+=1){
                Type te = td.getFieldType(i);
                if (te == Type.INT_TYPE){

                    //System.out.println(min[i]  + " " + max[i]) ;
                    //givein our min and max calculated earlier, we can create inthistogram
                    iHist[i] = new IntHistogram(NUM_HIST_BINS, min[i], max[i]);
                   // System.out.println("created intHist" + i);  
                }
                if (te == Type.STRING_TYPE){
                    jHist[i] = new StringHistogram(NUM_HIST_BINS);
                }
                
            }//end for
            //System.out.println("got here");

            //go back to beginning
            temp.rewind();
            //System.out.println("rewinded inc onstructor");
            while (temp.hasNext()){
                
                Tuple tup = temp.next();
                //now we insert into histogram
                //for each eleemtn
                for (int i =0; i < size; i++){
                    //System.out.println("added tuple");
                    Field f = tup.getField(i);
                    //check the types
                    if (f.getType() == Type.INT_TYPE){
                        //cast to int and insert into histo
                        IntField i_f = (IntField)f;
                        int val = i_f.getValue();
                        IntHistogram tempih = iHist[i];
                        tempih.addValue(val);
                    }
                    //add string value
                    else if (f.getType() == Type.STRING_TYPE){
                        StringField s_f = (StringField)f;
                        String val = s_f.getValue();
                        StringHistogram tempjh = jHist[i];
                        tempjh.addValue(val);
                    }
                else{/*do nothing*/}
                }//end for loop
                //System.out.println("finished adding tuples");
            }//end while
            Database.getBufferPool().transactionComplete(tempid);
        }catch(Exception e){
            System.out.println("catch");
        }
    }

    /**
     * Estimates the cost of sequentially scanning the file, given that the cost
     * to read a page is costPerPageIO. You can assume that there are no seeks
     * and that no pages are in the buffer pool.
     * 
     * Also, assume that your hard drive can only read entire pages at once, so
     * if the last page of the table only has one tuple on it, it's just as
     * expensive to read as a full page. (Most real hard drives can't
     * efficiently address regions smaller than a page at a time.)
     * 
     * @return The estimated cost of scanning the table.
     */
    public double estimateScanCost() {
        // some code goes here
        //return 0;
        //cost is file pages * cost per page
        double temp = hF.numPages() * cost;
        return temp;
    }

    /**
     * This method returns the number of tuples in the relation, given that a
     * predicate with selectivity selectivityFactor is applied.
     * 
     * @param selectivityFactor
     *            The selectivity of any predicates over the table
     * @return The estimated cardinality of the scan with the specified
     *         selectivityFactor
     */
    public int estimateTableCardinality(double selectivityFactor) {
        // some code goes here
        //return 0;
        //System.out.println("totalTuples is " + totalTuples());
        double temp = totalTuples() * selectivityFactor;
        //return temp;
        //convert to int >.>
        //System.out.println("Cardinatlity is " + temp);
        return (int)Math.floor(temp);
    }

    /**
     * The average selectivity of the field under op.
     * @param field
     *        the index of the field
     * @param op
     *        the operator in the predicate
     * The semantic of the method is that, given the table, and then given a
     * tuple, of which we do not know the value of the field, return the
     * expected selectivity. You may estimate this value from the histograms.
     * */
    public double avgSelectivity(int field, Predicate.Op op) {
        // some code goes here
        //return 1.0;
        //no value? use max - min/2?
        double ans= 0.0;
        IntHistogram tempih = iHist[field];
        int avg = (tempih.retMax() - tempih.retMin())/2;
        ans = tempih.estimateSelectivity(op, avg);
        // StringHistogram tempsh = jHist[field];
        // String savg  = (tempsh.maxVal() - tempsh.minVal())/2;
        // ans = ans + tempsh.estimateSelectivity(op,savg);
        // ans = ans /2; // could be over 1
        return ans;

    }

    /**
     * Estimate the selectivity of predicate <tt>field op constant</tt> on the
     * table.
     * 
     * @param field
     *            The field over which the predicate ranges
     * @param op
     *            The logical operation in the predicate
     * @param constant
     *            The value against which the field is compared
     * @return The estimated selectivity (fraction of tuples that satisfy) the
     *         predicate
     */
    public double estimateSelectivity(int field, Predicate.Op op, Field constant) {
        // some code goes here
        //return 1.0;

        //check the type
        //use field for which histogram
        //use value of constant for value in estimate
        if (constant.getType() == Type.INT_TYPE){
            //System.out.println("Checking" + field);
            IntHistogram temp = iHist[field];
            IntField i_f = (IntField)constant;
            int val = i_f.getValue();
            if (temp == null)
                System.out.println("you done goofed");
            //System.out.println( temp.estimateSelectivity(op, val));
            return temp.estimateSelectivity(op, val);
        }
        if (constant.getType() == Type.STRING_TYPE){
            //System.out.println("Scanning" + field);
            StringHistogram temp = jHist[field];
            StringField s_f = (StringField)constant;
            String val = s_f.getValue();
            return temp.estimateSelectivity(op, val);
        }
        else{ 
            System.out.println("bugg");
            return 1.0;}
    }

    /**
     * return the total number of tuples in this table
     * */
    public int totalTuples() {
        // some code goes here
        //return 0;
        return this.tupnum;
    }

}
