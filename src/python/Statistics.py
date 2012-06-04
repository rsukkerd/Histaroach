import os

class Statistics:
    def __init__(self, filename):
        self.data = {}
        f = open(filename, 'r')
        f.readline()
        for line in f:
            entry = line.rstrip().split(';')
            mid = entry[0]
            if mid in self.data:
                self.data[mid].append(entry)
            else:
                entries = []
                entries.append(entry)
                self.data[mid] = entries
            
    def countTestFlipFixes(self):
        count = 0
        for mid in self.data:
            entries = self.data[mid]
            if self.fixedFlip(entries):
                count += 1
        return count
        
    def fixedFlip(self, entries):
        for entry in entries:
            if entry[7] == '1' and entry[8] == '0' and entry[9] == '1':
                return True
        return False
            
def main():
    s = Statistics(os.path.realpath('../../output/mixedRevision_f92c899_4c49cf6.txt'))
    print s.countTestFlipFixes()
    
if __name__ == "__main__":
    main()