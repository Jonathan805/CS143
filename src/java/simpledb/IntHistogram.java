package simpledb;

/** A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {
    private int buckets;
    private int min;
    private int max;
    
    private int hintogram[];
    private int left[];// b.left
    private int right[];
    
    
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
    	buckets = buckets;
    	min = min;
    	max = max;
    	
    	
    	
    	//create the histogram with (buckets)
    	//careful if we have more buckets than we do range
    	int temp = max - min + 1; // number of ints in our range
    	if ( temp < buckets){//we have small range, so each int will get a bucket
    	    hintogram = new int[temp];
    	    left = new int[temp];
    	    right = new int[temp];
    	    bucket_size = 1;
    	    buckets = temp;
    	}
    	else{//buckets > temp , so bucket size > 1, use buckets
    	    hintogram = new int[buckets];
    	    left = new int[buckets];
    	    right = new int [buckets];
    	    bucket_size =( double)temp/(double)(buckets);
    	    
    	}
    	
    	
    	//we have no values :(
    	values = 0;
    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
    	// some code goes here
    	if (v < this.min || v > this.max){
    	    //invalid, do nothing
    	}
    	else{
    	    //add to histogram
    	    values+=1;
    	    //add one to corresponding bucket
    	    //bucket index = v-min/bucket_size
    	    int temp;
    	    if (v == this.max){
    	        temp = this.buckets -1;
    	    }
    	    else if (v == this.min){
    	        temp = 0;
    	    }
    	    else{
    	        //calculate the index, scale with size of bucket
    	        double tempd = (v - this.min)/bucket_size;
    	        temp = (int)Math.round(tempd); //index needs to be int
    	        if (temp == buckets){//if we happen to round up to buckets, that index doesn't exist
    	            temp -= 1;
    	        }
    	    }//end else
    	    
    	    //temp is index of bucket to add
    	    hintogram[temp]+=1; //h_b
    	    //new hintogram bucket?
    	    if (hintogram[temp] == 1){
    	        //new bar, new data
    	        left[temp] = v;
    	        right[temp] = v;
    	    }
    	    else{
    	        //if bucket size is larger than 1 then we need to update some data
    	        //don't have to if size is equal 1
    	        if (this.bucket_size != 1){
    	            if (v < left[temp]){
        	            //update left
        	            left[temp] = v;
        	        }
        	        if (v > right[temp]){
        	            //update right
        	            right[temp] = v;
        	        }
    	       }
    	    }
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
            if (v > this.max || v < this.max){
                //no selectivity at all!
                return 0.0;
            }
            else{
                //convert V to bucket index and check that index
                int index;
    	        double tempd = (v - this.min)/bucket_size;
    	        
    	        index = (int)Math.round(tempd); //index needs to be int
    	        if (index == buckets){
    	            index -= 1;
    	        }
        	    //now we have hintogram[index]
        	   //can be 0, can have width, 
        	   //sel = height/width/total num
        	   //diwth = right[index] - left[index]
        	   if (bucket_size == 1){//width is 1
        	        return (double)(hintogram[index]/values);
        	        //   height
        	        //   values
        	   }
        	   else{
        	       int width = right[index] - left[index] +1;
        	       return (double) (hintogram[index]/(width*values) );
        	       //   height
        	       //   width * values
        	   }
            }
        }
        else if (op == Predicate.Op.NOT_EQUALS){
            //not equal means excluding value for V
            //for histogram we can sum the values greather thana nd less than v
            return (estimateSelectivity(Predicate.Op.GREATER_THAN, v) + estimateSelectivity(Predicate.Op.LESS_THAN, v));
        }
        else if (op == Predicate.Op.GREATER_THAN){
            //check if in range
            if (v < thix.min) return 1.0;
            if (v > this.max) return 0.0;
            //we have to know the largest value of bucket
            //need right array and left array
            int index;
	        double tempd = ((double)(v - this.min))/(double)bucket_size;
	        index = (int)Math.round(tempd); //index needs to be int
	        if (index == buckets){
	            index -= 1;
	        }
	        if (right[index] > v){
	            double ans = 0.0;
	            //starting from bucket for V, go through all buckets and aggreate selectivity
                for (int i = index; i < this.buckets; i++){
                    double bS = ((double) this.hintogram[i])/(double)values;
                    
                    int width;
                    if (bucket_size == 1){
                        width =1;
                    }
                    else{
                        width = right[i]- left[i] +1;
                    }
                    //if v is within a bucket, we need to estimate within the bucket, use the right value
                    double buck_sel = 1.0;
                    if (v > this.lowest[i]){
                        
                        buck_sel = ((double) (this.right[i] - v)/(double) width);
                    }
                    ans += bS * buck_sel;
                    
                }
                return ans;
	        }
	        else{
	            //v is the greatest part of the histogram, we can include the whole thing
	            //similar to previous code, but we start fro mthe next index
	            double ans = 0.0;
	            //starting from bucket for V, go through all buckets and aggreate selectivity
                for (int i = index; i < this.buckets; i++){
                    double bS = ((double) this.hintogram[i])/(double)values;
                    
                    int width;
                    if (bucket_size == 1){
                        width =1;
                    }
                    else{
                        width = right[i]- left[i] +1;
                    }
                    //if v is within a bucket, we need to estimate within the bucket, use the right value
                    double buck_sel = 1.0;
                    if (v > this.lowest[i]){
                        
                        buck_sel = ((double) (this.right[i] - v)/(double) width);
                    }
                    ans += bS * buck_sel;
                    
                }
                return ans;
	            
	        }
            
        }
        else if (op == Predicate.OP.GREATER_THAN_OR_EQ){
            //we ahve equals and greater than
            //sum the two
            return estimateSelectivity(Predicate.Op.EQUALS, v)+
                        estimateSelectivity(Predicate.Op.GREATER_THAN, v);
        }
        else if (op == Predicate.OP.LESS_THAN){
            //we have greater than or equal to 
            return (1.0 - estimateSelectivity(Predicate.Op.GREATER_THAN_OR_EQ, v));
        }
        else if (op == Predicate.OP.LESS_THAN_OR_EQ){
            //we have greater than
            return (1.0 - estimateSelectivity(Predicate.Op.GREATER_THAN, v));
            
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
