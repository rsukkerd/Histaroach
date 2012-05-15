Histaroach project
==================

The Histaroach project contains a number of useful executables that
allow one to peruse distributed source control repositories to
identify parallel bug fixes, and perform bug localization by using
repository history. To reach the entire group, and to subscribe to
changes in this repository, email/visit the project's Google groups
page: http://groups.google.com/group/histaroach

This README will explain how to (1) set up the project in Eclipse, (2)
build the project and run its tests from the command line, and (3) how
to use the compiled utilities.


Setting up the project in Eclipse
----------------------------------

TODO


Using the command line
-----------------------

For TestIsolationDataGenerator:
 * -p : Project name. Example: voldemort.
 * -r : Project repository path. Example: /Users/Name/Project/voldemort/
 * -b : Project build command. Example: ant
 * -s : Start commit ID (the latest commit in the range), at least 7-character long. Example: ea2c4f0
 * -e : End commit ID (the earliest commit in the range), at least 7-character long. Example: fbd0f95


Using the compiled utilities
-----------------------------

### TestIsolationDataGenerator

TODO


### ParallelBugFixesFinder

TODO