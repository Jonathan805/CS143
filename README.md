# CS143
Lab 2  Report 
Jonathan Ong  104135858
Quanjie Geng	204160668	

Design based off of parameters and algorithms mentioned in textbook.  Most of aggregate design coded to fit unit tests and system tests.
Code was briefly noted during the first week, and coding began during the second week after the midterm.  
Not many difficulties until the aggregates.  They required familiarity with the rest of the project's constants. Also had to sort out the differnt data structures and cases for grouping and not grouping.

This Lab was completed and submitted on time, February 11, 2015.  Therefore, no flex days were consumed and the team members have 3 each.



~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Lab 1 report
No changes to APIs.
Design based off of paramters and return types of functions implemented.
Ample time was spent familiarizing ourselves with the lab and the flow of the program.  Difficulties were had when testing our implementation on the VMs. Each night, a file or two were implemented.  

We ran into many problems from the beginning: iterators not working, tuples not being converted to strings and many other small things.  When we ran our system tests and the classes we wrote started interacting with each other, there were two main bugs.  When we read from file, we originally believed that the offset parameter offset the pointer to the file, and we ran into problems with what was contained in each page file generated.  THe other bug was when we started comparing larger tests; our hash function didn't work as intended and we mistakingly hashed pages to collide, causing tuples to appear unexpectedly.

Because this lab was turned in 1 day late, 1 flex day was used for both Jonathan and Quanjie.  Of the 4 total flex days, both have 3 flex days left.
