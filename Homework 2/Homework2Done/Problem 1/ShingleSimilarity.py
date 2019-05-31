from Helpers import jaccardSim

class ShingleSimilarity:
    def __init__(self,shingleDocuments):

        self.neighboursPairs = set()
        self.nearDuplicates = set()

        for i in range(len(shingleDocuments)):
            shingleDoc1 = shingleDocuments[i]
            for j in range (i+1, len(shingleDocuments)):
                #if (j != i):
                shingleDoc2 = shingleDocuments[j]
                if jaccardSim(shingleDoc1.shingles, shingleDoc2.shingles) >= 0.8:
                    pair = (i,j)
                    self.neighboursPairs.add(pair)

        # remove symmetric pairs
        self.neighboursPairs = set((a,b) if a<=b else (b,a) for a,b in self.neighboursPairs)

        # select near-duplicates
        for pair in self.neighboursPairs:
            duplicateKey = pair[1]
            self.nearDuplicates.add(duplicateKey)