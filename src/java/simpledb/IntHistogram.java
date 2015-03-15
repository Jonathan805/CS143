package simpledb;

/** A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {
    private int nbuckets;
    private int min;
    private int max;
    
    private int hintogram[];
   
    
    
    private int values;
    
    private double bucket_size;

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

    	this.nbuckets = buckets;
    	this.min = min;
    	this.max = max;
    	
    	
    	
    	//create the histogram with (buckets)
    	//careful if we have more buckets than we do range
    	int temp = max - min +1; // number of ints in our range
        //for test inthistogram(10000, 0,100) // 100 values
        //we should have 10k buckets but only 100 values, so create 100 buckets
    	if ( temp < buckets){//we have small range, so each int will get a bucket
    	    hintogram = new int[temp];
    	    
    	    bucket_size = 1;
    	    this.nbuckets = temp;
            //System.out.println("this is legal");
            // buckets = temp;
    	}
    	else{//buckets > temp , so bucket size > 1, use buckets
    	    hintogram = new int[this.buckets];
    	    bucket_size =( double)temp/(double)(this.buckets);
    	}
    	
    	
    	//we have no values :(
    	values = 0;
    	//initialize the array
    	for (int i = 0; i < temp; i++){
    		hintogram[i] = 0;
    	}
    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
        //System.out.println(buckets);
    	// some code goes here

        System.out.println("The number of buckets is " + this.nbuckets);
        
    	if (v < this.min || v > this.max){
    	    //invalid, do nothing
    	}
    	else{
    	    //add to histogram
    	    values+=1;
            //System.out.println("added value");
    	    //add one to corresponding bucket
    	    //bucket index = v-min/bucket_size
    	    int temp;
    	    if (v == this.max){
    	        temp = this.nbuckets -1; //last bucket
    	    }
    	    else if (v == this.min){
    	        temp = 0;                  //first bucket
    	    }
    	    else{
    	        //calculate the index, scale with size of bucket
    	        double tempd = (v - this.min)/bucket_size;

    	        temp = (int)temp; //index needs to be int, cast -> does rounding?
    	        if (temp == this.nbuckets){//if we happen to round up to buckets, that index doesn't exist

    	            temp -= 1;//start from 0
    	        }
    	    }//end else

            //creates temp which points to where v is


            //test
    	    System.out.println(temp + " " + nbuckets + " " + v);


    	    //temp is index of bucket to add
    	    hintogram[temp]+=1; //h_b add to height
    	    //new hintogram bucket?

    	}
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
        System.out.println("here");
    	// some code goes here
        //return -1.0;
        //OP can equal Predicate.Op.EQUALS
        //*************************.GREATER_THAN
        //*************************.GREATER_THAN_OR_EQ
        //*************************.LESS_THAN
        //*************************.LESS_THAN_OR_EQ
        //*************************.NOT_EQUALS
        
        if (op == Predicate.Op.EQUALS){
            //check if in range
            System.out.println("in equals");
            if (v > this.max || v < this.max){
                //no selectivity at all!
                return 0.0;
            }
            else{
                //convert V to bucket index and check that index
                int index;
    	        double tempd = (v - this.min)/bucket_size;

    	        index = (int)(tempd); //index needs to be int
    	        if (index == this.nbuckets){

    	            index -= 1;
    	        }
        	    //now we have hintogram[index]
        	   //can be 0, can have width, 
        	   //sel = height/width/total num
        	   //diwth = right[index] - left[index]
        	   //if (bucket_size == 1){//width is 1
        	   //     return (double)(hintogram[index]/values);
        	   //     //   height
        	   //     //   values
        	   //}
        	   //else{
        	   //   // int width = right[index] - left[index] +1;
        	   //    return (double) (hintogram[index]/(width*values) );
        	   //    //   height
        	   //    //   width * values
        	   //}
        	   return ((double)hintogram[index]/(double)values);
            }//end if
        }//end equals
        else if (op == Predicate.Op.NOT_EQUALS){
            //not equal means excluding value for V
            //for histogram we can sum the values greather thana nd less than v
            return (estimateSelectivity(Predicate.Op.GREATER_THAN, v) + estimateSelectivity(Predicate.Op.LESS_THAN, v));
        }//end not equals
        else if (op == Predicate.Op.GREATER_THAN){
            //check if in range
            if (v < this.min) return 1.0;
            if (v > this.max) return 0.0;
            //we have to know the largest value of bucket
            //need right array and left array
            int index;
	        double tempd = ((double)(v - this.min))/(double)bucket_size;
	        index = (int)Math.round(tempd); //index needs to be int
	        if (index == this.nbuckets){
	            index -= 1;
	        }

	     int count = 0;
	     for (int i = index; i < this.buckets; i++){
	     	//from this bucket to the end
	     	count += hintogram[i];
	     	
	     }
	        return (double)count/(double)values;

            
        }//end greater than
        else if (op == Predicate.Op.GREATER_THAN_OR_EQ){
            //we ahve equals and greater than
            //sum the two
            return estimateSelectivity(Predicate.Op.EQUALS, v)+
                        estimateSelectivity(Predicate.Op.GREATER_THAN, v);
        }
        else if (op == Predicate.Op.LESS_THAN){
            //we have greater than or equal to 
            return (1.0 - estimateSelectivity(Predicate.Op.GREATER_THAN_OR_EQ, v));
        }
        else if (op == Predicate.Op.LESS_THAN_OR_EQ){
            //we have greater than
            return (1.0 - estimateSelectivity(Predicate.Op.GREATER_THAN, v));
            
        }
        else{
            //bad instruction?
            return 0.0;
        }
        
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
        return null;
    }
}
