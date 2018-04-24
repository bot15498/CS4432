Task 1:
-Added index type field to CreateIndexData to track which index to use.
-changed uplanner to IndexUpatePlanner in SimpleDB
-IndexUpdatePlanner is where indexes are updated.
-IndexMgr updated to save index type in schema, and is called when creating the index info.

Task 2:
-static hash and B-Tree already exist

Methodology for EHash:
-Store buckets as own tables with name of hahsed value.
-When tables split, split data into two new tables.
--Database doesn't seem to have functionality to drop tables, so tables can't be deleted. This is the best possible based on this project.
-Have upper level table called hash table
--holds names of tables (as a pointer)
ASSUMPTIONS:
-Max bucket size is greater than number of duplicates possible.

