package simpledb.index.ehash;

import static java.sql.Types.INTEGER;
import static simpledb.file.Page.BLOCK_SIZE;
import static simpledb.file.Page.INT_SIZE;

import simpledb.buffer.PageFormatter;
import simpledb.file.Page;
import simpledb.record.TableInfo;


/**
 * Page formatter for E-Hash's pages for buckets and hash table. Makes them all empty.
 * If flag is written as a -1, then it is empty.
 * If flag is 0, then it is a valid hash table entry
 * If flag is 1, then it is a
 */

public class EHashPageFormatter implements PageFormatter {

    private TableInfo ti;
    private int flag;

    public EHashPageFormatter(TableInfo ti, int flag) {
        this.ti = ti;
        this.flag = flag;
    }

    public void format(Page p) {
        p.setInt(0, flag);
        p.setInt(INT_SIZE, 0); //0 entries
        int recordSize = ti.recordLength();
        for (int pos = 2 * INT_SIZE; pos + recordSize <= BLOCK_SIZE; pos += recordSize) {
            makeDefaultRecord(p, pos);
        }
    }

    private void makeDefaultRecord(Page p, int pos) {
        for (String fldname : ti.schema().fields()) {
            int offset = ti.offset(fldname);
            if (ti.schema().type(fldname) == INTEGER)
                p.setInt(pos + offset, 0);
            else
                p.setString(pos + offset, "");
        }
    }
}
