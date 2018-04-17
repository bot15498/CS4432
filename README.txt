Task 1:
-Added index type field to CreateIndexData to track which index to use.
-changed uplanner to IndexUpatePlanner in SimpleDB
-IndexUpdatePlanner is where indexes are updated.
-IndexMgr updated to save index type in schema, and is called when creating the index info.

Task 2:
-static hash and B-Tree already exist
-
-TableInfo for "hash" part of index
-New class for each bucket. Bucket's hold a table scan.
