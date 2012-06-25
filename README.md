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

TODO


Using the compiled utilities
-----------------------------

### DataCollector

Command line usage: DataCollector [mode option] [common options] [HistoryGraph/IntermediateRevision options]

Mode Options:
 * --phaseI                                - Collect HistoryGraph data
 * --phaseII                               - Create IntermediateRevisions
 * --phaseIII                              - Run tests on IntermediateRevisions
 
Common Options:
 * -p --projectName                        - Project name
 * -r --repoDir                            - Repository directory
 * -b --buildCommand                       - Build command (Optional) [default ant]
 
HistoryGraph Options:
 * -s --startCommitID                      - Starting commit ID for HistoryGraph analysis
 * -e --endCommitID                        - Ending commit ID for HistoryGraph analysis

IntermediateRevision Options:
 * -c --clonedRepoDir                      - Cloned repository directory
 * -H --historyGraphXML                    - HistoryGraph xml file
 
IntermediateRevision (run tests) Options:
 * -I --intermediateRevisionXML            - IntermediateRevision xml file
 * -i --startIndex                         - Index of IntermediateRevision to begin analysis (Optional)
 * -n --numIntermediateRevisions           - Number of IntermediateRevisions to analyze (Optional)


### ExploreTestNondeterminism

Command line usage: ExploreTestNondeterminism [options]

General Options:
 * -h --help=<boolean>        - Print a help message [default false]
 * -r --repoDir=<filename>    - Repository directory
 * -v --version=<string>      - Version (ie. commit ID)
 * -b --buildCommand=<string> - Build command (Optional) [default ant]
 * -t --testName=<string>     - Test name


Output File Documentation
--------------------------

### IntermediateRevision Output

File Content:
An IntermediateRevision output file contains a list of IntermediateRevisions and their test results. 
Each IntermediateRevision in this file is created from a pair of parent-child Revisions, which contains 
at least 1 test flip. A test flip can be from pass (in parent) to fail (in child) or vice versa.

File Format:
 * IID              : a unique number for a particular IntermediateRevision
 * parentCommitID   : a parent commit ID
 * childCommitID    : a child commit ID
 * delta            : a set of (file-level) changes that are applied to the parent to create this IntermediateRevision
 * (change type)    : 'A' means the file is added;
                      'D' means the file is deleted;
                      'M' means the file is modified
 * compilable       : 0 means this IntermediateRevision is not compilable; 1 means compilable;
                      if 0, all of the test-related fields are 'n'
 * testAborted      : 0 means the tests terminate OK; 1 means the process that runs the tests has aborted;
                      if 1, all of the test-related fields are 'n'
 * test             : name of a test
 * intermediateTestResult : 0 means this IntermediateRevision fails this test; 1 means passes
 * parentTestResult : 0 means the parent of this IntermediateRevision fails this test; 1 means passes;
                      -1 means the parent does not have this test
 * childTestResult  : 0 means the child of this IntermediateRevision fails this test; 1 means passes