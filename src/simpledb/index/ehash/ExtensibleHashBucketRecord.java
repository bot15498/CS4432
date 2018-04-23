package simpledb.index.ehash;

import simpledb.query.Constant;

public class ExtensibleHashBucketRecord {

    private int block;
    private int id;
    private Constant dataval;

    public ExtensibleHashBucketRecord(int block, int id, Constant dataval) {
        this.block = block;
        this.id = id;
        this.dataval = dataval;
    }

    public int getBlock() {
        return block;
    }

    public int getId() {
        return id;
    }

    public Constant getDataval() {
        return dataval;
    }
}
