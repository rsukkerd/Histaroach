#!/usr/bin/python
#
# Script: pre-process.py
# Purpose: Processes a histaroach output file to extract relevant
#          (statistical) data about flips and fixes.
#          It produces a number of on-screen reports and files for
#          further processing in stats packages.
# Author: Jochen Wuttke, wuttkej@gmail.com
# Date: 2012-06-15
#
# Version: 1
#
# Usage: pre-process.py -f historaoch.output [output options]
# Output options:
# --summary     prints a short summary of the processed file, does not write
#               any data files

import argparse

class ChangedFile:
    fileName = ""
    changeType = "~"

    def toString(self, c):
        if ( c == "~" ): return "MODIFY"
        if ( c == "+" ): return "ADD"
        if ( c == "-" ): return "DELETE"
        return "UNDEFINED"


    def __init__(self, name, ctype):
        self.fileName = name
        self.changeType = ctype

    def __str__(self):
        return self.fileName + ": " + self.toString(self.changeType)

class TestResult:
    testName = ""
    parentResult = 1
    childResult = 1
    mixResult = 1

    def resultToString(self, result):
        if ( result == 1 ):
            return "pass"
        elif ( result == 0 ):
            return "fail" 

    def __init__(self, name, pRes, cRes, mRes):
        self.testName = name
        self.parentResult = pRes
        self.childResult = cRes
        self.mixResult = mRes

    def __str__(self):
        return "Test: " + self.testName + " Parent: " + self.resultToString(self.parentResult) + " Child: " + self.resultToString(self.childResult) + " Mix: " + self.resultToString(self.mixResult)

    def repairs_flip(self):
        return self.parentResult == 1 and self.childResult == 0 and self.mixResult == 1

    def breaks(self):
        return self.mixResult == 0 and self.parentResult == 1 and self.childResult == 1
    

class MixedRevision:
    mixID = -1
    changedFiles = []
    tests = []

    def __init__(self, mixID):
        self.mixID = mixID
        self.changedFiles = []
        self.tests = []

    def __str__(self):
        s = "Mixed Revision: " + str(self.mixID) + "\nChanged files: "  
        for  f in self.changedFiles :
            s = s + str(f) + ", "
        return s

    def is_repaired(self):
        tests_fixed = []
        tests_broken = []
        for t in self.tests:
            if t.repairs_flip(): tests_fixed.append(t)
            if t.breaks(): tests_broken.append(t)
        return len(tests_fixed) > 0 and len(tests_broken) == 0

class RevisionPair:
    parentID = ""
    childID = ""
    mixedRevisions = []
    '''
    Initialized a new RevisionPair object with 
    revision IDs
    '''
    def __init__(self, parentID, childID):
        self.parentID = parentID
        self.childID = childID
        self.mixedRevisions = []

    def __str__(self):
        return "Revision Pair: " + self.parentID + ", " + self.childID + "\tMixed Revisions: " + str(len(self.mixedRevisions))

    def is_repaired(self):
        repairs = []
        for t in self.mixedRevisions:
            if t != None and t.is_repaired(): repairs.append(t)
        return len(repairs) > 0

def init_mix(mix,line):
    fields = line.split(';')
    for f in fields[3].split(','):
        mix.changedFiles.append( ChangedFile( f[1:], f[0] ))

def build_mix( mixid, lines):
    if (len(lines) == 0 ): return
    mix = MixedRevision( mixid )
    init_mix(mix, lines[0])
    for line in lines:
        fields = line.split(";")
        mix.tests.append( TestResult(fields[6], int(fields[9]), int(fields[8]), int(fields[7]) ) )
    return mix

def build_rev_pair(parent, child, lines):
    #print "Building rev pair for input string \n" + string
    mixID = -1
    mixStrings = []
    revPair = RevisionPair(parent, child)
    #print revPair
    for line in lines:
        #print "Processing line " + line
        fields = line.split(';')
        #print fields
        if (mixID == -1):
            mixID = int(fields[0])
            #print "Found new mix " + fields[0] + " for revs " + parent + ", " + child
        if ( mixID == int(fields[0]) ):
            #collect data for the same mix
            mixStrings.append(line)
        else:
            #create a new mixed rev
            revPair.mixedRevisions.append( build_mix(mixID, mixStrings) )
            mixID = int(fields[0])
            mixStrings = [] 
            #print "Found new mix " + fields[0] + " for revs " + parent + ", " + child
    revPair.mixedRevisions.append( build_mix(mixID, mixStrings) )
    return revPair

def read_data(inputfile):
    data =  []#data to be returned, a list of RevisionPair objects

    #internal tracking data
    parentID = None
    childID = None
    revPairStrings = []
    #skip header line
    inputfile.next()
    for line in inputfile:
        items = line.split(';')
        if ( parentID == None and childID == None ):
            #this should happen only when we read the first line
            parentID = items[2]
            childID = items[1]
            #print "Found new IDs " + parentID + ", " + childID
        if ( parentID == items[2] and childID == items[1] ):
            #collect the line belonging to the same rev pair
            revPairStrings.append(line)
        else:
            #create an object from the collected string and start a new collection
            data.append( build_rev_pair(parentID, childID, revPairStrings) )
            parentID = items[2]
            childID = items[1]
            revPairStrings = []
            #print "Found new IDs " + parentID + ", " + childID
    data.append( build_rev_pair(parentID, childID, revPairStrings) )
    return data

def get_num_mixes(data):
    i = 0
    for d in data:
        i = i + len(d.mixedRevisions)
    return i

def get_repaired_flips(data):
    i = 0
    for d in data:
        if d.is_repaired(): i = i + 1
    return i

def print_summary(data):
    print "\nHistaroach Log file summary\n"
    print "Checked revision pairs: " + str(len(data)) + "\tTotal number of mixed Revisions: " + str(get_num_mixes(data))
    print "Repaired flips: " + str(get_repaired_flips(data))
    print "\n"

def parse_arguments():
    '''
    Parse commandline arguments and return an object containing 
    all values
    '''
    parser = argparse.ArgumentParser()
    parser.add_argument( "-f", dest="INPUT_FILE", required=True, 
        help="The Histaroach output file to be processed")
    #parser.add_argument( "--list-fixes", dest="LIST_FIXES")
    parser.add_argument( "--summary", dest="SUMMARY", default=False, action='store_true')
    return parser.parse_args()

def main():
    args = parse_arguments()
    infile = open(args.INPUT_FILE, "r")
    data = read_data(infile)
    #produce requested output
    if ( args.SUMMARY):
        print_summary(data)
    return

if __name__ == "__main__":
    main()
