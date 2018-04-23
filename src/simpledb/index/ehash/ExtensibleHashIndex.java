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

    //done
    public ExtensibleHashIndex(String idxname, Schema sch, Transaction tx) {
        this.idxname = idxname;
        this.sch = sch;
        this.tx = tx;
        //Table info for upper level hash table. Schema for table is hash number, free spots in that bucket, and name of table that represents the bucket
        String hashTable = idxname + "ehashtbl";
        Schema hashTableSch = new Schema();
        hashTableSch.addStringField("hash", MAX_HASH_SIZE);
        hashTableSch.addIntField("freeSpots");
        hashTableSch.addIntField("localDepth");
        hashTableSch.addStringField("pointerTable", MAX_NAME);
        hashTbl = new TableInfo(hashTable, hashTableSch);
        setGlobalDepth(); //puts global depth into memory
    }

    /**
     * Sets the searchkey to be looked at and loads the bucket that the key corresponds to into memory
     *
     * @param searchkey the search key value.
     */
    //done
    public void beforeFirst(Constant searchkey) {
        close();
        this.searchKey = searchkey;
        //get name of table that record would be in. Do this by finding hash
        String bucket = convertToHash(searchkey, globalDepth);
        String tblname = idxname + bucket;
        //open new table scan object. If table doesn't exist, it will now.
        TableInfo ti = new TableInfo(tblname, sch);
        currTableScan = new TableScan(ti, tx);
    }

    //done
    public boolean next() {
        while (currTableScan.next())
            if (currTableScan.getVal("dataval").equals(searchKey))
                return true;
        return false;
    }

    //done
    public RID getDataRid() {
        int blknum = currTableScan.getInt("block");
        int id = currTableScan.getInt("id");
        return new RID(blknum, id);
    }

    //needs split code
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
        while (next())
            if (getDataRid().equals(datarid)) {
                currTableScan.delete();
                //decrement number of free space
                TableScan hashTblScan = new TableScan(hashTbl, tx);
                hashTblScan.beforeFirst();
                while (hashTblScan.next()) {
                    String bucket = convertToHash(dataval, globalDepth);
                    String tblname = idxname + bucket;
                    if (hashTblScan.getString("pointerTable").equals(tblname)) {
                        int currentFreeSpace = hashTblScan.getInt("freeSpots");
                        hashTblScan.setInt("freeSpots",currentFreeSpace - 1);
                        return; //exit early to save time
                    }
                }
            }
    }

    //done
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

    /**
     * Scans through hash table looking for metadata on global depth.
     * This is stored in it's own record  with a freeSpots value of the global depth
     * and a pointerTable value of "globalDepth"
     */
    private void setGlobalDepth() {
        //First check to see if metadata record with global depth exists.
        TableScan hashTblScan = new TableScan(hashTbl, tx);
        hashTblScan.beforeFirst();
        while (hashTblScan.next()) {
            if (hashTblScan.getString("pointerTable").equals("globalDepth")) {
                globalDepth = hashTblScan.getInt("freeSpots");
                return;
            }
        }
        //reached end of hash table without finding metadata record. This means table is new and needs one
        hashTblScan.beforeFirst();
        hashTblScan.insert();
        hashTblScan.setInt("freeSpots", 1); //set global depth to be 1
        hashTblScan.setString("pointerTable", "globalDepth");
        globalDepth = 1;
    }
}