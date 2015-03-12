package simpledb;

import java.util.*;

/**
 * Filter is an operator that implements a relational select.
 */
public class Filter extends Operator {

    private static final long serialVersionUID = 1L;
    private Predicate PRE;
    private DbIterator CHILD;
    private DbIterator[] CHILDREN;

    /**
     * Constructor accepts a predicate to apply and a child operator to read
     * tuples to filter from.
     * 
     * @param p
     *            The predicate to filter tuples with
     * @param child
     *            The child operator
     */
    public Filter(Predicate p, DbIterator child) {
        // some code goes here
        PRE = p;
        CHILD = child;
        CHILDREN = new DbIterator[] {CHILD};
    }

    public Predicate getPredicate() {
        // some code goes here
        return PRE;
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return CHILD.getTupleDesc();
    }

    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        // some code goes here
        CHILD.open();
        super.open();
    }

    public void close() {
        // some code goes here
        super.close();
        CHILD.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        CHILD.rewind();
    }

    /**
     * AbstractDbIterator.readNext implementation. Iterates over tuples from the
     * child operator, applying the predicate to them and returning those that
     * pass the predicate (i.e. for which the Predicate.filter() returns true.)
     * 
     * @return The next tuple that passes the filter, or null if there are no
     *         more tuples
     * @see Predicate#filter
     */
    protected Tuple fetchNext() throws NoSuchElementException,
            TransactionAbortedException, DbException {
        // some code goes here
        while (CHILD.hasNext()) {
            Tuple temp = CHILD.next();
            if (PRE.filter(temp))   // filter the tuple returned by the child operator
                return temp;
        }
        return null;
    }

    @Override
    public DbIterator[] getChildren() {
        // some code goes here
        return CHILDREN;
    }

    @Override
    public void setChildren(DbIterator[] children) {
        // some code goes here
        CHILDREN = children;
        CHILD = CHILDREN[0];
    }

}
