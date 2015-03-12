package simpledb;

/**
 * Inserts tuples read from the child operator into the tableid specified in the
 * constructor
 */
public class Insert extends Operator {

    private static final long serialVersionUID = 1L;

    private TransactionId TID;
    private DbIterator CHILD;
    private int table;
    private DbIterator [] CHILDREN;
    private TupleDesc TD;
    private boolean inserted;
    /**
     * Constructor.
     * 
     * @param t
     *            The transaction running the insert.
     * @param child
     *            The child operator from which to read tuples to be inserted.
     * @param tableid
     *            The table in which to insert tuples.
     * @throws DbException
     *             if TupleDesc of child differs from table into which we are to
     *             insert.
     */
    public Insert(TransactionId t,DbIterator child, int tableid)
            throws DbException {
        // some code goes here
        TID = t;
        CHILD = child;
        table = tableid;
        CHILDREN = new DbIterator[] {CHILD};
        Type [] type = {Type.INT_TYPE};
        String [] string = {"INSERTED"};
        TD = new TupleDesc(type, string);
        inserted = false;

        if (!CHILD.getTupleDesc().equals(
            Database.getCatalog().getDatabaseFile(table).getTupleDesc()))
            throw new DbException(
                "TupleDesc of child differs from table into which we are to insert");
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
     * Inserts tuples read from child into the tableid specified by the
     * constructor. It returns a one field tuple containing the number of
     * inserted records. Inserts should be passed through BufferPool. An
     * instances of BufferPool is available via Database.getBufferPool(). Note
     * that insert DOES NOT need check to see if a particular tuple is a
     * duplicate before inserting it.
     * 
     * @return A 1-field tuple containing the number of inserted records, or
     *         null if called more than once.
     * @see Database#getBufferPool
     * @see BufferPool#insertTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        if (inserted)
            return null;

        BufferPool bp = Database.getBufferPool();
        int numInserted = 0;
        while (CHILD.hasNext()){
            try {
                bp.insertTuple(TID, table, CHILD.next());
                numInserted++;
            }
            catch (Exception e) {
                throw new DbException("Cannot insert tuple");
            }
        }
        Tuple t = new Tuple(TD);    // create return tuple
        t.setField(0, new IntField(numInserted));
        inserted = true;
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
