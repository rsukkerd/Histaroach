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
from copy import deepcopy

class Delta:
    def __init__(self, fieldString):
        fields = fieldString.strip().split(";")
        self.parentID = fields[0]
        self.childID = fields[1]
        files = []
        for f in fields[2].split(","):
            files.append(ChangedFile(f[1:], f[0]))
        self.totalDelta = files

    def __str__(self):
        print self.parentID + ":" + self.childID + "\n" + str(self.totalDelta)

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

    def __eq__(self, other):
        return self.fileName == other.fileName and self.changeType == other.changeType

    def __ne__(self, other):
        return not self.__eq__(other)

class TestResult:
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
    
    def is_flip(self):
        return self.parentResult == 1 and self.childResult == 0

class MixedRevision:

    def __init__(self, mixID):
        self.mixID = mixID
        self.revertedFiles = []
        self.tests = []
        self.compilable = True

    def __str__(self):
        s = "Mixed Revision: " + str(self.mixID) + "\nChanged files (" + str(len(self.revertedFiles)) + "):\n"  
        for  f in self.revertedFiles :
            s = s + "    " + str(f) + "\n"
        if ( self.is_repaired() ):
            s = s + "Fixed flips:\n"
            for f in self.get_fixed_flips():
                s = s + f + "\n"
        return s

    def is_repaired(self):
        tests_fixed = []
        tests_broken = []
        for t in self.tests:
            if t.repairs_flip(): tests_fixed.append(t)
            if t.breaks(): tests_broken.append(t)
        return len(tests_fixed) > 0 and len(tests_broken) == 0

    def get_fixed_flips(self):
        flips = []
        for t in self.tests:
            if ( t.repairs_flip() ): flips.append(t.testName)
        return flips

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
        
    def get_broken_mixes(self):
        '''
        Returns a list of all mixes that don't fix the flip
        '''
        broken = []
        repairs = self.get_repairs()
        for t in self.mixedRevisions:
            if ( not repairs.__contains__(t) ): broken.append(t)
        return broken

    def is_repaired(self):
        return len(self.get_repairs()) > 0

    def get_delta_p_bar(self, delta):
        '''
        Returns the list of mixed revisions that are in delta_p, but the list of files is the complement of delta_p
        '''
        delta_p = self.get_delta_p()
        delta_p_bar = []
        for d in delta_p:
            d_bar = MixedRevision(d.mixID)
            d_bar.tests = d.tests
            d_bar.compilable = d.compilable
            delta_copy = deepcopy(delta.totalDelta) 
            for i in range(len(d.revertedFiles)):
                for j in range(len(delta_copy)):
                    if ( d.revertedFiles[i].fileName == delta_copy[j].fileName): 
                        delta_copy.remove(delta_copy[j]) 
                        break
            d_bar.revertedFiles = delta_copy
            delta_p_bar.append(d_bar)
        return delta_p_bar
                    

    def get_delta_p(self):
        '''
        Returns a list of all mixed revisions that have the maximum length delta_p
        '''
        longest = 0
        delta_p = []
        for r in self.get_repairs():
            temp = len(r.revertedFiles)
            if ( temp == longest ): delta_p.append(r)
            if ( temp > longest ):
                longest = temp
                delta_p = [r]
        return delta_p

    def get_delta_f(self, delta):
        shortest = len(delta.totalDelta)
        delta_f = []
        for r in self.get_broken_mixes():
            temp = len(r.revertedFiles)
            if ( temp == shortest ): delta_f.append(r)
            if ( temp < shortest ):
                shortest = temp
                delta_f = [r]
        return delta_f

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
            revPairLines = [lineitems]
            #print "Found new IDs " + parentID + ", " + childID
    data.append( build_rev_pair(parentID, childID, revPairLines) )
    return data

def get_num_mixes(data):
    i = 0
    for d in data:
        i = i + len(d.mixedRevisions)
    return i

def get_all_mixes(data):
    mixes = []
    for r in data:
        mixes.extend(r.mixedRevisions)
    return mixes

def check_all_revs(mixes):
    last = - 1
    for m in mixes:
        if ( m.mixID != last + 1 ): print m.mixID - 1
        last = m.mixID

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
    print "\tRepaired flips: " + str(get_repaired_flips(data))
    print "\n"

def print_flips(rev_pair):
    print "Flipped tests:"
    for t in rev_pair.get_repairs()[0].tests:
        if ( t.is_flip() ):
            print t.testName

def print_fix(rev_pair, mixes, deltas):
    '''
    Prints summary info about this fixed revision pair and set of deltas
    '''
    s = "\nRevision pair: " + rev_pair.parentID + ":" + rev_pair.childID 
    s = s + "\tTotal delta: " 
    s = s + str(len(get_delta(deltas, rev_pair.parentID, rev_pair.childID).totalDelta)) + " files"
    print s
    print_flips(rev_pair)
    print ""
    for f in mixes:
        print f

def print_delta_p_bar(rev_pairs, deltas):
    print "Delta P bar"
    print "-----------"
    for d in rev_pairs:
        if d.is_repaired(): print_fix(d, d.get_delta_p_bar( get_delta(deltas, d.parentID, d.childID ) ), deltas )
    print ""

def print_delta_p(rev_pairs, deltas):
    print "Delta P"
    print "-------"
    for d in rev_pairs:
        if d.is_repaired(): print_fix(d, d.get_delta_p(), deltas )
    print ""

def print_delta_f(data, deltas):
    print "Delta F"
    print "-------"
    for d in data:
        if d.is_repaired(): print_fix(d, d.get_delta_f( get_delta(deltas, d.parentID, d.childID) ), deltas )
    print ""

def print_comparison(data, deltas):
    '''
    Compares delta_p_bar and delta_f and prints if it finds discrepancies
    '''
    print "\nDelta F - Delta P bar Mismatches"
    print "--------------------------------\n"
    for d in data:
        delta = get_delta(deltas, d.parentID, d.childID)
        p_bar = d.get_delta_p_bar(delta)
        f = d.get_delta_f(delta)
        in_f_not_p = []
        in_p_not_f = []
        printed_header = False
        printed_mix = False
        for m_p in p_bar:
            for m_f in f:
                for cf in m_f.revertedFiles:
                    if ( not m_p.revertedFiles.__contains__(cf) ):
                        in_f_not_p.append(cf)
                for cf in m_p.revertedFiles:
                    if( not m_f.revertedFiles.__contains__(cf) ):
                        in_p_not_f.append(cf)
                if ( len(in_p_not_f) > 0  or len(in_f_not_p) > 0 ):
                    if ( not printed_header ):
                        print ""
                        print d
                        printed_header = True
                    if ( not printed_mix ):
                        print "\nMixes (P/F): " + str(m_p.mixID) + "/" + str(m_f.mixID)
                        printed_mix = True
                    if ( len(in_p_not_f) > 0 ):
                        print "Files in Delta P bar that are missing in Delta F:"
                        for f_ in in_p_not_f: print f_
                        printed_mix = False
                    if ( len(in_f_not_p) > 0):
                        print "Files in Delta F that are missing in Delta P bar:"
                        for f_ in in_f_not_p: print f_
                        printed_mix = False
                in_f_not_p = []
                in_p_not_f = []
    return

def read_deltas(filename):
    df = open(filename, "r")
    #skip header
    df.next()
    deltas = []
    for line in df:
        delta = Delta(line)
        #print delta
        deltas.append( delta )
    return deltas

def get_delta(deltas, parent, child):
    for delta in deltas:
        if ( delta.parentID == parent and delta.childID == child): return delta
    return None

def parse_arguments():
    '''
    Parse commandline arguments and return an object containing 
    all values
    '''
    parser = argparse.ArgumentParser()
    parser.add_argument( "-f", dest="INPUT_FILE", required=True, 
        help="The prefix of the Histaroach output files to be processed")
    parser.add_argument( "--delta-p-bar", dest="DELTA_P_BAR", default=False, action='store_true')
    parser.add_argument( "--delta-p", dest="DELTA_P", default=False, action='store_true')
    parser.add_argument( "--delta-f", dest="DELTA_F", default=False, action='store_true')
    parser.add_argument( "--summary", dest="SUMMARY", default=False, action='store_true')
    parser.add_argument( "--compare", dest="COMPARE", default=False, action='store_true')
    return parser.parse_args()

def main():
    args = parse_arguments()
    infile = open(args.INPUT_FILE + ".txt", "r")
    data = read_data(infile)
    deltas = read_deltas( args.INPUT_FILE + "_totalDelta.txt")
    #produce requested output
    if ( args.SUMMARY or args.DELTA_P_BAR or args.DELTA_P):
        print_summary(data)
    if ( args.COMPARE ):
        print_comparison(data, deltas)
    if ( args.DELTA_P ):
        print_delta_p(data, deltas)
    if ( args.DELTA_P_BAR ):
        print_delta_p_bar(data, deltas)
    if ( args.DELTA_F ):
        print_delta_f(data, deltas)
    return

if __name__ == "__main__":
    main()
