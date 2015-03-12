package simpledb;
import java.util.HashMap;
import java.util.ArrayList;


import simpledb.Aggregator.Op;
/**
* Knows how to compute some aggregate over a set of StringFields.
*/
public class StringAggregator implements Aggregator {
        private static final long serialVersionUID = 1L;

        //set at constructor
        private int gbfield;        
        private int afield;
        private Op what;
        private TupleDesc tupleDesc;

        // g->a
        private ArrayList<Tuple> list;
        private HashMap<Field, ArrayList<String>> map;


        //tuples not working, mismatched, need temporary td
        //for integers
        private TupleDesc td;
        
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
            this.afield = afield;
            this.what = what;
            
            //check for grouping {g, a}
            if (gbfield != Aggregator.NO_GROUPING) {
                Type[] t = {gbfieldtype, Type.STRING_TYPE};
                this.tupleDesc = new TupleDesc(t);

                Type[] ta = {gbfieldtype, Type.INT_TYPE};
                td = new TupleDesc(ta);
            }
            else {//jsut use {a}
                Type[] t = {Type.STRING_TYPE};
                this.tupleDesc = new TupleDesc(t);

                Type[] ta = {Type.INT_TYPE};
                td = new TupleDesc(ta);
            }
            //initialize array and map
            list = new ArrayList<Tuple>();
            map = new HashMap<Field, ArrayList<String>>();
        }
        /**
        * Merge a new tuple into the aggregate, grouping as indicated in the constructor
        * @param tup the Tuple containing an aggregate field and a group-by field
        */
        public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
            //check for grouping, the only operator is COUNT, so we return the size
            if (gbfield == Aggregator.NO_GROUPING){
                //if not initialized
                if (list.size() == 0) {
                        //ad tuple to list                        
                        Tuple temp = new Tuple(td);
                        IntField intField = new IntField(1);
                        temp.setField(afield, intField);
                        
                        StringField f = (StringField) tup.getField(afield);
                        ArrayList<String> values = new ArrayList<String>();
                        values.add(f.getValue());


                        //add to hashmap and list with nogrouping value
                        map.put(new IntField(Aggregator.NO_GROUPING), values);
                        list.add(temp);
                        return;
                    }
                    else{
                        //retrieve from hashmap
                        ArrayList<String> stringal = map.get(new IntField(Aggregator.NO_GROUPING));
                        //convert input to string to add to array
                        StringField input = (StringField) tup.getField(afield);
                        stringal.add(input.getValue());


                        IntField f = new IntField(stringal.size());
                        //yodate
                        list.get(0).setField(afield, f);
                }
            }//end no rouping
            else {//grouping
                    if (list.size() == 0) {
                        IntField if1 = new IntField(1);
                        Tuple toAdd = new Tuple(td);
                        toAdd.setField(gbfield, tup.getField(gbfield));
                        toAdd.setField(afield, if1);   

                        //add tuple to map by adding to a string list
                        StringField sf = (StringField) tup.getField(afield);
                        ArrayList<String> stringal = new ArrayList<String>();
                        stringal.add(sf.getValue());


                        //udpate
                        map.put(tup.getField(gbfield), stringal);
                        list.add(toAdd);
                        return;
                        }
                    //search for equvalent array stored using gfield
                    for (int i = 0; i < list.size(); i++){
                        if (list.get(i).getField(gbfield).equals (tup.getField(gbfield))){
                                //entry already exists, add new value
                                
                                ArrayList<String> stringal = map.get(list.get(i).getField(gbfield));
                                StringField fTemp = (StringField) tup.getField(afield);
                                stringal.add(fTemp.getValue());
                                IntField f2 = new IntField(stringal.size());

                                list.get(i).setField(afield, f2);
                                return;
                        }
                    }
                    //no match so we add new entry to hashmap
                        IntField if1 = new IntField(1);
                        Tuple tempT = new Tuple(td);
                        //grouping so add two values, tup.getField is unique
                        tempT.setField(0, tup.getField(0));
                        tempT.setField(1, if1);
                        
                        StringField f2 = (StringField) tup.getField(afield);
                        ArrayList<String> stringal = new ArrayList<String>();
                        stringal.add(f2.getValue());

                        map.put(tup.getField(gbfield), stringal);
                        list.add(tempT);
                }
      }

                
        /**
        * Create a DbIterator over group aggregate results.
        *
        * @return a DbIterator whose tuples are the pair (groupVal,
        * aggregateVal) if using group, or a single (aggregateVal) if no
        * grouping. The aggregateVal is determined by the type of
        * aggregate specified in the constructor.
        */
        public DbIterator iterator() {
        // some code goes here
            return new TupleIterator(td, list);
        }
}