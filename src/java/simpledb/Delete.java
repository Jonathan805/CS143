package simpledb;

import java.io.IOException;

/**
 * The delete operator. Delete reads tuples from its child operator and removes
 * them from the table they belong to.
 */
public class Delete extends Operator {

    private static final long serialVersionUID = 1L;

    private TransactionId TID;
    private DbIterator CHILD;
    private DbIterator [] CHILDREN;
    private TupleDesc TD;
    private boolean deleted;
    /**
     * Constructor specifying the transaction that this delete belongs to as
     * well as the child to read from.
     * 
     * @param t
     *            The transaction this delete runs in
     * @param child
     *            The child operator from which to read tuples for deletion
     */
    public Delete(TransactionId t, DbIterator child) {
        // some code goes here
        TID = t;
        CHILD = child;
        CHILDREN = new DbIterator[] {CHILD};
        Type [] type = {Type.INT_TYPE};
        String [] string = {"DELETED"};
        TD = new TupleDesc(type, string);
        deleted = false;
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return TD;
    }

    public void open() throws DbException, TransactionAbortedException {
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
     * Deletes tuples as they are read from the child operator. Deletes are
     * processed via the buffer pool (which can be accessed via the
     * Database.getBufferPool() method.
     * 
     * @return A 1-field tuple containing the number of deleted records.
     * @see Database#getBufferPool
     * @see BufferPool#deleteTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        if (deleted)
            return null;

        BufferPool bp = Database.getBufferPool();
        int numDeleted = 0;
        while (CHILD.hasNext()){
            try {
                bp.deleteTuple(TID, CHILD.next());
                numDeleted++;
            }
            catch (Exception e) {
                throw new DbException("Cannot delete tuple");
            }
        }
        Tuple t = new Tuple(TD);    // create return tuple
        t.setField(0, new IntField(numDeleted));
        deleted = true;
        return t;
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
