package simpledb;


import java.util.ArrayList;
import java.util.HashMap;
/**
* Knows how to compute some aggregate over a set of IntFields.
*/
public class IntegerAggregator implements Aggregator {
      private static final long serialVersionUID = 1L;
      private int gbfield;      
      private int afield;
      private Op what;
      private TupleDesc tupleDesc;
      private ArrayList<Tuple> list;
      private HashMap<Field, ArrayList<Integer>> counts;
      /**
      * Aggregate constructor
      *
      * @param gbfield
      * the 0-based index of the group-by field in the tuple, or
      * NO_GROUPING if there is no grouping
      * @param gbfieldtype
      * the type of the group by field (e.g., Type.INT_TYPE), or null
      * if there is no grouping
      * @param afield
      * the 0-based index of the aggregate field in the tuple
      * @param what
      * the aggregation operator
      */
        public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
        this.gbfield = gbfield;
        
        this.afield = afield;
        this.what = what;

          // grouping {g, a}
          if (gbfield != Aggregator.NO_GROUPING) {
            Type[] typeArray = {gbfieldtype, Type.INT_TYPE};
            this.tupleDesc = new TupleDesc(typeArray);
          }

          //no grouping
          else {
            Type[] typeArray = {Type.INT_TYPE};
            this.tupleDesc = new TupleDesc(typeArray);
          }
            list = new ArrayList<Tuple>();
            counts = new HashMap<Field, ArrayList<Integer>>();
        }


        /**
        * Merge a new tuple into the aggregate, grouping as indicated in the
        * constructor
        *
        * @param tup
        * the Tuple containing an aggregate field and a group-by field
        */
        public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        //check for grouping
        if (gbfield == Aggregator.NO_GROUPING){
          //initialize for empty list
          if (list.size() == 0) {
              IntField f = (IntField) tup.getField(afield);
              ArrayList<Integer> values = new ArrayList<Integer>();
              values.add(f.getValue());
              counts.put(new IntField(Aggregator.NO_GROUPING), values);

              if (what == Op.COUNT) {
                  tup.setField(afield, new IntField(1));
              }
              list.add(tup);
              return;
          }

        // switch (what.toString()) {
        //     case "min": 
       // if (what.toString() == "min"){
          if (what.toString() == "min"){
            System.out.println("min function:" + tup.toString());
                      IntField if1 = (IntField)(list.get(0).getField(afield));
                      IntField if2 = (IntField)(tup.getField(afield));
                      if (if1.getValue() > if2.getValue()) {
                  list.set(0, tup);
                  System.out.println("min function:assigned");
                }
              //  list.add(tup);
              }
        if (what.toString() == "max"){
                      IntField if1 = (IntField)(list.get(0).getField(afield));
                      IntField if2 = (IntField)(tup.getField(afield));
                      if (if1.getValue() < if2.getValue()) {
              list.set(0, tup);
            }
           // list.add(tup);
        }
        if (what.toString() == "sum"){
          //update sum, get the 0th value and then add the new tuple value
          IntField f1 = (IntField)(tup.getField(afield));
          IntField f2 = (IntField)(list.get(0).getField(afield));
          Field f = new IntField(f1.getValue() + f2.getValue());
          //calculate new sum, add to 0th field

          list.get(0).setField(afield, f);
          //list.add(tup);
        }
        if (what.toString() == "count"){
          //update list.get(0)'s field with a new count of array
            ArrayList<Integer> intarr = counts.get(new IntField(Aggregator.NO_GROUPING));
            IntField input = (IntField)tup.getField(afield);
            intarr.add(input.getValue());
            //create new field based off new conut
            IntField f1 = new IntField(intarr.size());
            list.get(0).setField(afield,f1);

            //list.add(tup);

        }
        if (what.toString() == "avg"){
          //calculate the average and update the 0th field
          //get every value of the whole list and caluclate sum
          int sum = 0;
          //convert from field to integer, but we have hash map
          ArrayList<Integer> intarr = counts.get(new IntField(Aggregator.NO_GROUPING));  

          
          for (int i = 0; i < intarr.size(); i++){
              sum += intarr.get(i);
          }
          //convert tuple into intfield, and calbulate sum
          IntField input = (IntField) tup.getField(afield);
          sum += input.getValue();
          //add tuple value
          intarr.add(input.getValue());
          int avg =  sum/(intarr.size());

          //now add to list and intarr
          IntField f3 = new IntField(avg);
          list.get(0).setField(afield, f3);

          // list.add(tup);
        }

              
        }// end if not grouping
          else {//if grouping
              //if list is empty, add values and list
          if (list.size() == 0) {
                Field f1 = tup.getField(gbfield);
                IntField f2 = (IntField) tup.getField(afield);
                ArrayList<Integer> values = new ArrayList<Integer>();
                values.add(f2.getValue());
                counts.put(f1, values);
                if (what == Op.COUNT) {
                    tup.setField(afield, new IntField(1));
                }
                list.add(tup);
                return;
          }
         if (what == Op.AVG){
            for (int i = 0; i < list.size(); i++){
              if (list.get(i).getField(gbfield).equals(tup.getField(gbfield))){

                //calculate average
                ArrayList<Integer> tempal = counts.get(list.get(i).getField(gbfield));
                //add values to asum
                int sum = 0;
                for (int j = 0; j < tempal.size(); j ++){
                    sum += tempal.get(j);
                }//sum has value

                //interact with intput tuple
                IntField f1 = (IntField) tup.getField(afield);
                sum += f1.getValue();
                tempal.add (f1.getValue());
                int avg = sum/tempal.size();
                //remember to add back to counts
                IntField out = new IntField(avg);
                list.get(i).setField(afield,out);
                return;
              }
            }
            //hash doens't exist for that gfield, create one
            list.add(tup);
            //adjust counts with new member, {g a}
            Field f = tup.getField(gbfield);
            IntField if1 = (IntField)tup.getField(afield);
            ArrayList<Integer> temp = new ArrayList<Integer>();
            temp.add(if1.getValue());//new value to array list
            counts.put(f,temp);// add to hash map {g, a}
          }
          if (what == Op.MIN){
                //System.out.println("min function" +tup.toString());
                //for every group, find the matching grouping field
                for (int i = 0; i < list.size(); i++){
                  //if the grouping type matches tup's type
                    if (list.get(i).getField(gbfield).equals(tup.getField(gbfield))){//check if that value is greater than tup
                      IntField if1 = (IntField)(list.get(i).getField(afield));
                      IntField if2 = (IntField)(tup.getField(afield));
                      if (if1.getValue() > if2.getValue()) {
                        //if it is then tup should be the "min"
                        list.set(i, tup);
                      //System.out.println("set");
                      
                      }
                      return;
                    }
                }
                list.add(tup);
             }
          

          if (what == Op.SUM){
            //first find the field
            for (int i = 0; i < list.size(); i++){
              if (list.get(i).getField(gbfield).equals(tup.getField(gbfield))){
                //now we need to add to sum
                IntField f1 = (IntField)tup.getField(afield);
                IntField f2 = (IntField)list.get(i).getField(afield);
                //calculate sum
                int sum = f1.getValue() + f2.getValue();
                Field temp = new IntField(sum);
                //save to list
                list.get(i).setField(afield,temp);
                return;
              }
            }
            list.add(tup);
          }

          if( what == Op.MAX){
                //similar but uses less than predicate
                for (int i = 0; i < list.size(); i++){
                  if (list.get(i).getField(gbfield).equals(tup.getField(gbfield))){
                    
                    IntField if1 = (IntField)(list.get(i).getField(afield));
                      IntField if2 = (IntField)(tup.getField(afield));
                      if (if1.getValue() < if2.getValue()) {
                      list.set(i, tup);
                    }
                    return;
                  }
                }
                list.add(tup);

          }
          
          if (what == Op.COUNT){
              for (int i = 0; i < list.size(); i++){
                if (list.get(i).getField(gbfield).equals(tup.getField(gbfield))){
                  //calculate count...
                  //convert input tuple
                  IntField f1 = (IntField)tup.getField(afield);
                  ArrayList<Integer> tempal = counts.get(list.get(i).getField(gbfield));
                  tempal.add(f1.getValue());
                  //generate new field to set list.get(i) to 
                  IntField f2 = new IntField(tempal.size());
                  list.get(i).setField(afield, f2);
                  return;
                }
              }
              list.add(tup);
              //adjust counts with new member, {g a}
              Field f = tup.getField(gbfield);
              IntField if1 =(IntField) tup.getField(afield);
              ArrayList<Integer> temp = new ArrayList<Integer>();
              temp.add(if1.getValue());//new value to array list
              counts.put(f,temp);// add to hash map {g, a}
              IntField counted = new IntField(1);
              tup.setField(afield,counted);

          }
      
      }//end else
}//end function


          /**
          * Create a DbIterator over group aggregate results.
          *
          * @return a DbIterator whose tuples are the pair (groupVal, aggregateVal)
          * if using group, or a single (aggregateVal) if no grouping. The
          * aggregateVal is determined by the type of aggregate specified in
          * the constructor.
          */
          public DbIterator iterator() {
              // some code goes here
                  return new TupleIterator(tupleDesc, list);
              }
}

    
