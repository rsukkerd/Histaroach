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
    compilable = True

    def __init__(self, mixID):
        self.mixID = mixID
        self.changedFiles = []
        self.tests = []
        self.compilable = True

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

    def get_repairs(self):
        repairs = []
        for t in self.mixedRevisions:
            if t.is_repaired(): repairs.append(t)
        return repairs
        
    def get_all_files(self):
        files = []
        for m in self.mixedRevisions:
            if ( m == None): continue
            for f in m.changedFiles:
                if (not files.__contains__(f.fileName) ): files.append(f.fileName)
        return files
    
    def is_repaired(self):
        return len(self.get_repairs()) > 0

    def get_delta_p_bar(self):
        '''
        Returns the list of fixed mixed revisions with the fewest files
        '''
        shortest = len(self.get_all_files()) - 1 
        smallest = []
        for r in self.get_repairs():
            temp = len(r.changedFiles)
            if ( temp == shortest ):
                smallest.append(r)
            if (temp < shortest):
                shortest = temp
                smallest = [r]
        return smallest

    def get_delta_p(self):
        longest = 0
        delta_p = []
        for r in self.get_repairs():
            temp = len(r.changedFiles)
            if ( temp == longest ): delta_p.append(r)
            if ( temp > longest ):
                longest = temp
                delta_p = [r]
        return delta_p

    def get_delta_f(self):
        return []

    def get_delta_f_bar(self):
        return []
            
def init_mix(mix,line):
    fields = line.split(';')
    for f in fields[3].split(','):
        mix.changedFiles.append( ChangedFile( f[1:], f[0] ))

def build_mix( mixid, lines):
    if (len(lines) == 0 ): 
        print "Empty lines for mixid " + str(mixid); 
        return
    mix = MixedRevision( mixid )
    init_mix(mix, lines[0])
    for line in lines:
        fields = line.split(";")
        if ( int(fields[4]) == 1 ):
            mix.compilable = True
            mix.tests.append( TestResult(fields[6], int(fields[9]), int(fields[8]), int(fields[7]) ) )
        else:
            mix.compilable = False
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
        #if ( int(fields[0]) == 19 ): print fields
        #print fields
        if (mixID == -1):
            mixID = int(fields[0])
            #print "Found new mix " + fields[0] + " for revs " + parent + ", " + child
        if ( mixID == int(fields[0]) ):
            #collect data for the same mix
            #if ( mixID == 19 ): print "appending for 19"
            mixStrings.append(line)
        else:
            #create a new mixed rev
            #print "Building mix rev " + str(mixID) + " with " + str(len(mixStrings)) + " lines"
            revPair.mixedRevisions.append( build_mix(mixID, mixStrings) )
            mixID = int(fields[0])
            mixStrings = [line] 
            #print "Found new mix " + fields[0] + " for revs " + parent + ", " + child
    #print "Building mix rev " + str(mixID) + " with " + str(len(mixStrings)) + " lines"
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
    '''
    Returns the number of repaired flips given a list of revision pairs
    '''
    i = 0
    for d in data:
        if d.is_repaired(): i = i + 1
    return i

def print_summary(data):
    print "\nHistaroach Log file summary\n"
    print "Checked revision pairs: " + str(len(data)) + "\tTotal number of mixed Revisions: " + str(get_num_mixes(data))
    print "Repaired flips: " + str(get_repaired_flips(data))
    print "\n"

def print_fix(rev_pair, deltas):
    '''
    Prints summary info about this fixed revision pair and set of deltas
    '''
    print "\nRevision pair: " + rev_pair.parentID + ":" + rev_pair.childID + "\tDelta size: " + str(len(deltas[0].changedFiles))
    for f in deltas:
        print f

def print_delta_p_bar(data):
    print "Delta P bar"
    print "-----------"
    for d in data:
        if d.is_repaired(): print_fix(d, d.get_delta_p_bar() )

def parse_arguments():
    '''
    Parse commandline arguments and return an object containing 
    all values
    '''
    parser = argparse.ArgumentParser()
    parser.add_argument( "-f", dest="INPUT_FILE", required=True, 
        help="The Histaroach output file to be processed")
    parser.add_argument( "--delta-p-bar", dest="DELTA_P_BAR", default=False, action='store_true')
    parser.add_argument( "--summary", dest="SUMMARY", default=False, action='store_true')
    return parser.parse_args()

def main():
    args = parse_arguments()
    infile = open(args.INPUT_FILE, "r")
    data = read_data(infile)
    #produce requested output
    if ( args.SUMMARY or args.DELTA_P_BAR):
        print_summary(data)
    if ( args.DELTA_P_BAR ):
        print_delta_p_bar(data)
    return

if __name__ == "__main__":
    main()
