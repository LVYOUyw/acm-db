The page eviction policy I used is First In First Out. In the beginning, I used the Random policy. 

However, using this policy is not easy to debug since it is not deterministic.

For split operation, just build a new entry with the "middle" key in the old page and "exchange"

this new entry with its parent entry.  Merge operation is almost doing the same, just reverse.

I take about 4 days to finish this lab, 2 days to know how the code implement the B+ tree.