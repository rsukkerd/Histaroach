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

### DataCollector

TODO


Output File Documentation
--------------------------

### MixedRevision Output

File Content:
A MixedRevision output file contains a list of MixedRevisions and their test results. Each MixedRevision 
in this file is created from a pair of child-parent Revisions, which contains at least 1 test flip. 
A test flip can be from pass (in parent) to fail (in child) or vice versa.

File Format:
 * mixedRevisionID : a unique number for a particular MixedRevision
 * baseRevisionID  : a child commit ID
 * otherRevisionID : a parent commit ID
 * revertedFiles   : a set of files that are reverted from child version to parent version
 * (reverted type) : + means the file (from parent) is added to child;
                     - means the file is removed from child;
                     ~ means the file is replaced with the parent version of itself
 * compilable      : 0 means this MixedRevision is not compilable; 1 means compilable;
                     if 0, all of the test-related fields are 'n'
 * testAborted     : 0 means the tests terminate OK; 1 means the process that runs the tests has aborted
 * test            : name of a test
 * mixedTestResult : 0 means this MixedRevision fails this test; 1 means passes
 * baseTestResult  : 0 means the child of this MixedRevision fails this test; 1 means passes
 * otherTestResult : 0 means the parent of this MixedRevision fails this test; 1 means passes