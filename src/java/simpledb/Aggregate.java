package simpledb;
import java.util.*;
/**
* The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
* min). Note that we only support aggregates over a single column, grouped by a
* single column.
*/
public class Aggregate extends Operator {
    private static final long serialVersionUID = 1L;
        /**
        * Constructor.
        *
        * Implementation hint: depending on the type of afield, you will want to
        * construct an {@link IntAggregator} or {@link StringAggregator} to help
        * you with your implementation of readNext().
        *
        *
        * @param child
        * The DbIterator that is feeding us tuples.
        * @param afield
        * The column over which we are computing an aggregate.
        * @param gfield
        * The column over which we are grouping the result, or -1 if
        * there is no grouping
        * @param aop
        * The aggregation operator to use
        */
    private DbIterator mychild;
    private int afield;
    private int gfield;
    private Aggregator.Op aop;
    public DbIterator iterator;    
    private TupleDesc desc;


        public Aggregate(DbIterator child, int afield, int gfield, Aggregator.Op aop) {
        // some code goes here
        this.mychild = child;
        this.afield = afield;
        this.gfield = gfield;
        this.aop = aop;
        this.iterator = null;
        
        if (gfield == Aggregator.NO_GROUPING) {
            //no grouping so only add aggregate field
            //from iterator create new TupleDesc
            Type t[] = { mychild.getTupleDesc().getFieldType(aggregateField())};
            String[] s = {mychild.getTupleDesc().getFieldName(aggregateField())};
            desc = new TupleDesc(t,s);
        }
        else {
            //pair grouping with aggregate {g, a}
            Type[] t = {mychild.getTupleDesc().getFieldType(gfield), mychild.getTupleDesc().getFieldType(aggregateField())};
            String[] s = {mychild.getTupleDesc().getFieldName(this.gfield), mychild.getTupleDesc().getFieldName(aggregateField())};
            desc = new TupleDesc(t,s);
        }
    }

        /**
        * @return If this aggregate is accompanied by a groupby, return the groupby
        * field index in the <b>INPUT</b> tuples. If not, return
        * {@link simpledb.Aggregator#NO_GROUPING}
        * */
        public int groupField() {
        if (this.gfield == Aggregator.NO_GROUPING)
            return Aggregator.NO_GROUPING;
        else
            return this.gfield;
        
        }

        /**
        * @return If this aggregate is accompanied by a group by, return the name
        * of the groupby field in the <b>OUTPUT</b> tuples If not, return
        * null;
        * */

        public String groupFieldName() {
            // some code goes here
            if (this.gfield != Aggregator.NO_GROUPING){
                return mychild.getTupleDesc().getFieldName(this.gfield);
            }
            return null;
        }

        /**
        * @return the aggregate field
        * */

        public int aggregateField() {
        // some code goes here
            return afield;
        }

            /**
            * @return return the name of the aggregate field in the <b>OUTPUT</b>
            * tuples
            * */

            public String aggregateFieldName() {
                // some code goes here
                return mychild.getTupleDesc().getFieldName(aggregateField());
            }

        /**
        * @return return the aggregate operator
        * */

        public Aggregator.Op aggregateOp() {
        // some code goes here
            return this.aop;
        }


        public static String nameOfAggregatorOp(Aggregator.Op aop) {
            
            return aop.toString();

        }

            //open opens iterator of the String or Int agg
            public void open() throws NoSuchElementException, DbException,
            TransactionAbortedException {
                // some code goes here
                //need to open the operaterinterface
                super.open();
                Aggregator tempAgg = null;

                Type temp = mychild.getTupleDesc().getFieldType(aggregateField());

                if (gfield == Aggregator.NO_GROUPING) {//no grouping
                    //string or int
                    if (temp == Type.STRING_TYPE){
                        tempAgg = new StringAggregator(Aggregator.NO_GROUPING, null, 0, this.aggregateOp());
                    }
                    else {
                        tempAgg = new IntegerAggregator(Aggregator.NO_GROUPING, null, 0, this.aggregateOp());
                    }
                    
                }//end grouping
                else {//grouping
                    if (temp == Type.STRING_TYPE){
                        tempAgg = new StringAggregator(0, mychild.getTupleDesc().getFieldType(this.groupField()), 1, this.aggregateOp());
                    }
                    else{
                        tempAgg = new IntegerAggregator(0, mychild.getTupleDesc().getFieldType(this.groupField()), 1, this.aggregateOp());
                    }
                    
                }


                mychild.open();
                //merge tuples into aggregate
                //for every tuple
                while (mychild.hasNext()) {
                    Tuple tempTup = mychild.next();
                    Tuple newT = new Tuple(desc);
                    if (gfield == Aggregator.NO_GROUPING) {
                        //only enter aggregate
                        newT.setField(0, tempTup.getField(aggregateField()));
                    }
                    else {
                        // Tuple{g,a}
                        newT.setField(0, tempTup.getField(gfield));
                        newT.setField(1, tempTup.getField(aggregateField()));
                    }
                        tempAgg.mergeTupleIntoGroup(newT);
                }

                mychild.close();
                this.iterator = tempAgg.iterator();
                this.iterator.open();
        }
        /**
        * Returns the next tuple. If there is a group by field, then the first
        * field is the field by which we are grouping, and the second field is the
        * result of computing the aggregate, If there is no group by field, then
        * the result tuple should contain one field representing the result of the
        * aggregate. Should return null if there are no more tuples.
        */
        protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
            //check if there is any value
            if (this.iterator != null && this.iterator.hasNext()){
                
                            return iterator.next();
                }
              return null;
        }


        public void rewind() throws DbException, TransactionAbortedException {
            // some code goes here
            if (this.iterator != null){
                this.iterator.rewind();
            }
        }
        /**
        * Returns the TupleDesc of this Aggregate. If there is no group by field,
        * this will have one field - the aggregate column. If there is a group by
        * field, the first field will be the group by field, and the second will be
        * the aggregate value column.
        *
        * The name of an aggregate column should be informative. For example:
        * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
        * given in the constructor, and child_td is the TupleDesc of the child
        * iterator.
        */
        public TupleDesc getTupleDesc() {
        // some code goes here
            return desc;
        }

        public void close() {
            // some code goes here
            if (this.iterator != null){
                this.iterator.close();
            }
        }

        @Override
        public DbIterator[] getChildren() {
        // some code goes here
            //return mychild
            //can't return mychild.. needs array...
            DbIterator[] temp = new DbIterator[1];
            temp[0] = mychild;
            return temp;
        }

        @Override
        public void setChildren(DbIterator[] children) {
        // some code goes here
            mychild = children[0];
        }
}