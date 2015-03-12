package simpledb;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Tuple maintains information about the contents of a tuple. Tuples have a
 * specified schema specified by a TupleDesc object and contain Field objects
 * with the data for each field.
 */
public class Tuple implements Serializable {

    private static final long serialVersionUID = 1L;

    private TupleDesc TD;
    private Field[] FA;
    private RecordId RID;

    /**
     * Create a new tuple with the specified schema (type).
     * 
     * @param td
     *            the schema of this tuple. It must be a valid TupleDesc
     *            instance with at least one field.
     */
    public Tuple(TupleDesc td) {

        TD = td;
        FA = new Field[TD.numFields()];
    }

    /**
     * @return The TupleDesc representing the schema of this tuple.
     */
    public TupleDesc getTupleDesc() {
       return TD;
    }

    /**
     * @return The RecordId representing the location of this tuple on disk. May
     *         be null.
     */
    public RecordId getRecordId() {
        // some code goes here
        return this.RID;
    }

    /**
     * Set the RecordId information for this tuple.
     * 
     * @param rid
     *            the new RecordId for this tuple.
     */
    public void setRecordId(RecordId rid) {
        // some code goes here
        this.RID = rid;
    }

    /**
     * Change the value of the ith field of this tuple.
     * 
     * @param i
     *            index of the field to change. It must be a valid index.
     * @param f
     *            new value for the field.
     */
    public void setField(int i, Field f) {
        // some code goes here
        //check if valid
        if (i <0 || i > TD.numFields()) {}
            //do nothing
        else //assign f to FA
            this.FA[i] = f;
    }

    /**
     * @return the value of the ith field, or null if it has not been set.
     * 
     * @param i
     *            field index to return. Must be a valid index.
     */
    public Field getField(int i) {
        // some code goes here
        if (i < 0 || i > TD.numFields()){
            return null;
        }
        else
            return FA[i];
    }

    /**
     * Returns the contents of this Tuple as a string. Note that to pass the
     * system tests, the format needs to be as follows:
     * 
     * column1\tcolumn2\tcolumn3\t...\tcolumnN\n
     * 
     * where \t is any whitespace, except newline, and \n is a newline
     */
    public String toString() {
        // some code goes here
        //throw new UnsupportedOperationException("Implement this");
        String toReturn ="";

        //have to sepearate contents into columns with \t
        for (int i = 0 ; i < TD.numFields(); i++){
            //add field and then '\t'
            if (i == TD.numFields() - 1)
                toReturn += FA[i].toString() + "\n";
            else
                toReturn += FA[i].toString() + "\t";
        }
        return toReturn;
    }
    
    /**
     * @return
     *        An iterator which iterates over all the fields of this tuple
     * */
    public Iterator<Field> fields()
    {
        // some code goes here
        //return null;
        class toReturn implements Iterator<Field>{
            public toReturn(Tuple tup){
                this.tup = tup;
                this.index = 0;
            }
            public Field next(){
                return this.tup.FA[index++];
            }
            public void remove(){
                
            }
            public boolean hasNext(){
                if (index + 1 < this.tup.TD.numFields())
                    return true;
                else
                    return false;
            }
            public int index;
            public Tuple tup;
        }
        return new toReturn(this);
    }
    
    /**
     * reset the TupleDesc of this tuple
     * */
    public void resetTupleDesc(TupleDesc td)
    {
        FA = null;
        TD = null;
        RID = null;
    }
}
