package simpledb.index.ehash;

import simpledb.file.Block;

public class EHashBucket {

    private int hashKey;
    private int localDepth = 1;
    private Block currBlock;

    /**
     * Increases the local depth of the bucket and splits contents of bucket into two blocks.
     * @return reference to new block created
     */
    public Block split() {
        localDepth++;

    }


}
