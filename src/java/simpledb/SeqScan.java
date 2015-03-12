package simpledb;

import javax.xml.crypto.Data;
import java.util.*;

/**
 * SeqScan is an implementation of a sequential scan access method that reads
 * each tuple of a table in no particular order (e.g., as they are laid out on
 * disk).
 */
public class SeqScan implements DbIterator {

    private static final long serialVersionUID = 1L;
    private TransactionId TID;
    private int tableID;
    private String alias;
    private DbFileIterator Dbterator;
    /**
     * Creates a sequential scan over the specified table as a part of the
     * specified transaction.
     * 
     * @param tid
     *            The transaction this scan is running as a part of.
     * @param tableid
     *            the table to scan.
     * @param tableAlias
     *            the alias of this table (needed by the parser); the returned
     *            tupleDesc should have fields with name tableAlias.fieldName
     *            (note: this class is not responsible for handling a case where
     *            tableAlias or fieldName are null. It shouldn't crash if they
     *            are, but the resulting name can be null.fieldName,
     *            tableAlias.null, or null.null).
     */
    public SeqScan(TransactionId tid, int tableid, String tableAlias) {
        // some code goes here
        this.alias = tableAlias;
        this.tableID = tableid;
        this.TID = tid;
        Dbterator = null;
    }

    /**
     * @return
     *       return the table name of the table the operator scans. This should
     *       be the actual name of the table in the catalog of the database
     * */
    public String getTableName() {
        return Database.getCatalog().getTableName(tableID);
    }
    
    /**
     * @return Return the alias of the table this operator scans. 
     * */
    public String getAlias()
    {
        // some code goes here
        return alias;
    }

    /**
     * Reset the tableid, and tableAlias of this operator.
     * @param tableid
     *            the table to scan.
     * @param tableAlias
     *            the alias of this table (needed by the parser); the returned
     *            tupleDesc should have fields with name tableAlias.fieldName
     *            (note: this class is not responsible for handling a case where
     *            tableAlias or fieldName are null. It shouldn't crash if they
     *            are, but the resulting name can be null.fieldName,
     *            tableAlias.null, or null.null).
     */
    public void reset(int tableid, String tableAlias) {
        // some code goes here
        this.tableID = tableid;
        this.alias = tableAlias;
        this.Dbterator = null;
    }

    public SeqScan(TransactionId tid, int tableid) {
        this(tid, tableid, Database.getCatalog().getTableName(tableid));
    }

    public void open() throws DbException, TransactionAbortedException {
        //open a heap file
        this.Dbterator = ((HeapFile)(Database.getCatalog().getDatabaseFile(this.tableID))).iterator(this.TID);
        this.Dbterator.open();
        // some code goes here
    }

    /**
     * Returns the TupleDesc with field names from the underlying HeapFile,
     * prefixed with the tableAlias string from the constructor. This prefix
     * becomes useful when joining tables containing a field(s) with the same
     * name.
     * 
     * @return the TupleDesc with field names from the underlying HeapFile,
     *         prefixed with the tableAlias string from the constructor.
     */
    public TupleDesc getTupleDesc() {
        // tuple Desc needs array of types and strings
        // use alias to get these types
        TupleDesc temp = ((HeapFile)(Database.getCatalog().getDatabaseFile(this.tableID))).getTupleDesc();
        String [] buildstr = new String[temp.numFields()];
        Type [] buildtyp = new Type [temp.numFields()];

        //fill arrays
        for (int i = 0; i < temp.numFields(); i++) {
            buildtyp[i] = temp.getFieldType(i);     //copy type
            //field names are "prefixed with table alias string
            buildstr[i] = this.alias + temp.getFieldName(i);
        }
        // some code goes here

        //return new tuple
        return new TupleDesc(buildtyp,buildstr);
    }

    public boolean hasNext() throws TransactionAbortedException, DbException {
        // some code goes here
        return this.Dbterator.hasNext();
    }

    public Tuple next() throws NoSuchElementException,
            TransactionAbortedException, DbException {
        // some code goes here
        return this.Dbterator.next();
    }

    public void close() {
        // some code goes here
        this.Dbterator.close();
    }

    public void rewind() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        // some code goes here
        this.Dbterator.rewind();

    }
}
