package simpledb.index.ehash;

import simpledb.file.Block;
import simpledb.index.Index;
import simpledb.query.Constant;
import simpledb.record.RID;
import simpledb.record.Schema;
import simpledb.record.TableInfo;
import simpledb.tx.Transaction;

public class ExtensibleHashIndex implements Index {

    private String idxname;
    private Schema sch;
    private Transaction tx;
    private int globalHashDepth = 1;
    private EHashBucket bucket;
    private TableInfo hashTableTi, bucketTi;
    private Block topBlock; //top block in hash table

    public ExtensibleHashIndex(String idxname, Schema sch, Transaction tx) {
        this.idxname = idxname;
        this.sch = sch;
        this.tx = tx;

        //TableInfo for buckets:
        String bucketTable = idxname + "bucket";
        bucketTi = new TableInfo(bucketTable, sch);
        if (tx.size(bucketTi.fileName()) == 0) {
            tx.append(bucketTi.fileName(), new EHashPageFormatter(bucketTi, -1)); //initialize all pages to be empty
        }

        //Table info for upper level hash table.
        String hashTable = idxname + "ehashtbl";
        Schema hashTableSch = new Schema();
        hashTableSch.add("block", sch);
        hashTableSch.add("dataval", sch);
        hashTableTi = new TableInfo(hashTable, hashTableSch);
        topBlock = new Block(hashTableTi.fileName(), 0);
        if (tx.size(hashTableTi.fileName()) == 0) {
            //If
            tx.append(hashTableTi.fileName(), new EHashPageFormatter(hashTableTi, 0));
        }
    }

    public void beforeFirst(Constant searchkey) {

    }

    public boolean next() {
        return false;
    }

    public RID getDataRid() {
        return null;
    }

    public void insert(Constant dataval, RID datarid) {

    }

    public void delete(Constant dataval, RID datarid) {

    }

    public void close() {

    }
}
