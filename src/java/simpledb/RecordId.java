package simpledb;

import java.io.Serializable;

/**
 * A RecordId is a reference to a specific tuple on a specific page of a
 * specific table.
 */
public class RecordId implements Serializable {

    private static final long serialVersionUID = 1L;
    private PageId PID;
    private int TN;
    /**
     * Creates a new RecordId referring to the specified PageId and tuple
     * number.
     * 
     * @param pid
     *            the pageid of the page on which the tuple resides
     * @param tupleno
     *            the tuple number within the page.
     */
    public RecordId(PageId pid, int tupleno) {
        // some code goes here
        PID = pid;
        TN = tupleno;
    }

    /**
     * @return the tuple number this RecordId references.
     */
    public int tupleno() {
        // some code goes here
        return TN;
    }

    /**
     * @return the page id this RecordId references.
     */
    public PageId getPageId() {
        // some code goes here
        return PID;
    }

    /**
     * Two RecordId objects are considered equal if they represent the same
     * tuple.
     * 
     * @return True if this and o represent the same tuple
     */
    @Override
    public boolean equals(Object o) {
        // some code goes here
        // throw new UnsupportedOperationException("implement this");
        if (o == null) {
            if (this == null)
                return true;
            else
                return false;
        }

        try {
            if (this.PID.equals(((RecordId)o).getPageId()) 
                && this.TN == ((RecordId)o).tupleno())
                return true;
        }
        catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * You should implement the hashCode() so that two equal RecordId instances
     * (with respect to equals()) have the same hashCode().
     * 
     * @return An int that is the same for equal RecordId objects.
     */
    @Override
    public int hashCode() {
        int mask = 0x0000ffff;
        int hash = (PID.pageNumber() & mask) << 16;  // put page number on the higher 16 bits
        hash |= (TN & mask);    // put tuple number on the lower 16 bits
        return hash;
    }

}
