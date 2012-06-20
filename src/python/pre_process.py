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

class InputFileLine:
    def __init__(self, fields):
        if (len(fields) != 10 ):
            print "Invalid inputs " + fields
            return
        self.mixID = int(fields[0])
        self.parentID = fields[1]
        self.childID = fields[2]
        self.changedFiles = fields[3]
        self.compilable = (int(fields[4]) == 1)
        self.aborted = (int(fields[5]) == 1)
        self.testName = fields[6]
        self.mixResult = fields[7]
        self.parentResult = fields[8]
        self.childResult = fields[9].strip()


class ChangedFile:
    fileName = ""
    changeType = "~"

    def toString(self, c):
        if ( c == "M" ): return "MODIFY"
        if ( c == "A" ): return "ADD"
        if ( c == "D" ): return "DELETE"
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
    revertedFiles = []
    tests = []
    compilable = True

    def __init__(self, mixID):
        self.mixID = mixID
        self.revertedFiles = []
        self.tests = []
        self.compilable = True

    def __str__(self):
        s = "Mixed Revision: " + str(self.mixID) + "\nReverted files: "  
        for  f in self.revertedFiles :
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
        
    '''
    Returns a list of all mixes that don't fix the flip
    '''
    def get_broken_mixes(self):
        broken = []
        repairs = self.get_repairs()
        for t in self.mixedRevisions:
            if ( not repairs.__contains__(t) ): broken.append(t)
        return broken

    def get_all_files(self):
        files = []
        for m in self.mixedRevisions:
            if ( m == None): continue
            for f in m.revertedFiles:
                if (not files.__contains__(f.fileName) ): files.append(f.fileName)
        return files

    def get_delta(self, mixedRevision):
        '''
        Returns all the files that have been changed between parent and child
        '''
        all_changes = []
        for m in mixedRevisions:
            for f in m.revertedFiles:
                if ( not all_changes.__contains__(f) ): all_changes.append(f)
        delta = []
        for f in all_changes:
            if ( not m.revertedFiles.__contains__(f) ) : delta.append(f)
        return delta
    
    def is_repaired(self):
        return len(self.get_repairs()) > 0

    def get_delta_p_bar(self):
        '''
        Returns the list of fixed mixed revisions with the fewest files
        '''
        shortest = len(self.get_all_files()) - 1 
        smallest = []
        for r in self.get_repairs():
            temp = len(r.revertedFiles)
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
            temp = len(r.revertedFiles)
            if ( temp == longest ): delta_p.append(r)
            if ( temp > longest ):
                longest = temp
                delta_p = [r]
        return delta_p

    def get_delta_f(self):
        shortest = len(self.get_all_files())-1
        delta_f = []
        for r in self.get_broken_mixes():
            temp = len(r.revertedFiles)
            if ( temp == shortest ): delta_f.append(r)
            if ( temp < shortest ):
                shortest = temp
                delta_f = [r]
        return delta_f

    def get_delta_f_bar(self):
        return []
            
def init_mix(mix,line):
    for f in line.changedFiles.split(','):
        mix.revertedFiles.append( ChangedFile( f[1:], f[0] ))

def build_mix( mixid, lines):
    #if (len(lines) == 0 ): 
    #    print "Empty lines for mixid " + str(mixid); 
    #    return
    mix = MixedRevision( mixid )
    init_mix(mix, lines[0])
    for line in lines:
        if ( line.compilable ):
            mix.compilable = True
            mix.tests.append( TestResult(line.testName, int(line.parentResult), int(line.childResult), int(line.mixResult) ) )
        else:
            mix.compilable = False
    return mix

def build_rev_pair(parent, child, lines):
    #print "Building rev pair for input string \n" + string
    mixID = -1
    mixLines = []
    revPair = RevisionPair(parent, child)
    #print revPair
    for line in lines:
        #print "Processing line " + line
        #fields = line.split(';')
        #if ( int(fields[0]) == 19 ): print fields
        #print fields
        if (mixID == -1):
            mixID = line.mixID
            #print "Found new mix " + fields[0] + " for revs " + parent + ", " + child
        if ( mixID == line.mixID ):
            #collect data for the same mix
            #if ( mixID == 19 ): print "appending for 19"
            mixLines.append(line)
        else:
            #create a new mixed rev
            #print "Building mix rev " + str(mixID) + " with " + str(len(mixStrings)) + " lines"
            revPair.mixedRevisions.append( build_mix(mixID, mixLines) )
            mixID = line.mixID
            mixLines = [line] 
            #print "Found new mix " + fields[0] + " for revs " + parent + ", " + child
    #print "Building mix rev " + str(mixID) + " with " + str(len(mixStrings)) + " lines"
    revPair.mixedRevisions.append( build_mix(mixID, mixLines) )
    return revPair

def read_data(inputfile):
    data =  []#data to be returned, a list of RevisionPair objects

    #internal tracking data
    parentID = None
    childID = None
    revPairLines = []
    #skip header line
    inputfile.next()
    for line in inputfile:
        items = line.split(';')
        lineitems = InputFileLine(items)
        if ( parentID == None and childID == None ):
            #this should happen only when we read the first line
            parentID = lineitems.parentID
            childID = lineitems.childID
            #print "Found new IDs " + parentID + ", " + childID
        if ( parentID == lineitems.parentID and childID == lineitems.childID ):
            #collect the line belonging to the same rev pair
            revPairLines.append(lineitems)
        else:
            #create an object from the collected string and start a new collection
            data.append( build_rev_pair(parentID, childID, revPairLines) )
            parentID = lineitems.parentID
            childID = lineitems.childID
            revPairLines = []
            #print "Found new IDs " + parentID + ", " + childID
    data.append( build_rev_pair(parentID, childID, revPairLines) )
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
    print "\nRevision pair: " + rev_pair.parentID + ":" + rev_pair.childID + "\tFiles changed: " + str(len(rev_pair.get_all_files())) + "\tDelta size: " + str(len(deltas[0].revertedFiles))
    for f in deltas:
        print f

def print_delta_p_bar(data):
    print "Delta P bar"
    print "-----------"
    for d in data:
        if d.is_repaired(): print_fix(d, d.get_delta_p_bar() )
    print ""

def print_delta_p(data):
    print "Delta P"
    print "-------"
    for d in data:
        if d.is_repaired(): print_fix(d, d.get_delta_p() )
    print ""

def print_delta_f(data):
    print "Delta F"
    print "-------"
    for d in data:
        if d.is_repaired(): print_fix(d, d.get_delta_f() )
    print ""

def parse_arguments():
    '''
    Parse commandline arguments and return an object containing 
    all values
    '''
    parser = argparse.ArgumentParser()
    parser.add_argument( "-f", dest="INPUT_FILE", required=True, 
        help="The Histaroach output file to be processed")
    parser.add_argument( "--delta-p-bar", dest="DELTA_P_BAR", default=False, action='store_true')
    parser.add_argument( "--delta-p", dest="DELTA_P", default=False, action='store_true')
    parser.add_argument( "--delta-f", dest="DELTA_F", default=False, action='store_true')
    parser.add_argument( "--summary", dest="SUMMARY", default=False, action='store_true')
    return parser.parse_args()

def main():
    args = parse_arguments()
    infile = open(args.INPUT_FILE, "r")
    data = read_data(infile)
    #produce requested output
    if ( args.SUMMARY or args.DELTA_P_BAR or args.DELTA_P):
        print_summary(data)
    if ( args.DELTA_P_BAR ):
        print_delta_p_bar(data)
    if ( args.DELTA_P ):
        print_delta_p(data)
    if ( args.DELTA_F ):
        print_delta_f(data)
    return

if __name__ == "__main__":
    main()
