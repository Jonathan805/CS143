package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {
    private File BF;
    private TupleDesc TD;
    private int pageSize;
    private int NP;
    protected ArrayList<HeapPageId> hpidarray;
    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        BF = f;
        TD = td;
        pageSize = BufferPool.getPageSize();
        hpidarray = new ArrayList<HeapPageId>();
        NP = ((int)BF.length()) / pageSize;
        for (int i = 0; i < numPages(); i++)    // create a HeapPageId for each page in the HeapFile
            hpidarray.add(new HeapPageId(getId(), i));
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return BF;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        // throw new UnsupportedOperationException("implement this");
        return BF.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        // throw new UnsupportedOperationException("implement this");
        return TD;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        try {
            RandomAccessFile RAF = new RandomAccessFile(BF, "r");
            long offset = (long)pid.pageNumber() * pageSize;    // calculate the offset in the file
            RAF.seek(offset);   // move the read head
            byte[] data = new byte[pageSize];
            int result = RAF.read(data, 0, pageSize);  // read from file
            RAF.close();
            if (result != pageSize)
                return null;
            else
                return new HeapPage((HeapPageId)pid, data);
        }
        catch (IOException e) {
            System.out.println("Read errors");
            return null;
        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
        RandomAccessFile RAF = new RandomAccessFile(BF, "rw");
        long offset = (long)page.getId().pageNumber() * pageSize;    // calculate the offset in the file
        RAF.seek(offset);   // move the write head
        byte[] data = page.getPageData();
        RAF.write(data);  // write to file
        RAF.close();
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return NP;
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        ArrayList<Page> pageList = new ArrayList<Page>();
        boolean newPageNeeded = true;

        for (int i = 0; i < numPages(); i++) {
            HeapPage temp = (HeapPage)(Database.getBufferPool().getPage(
                tid, hpidarray.get(i), Permissions.READ_WRITE));
            if (temp.getNumEmptySlots() > 0) {  // if there is page with empty slot(s)
                temp.insertTuple(t);
                pageList.add(temp);
                newPageNeeded = false;
                break;
            }
        }
        if (newPageNeeded) {
            HeapPageId tempid = new HeapPageId(getId(), numPages());
            hpidarray.add(tempid);  // create a page if no page has empty slots
            HeapPage temp = new HeapPage(tempid, HeapPage.createEmptyPageData());
            temp.insertTuple(t);
            writePage(temp);
            NP++;
            pageList.add(temp);
        }

        return pageList;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        ArrayList<Page> pageList = new ArrayList<Page>();
        PageId hpid = t.getRecordId().getPageId();

        HeapPage temp = (HeapPage)(Database.getBufferPool().getPage(
            tid, hpid, Permissions.READ_WRITE));
        if (temp == null)
            throw new DbException("tuple is not a member of this file.");

        temp.deleteTuple(t);
        pageList.add(temp);
        
        return pageList;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        class DBterator implements DbFileIterator{
            private int index;
            private HeapFile HF;
            private TransactionId TID;
            private Iterator<Tuple> IT;
            private int heapindex;
            public DBterator(HeapFile hf, TransactionId tid){
                TID = tid;
                HF = hf;
                index = 0;
                heapindex = 0;
                IT = null;
            }
            @Override
            //returns next tuple, needs tuple iterator
            public boolean hasNext() throws DbException, TransactionAbortedException {
                if (IT == null){
                    //iterator hasn't bee=n declared so open
                   return false;
                }
                else{//iT declared
                    //check if there is a tuple!

                    if (IT.hasNext())
                        return true;
                    else{
                        //move onto next oage if needed
                        if (heapindex < HF.numPages())
                        {
                            heapindex++;
                            open();
                            return hasNext();
                        }
                        else {
                            return false;
                        }
                    }
                }


            }

            @Override
            public void close() {
                IT = null;
            }

            @Override
            //starts the iterator.
            public void open() throws DbException, TransactionAbortedException {
                //open creates the iterator
                if (!(heapindex == HF.numPages())){ 
                HeapPageId ID = HF.hpidarray.get(heapindex);
                //ID = new HeapPageId(HF.getId(), heapindex);
                //System.out.println( heapindex + " + " + ID.pageNumber());

                Page temp = (Database.getBufferPool().getPage(TID, ID, Permissions.READ_ONLY));
                //System.out.println(Database.getBufferPool().intPage.keySet().toString());
                //System.out.println(ID.hashCode());


                //IT is now the new iterator
                //System.out.println(temp==null);
                IT = ((HeapPage)temp).iterator();

                //System.out.println(IT==null);
                }
            }

            @Override
            public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
                if(IT != null){
                    Tuple toReturn = null;//check for another tuple
                    if(this.hasNext()) {
                        toReturn = IT.next();//gets next tuple from buffer page
                        return toReturn;
                    }
                    else
                        throw new NoSuchElementException();
                }
                //if IT is null
                else{
                    throw new NoSuchElementException();
                }

            }

            @Override
            public void rewind() throws DbException, TransactionAbortedException {
                //rewinds the iterator back to beginning
                index = 0;
                heapindex = 0;
                // System.out.println("rewind!");
                open();
                // System.out.println("hasNext is " + hasNext());
            }
        }
        DBterator temp = new DBterator(this, tid);
        return temp;
    }


}

