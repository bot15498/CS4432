package simpledb.index.ehash;

import simpledb.file.Block;
import simpledb.index.Index;
import simpledb.query.Constant;
import simpledb.query.TableScan;
import simpledb.record.RID;
import simpledb.record.RecordFile;
import simpledb.record.Schema;
import simpledb.record.TableInfo;
import simpledb.tx.Transaction;

import static simpledb.metadata.TableMgr.MAX_NAME;

public class ExtensibleHashIndex implements Index {

    public static final int MAX_BUCKET_SIZE = 4;
    public static final int MAX_HASH_SIZE = 20;

    private String idxname;
    private Schema sch;
    private Transaction tx;
    private Constant searchKey;
    private TableScan currTableScan; //table scan of bucket correlating to the searchkey
    private TableInfo hashTbl;
    private int globalDepth = 0;

    public ExtensibleHashIndex(String idxname, Schema sch, Transaction tx) {
        this.idxname = idxname;
        this.sch = sch;
        this.tx = tx;
        //Table info for upper level hash table. Schema for table is hash number, free spots in that bucket, and name of table that represents the bucket
        String hashTable = idxname + "ehashtbl";
        Schema hashTableSch = new Schema();
        hashTableSch.addStringField("hash", MAX_HASH_SIZE);
        hashTableSch.addIntField("freeSpots");
        hashTableSch.addStringField("pointerTable", MAX_NAME);
        hashTbl = new TableInfo(hashTable, hashTableSch);
        setGlobalDepth(); //puts global depth into memory

        //add first two buckets to hash index
//        RecordFile bucket0 = new RecordFile(hashTbl, tx);
//        bucket0.insert();
//        bucket0.setInt("hash",0);
//        bucket0.setInt("freeSpots", MAX_BUCKET_SIZE);
//        bucket0.setString("pointerTable",);
//        bucket0.close();
//        RecordFile bucket1 = new RecordFile(hashTbl, tx);
//        bucket0.insert();
//        bucket0.setInt("hash",1);
//        bucket0.setInt("freeSpots", MAX_BUCKET_SIZE);
//        bucket0.close();

        //TODO: Create table info for top level hash table

        //TableInfo for buckets:
//        String bucketTable = idxname + "bucket";
//        bucketTi = new TableInfo(bucketTable, sch);
//        if (tx.size(bucketTi.fileName()) == 0) {
//            tx.append(bucketTi.fileName(), new EHashPageFormatter(bucketTi, -1)); //initialize all pages to be empty
//        }

        //Table info for upper level hash table.
//        String hashTable = idxname + "ehashtbl";
//        Schema hashTableSch = new Schema();
//        hashTableSch.add("block", sch);
//        hashTableSch.add("dataval", sch);
//        hashTableTi = new TableInfo(hashTable, hashTableSch);
//        topBlock = new Block(hashTableTi.fileName(), 0);
//        if (tx.size(hashTableTi.fileName()) == 0) {
//            //If
//            tx.append(hashTableTi.fileName(), new EHashPageFormatter(hashTableTi, 0));
//        }
    }

    /**
     * Sets the searchkey to be looked at and loads the bucket that the key corresponds to into memory
     *
     * @param searchkey the search key value.
     */
    public void beforeFirst(Constant searchkey) {
        close();
        this.searchKey = searchkey;
        //bucket = identifier to bucket
        String bucket = convertToHash(searchkey, globalDepth);
        String tblname = idxname + bucket;
        TableInfo ti = new TableInfo(tblname, sch);
        currTableScan = new TableScan(ti, tx);

        //filter searchkey
        searchkey.hashCode()

        TableScan hashts = new TableScan(hashTbl, tx);
        hashts.beforeFirst();
        boolean found = false;
        while (!found && hashts.next()) {
            if (hashts.getVal("dataval").equals(searchkey)) {
                found = true;
            }
        }
    }

    //done
    public boolean next() {
        while (currTableScan.next())
            if (currTableScan.getVal("dataval").equals(searchKey))
                return true;
        return false;
    }

    public RID getDataRid() {
        return null;
    }

    public void insert(Constant dataval, RID datarid) {
        beforeFirst(dataval); //get to table/bucket we want
        currTableScan.insert();
        currTableScan.setInt("block", datarid.blockNumber());
        currTableScan.setInt("id", datarid.id());
        currTableScan.setVal("dataval", dataval);

    }

    //done
    public void delete(Constant dataval, RID datarid) {
        beforeFirst(dataval);
        //Only concerned with records in our currrent hash bucket because duplicates of dataval would be together
        while(next())
            if (getDataRid().equals(datarid)) {
                currTableScan.delete();
                return;
            }
    }

    //maybe done
    public void close() {
        if (currTableScan != null) {
            currTableScan.close();
        }
    }

    /**
     * Helper function that gets the least significant bits of a value's HashCode for indexing.
     *
     * @param value The constant
     * @param depth How many bits to look at.
     * @return A string of 0's and 1's that are the last bits of a hash code.
     */
    private String convertToHash(Constant value, int depth) {
        int hashCode = value.hashCode();
        int mask = (int) Math.pow(2, depth) - 1;
        int bucketVal = hashCode & mask;
        String bucketValString = Integer.toBinaryString(bucketVal);
        while (bucketValString.length() < depth) {
            bucketValString = "0" + bucketValString;
        }
        return bucketValString;
    }

    private void setGlobalDepth() {
        //First check to see if metadata record with global depth exists.
        TableScan hashTblScan = new TableScan(hashTbl, tx);
        hashTblScan.beforeFirst();
        while(hashTblScan.next()) {
            if(hashTblScan.getString("pointerTable").equals("globalDepth")) {
                globalDepth = hashTblScan.getInt("freeSpots");
                return;
            }
        }
        //reached end of hash table without finding metadata record. This means table is new and needs one
        hashTblScan.beforeFirst();
        hashTblScan.insert();
        hashTblScan.setInt("freeSpots",1); //set global depth to be 1
        hashTblScan.setString("pointerTable","globalDepth");
        globalDepth = 1;
    }
}