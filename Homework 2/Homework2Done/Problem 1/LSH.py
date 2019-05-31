from Helpers import *

class LSH:
    def __init__(self, minHashSets, numBands, bandWidth, n):

        self.bandsArray = []
        self.candidatePairs = []
        self.nearDuplicates = set()

        start = 0
        for b in range(numBands):
            newDic = {}
            self.bandsArray.append(newDic)
            if (b != 0):
                start += bandWidth

            for key in minHashSets:
                signature = minHashSets[key]
                sliceSignature = signature[start:start+bandWidth]
                sliceSignatureConcat = ""

                for minhash in sliceSignature:
                    sliceSignatureConcat += minhash
                    currentDictionary = self.bandsArray[b]
                hashBucketKey = hash(sliceSignatureConcat)

                if not currentDictionary.has_key(hashBucketKey):
                    currentDictionary[hashBucketKey] = []
                    currentDictionary[hashBucketKey].append(key)
                else:
                    for documentKey in currentDictionary[hashBucketKey]:
                        candidatePair = (key,documentKey)
                        self.candidatePairs.append(candidatePair)
                    currentDictionary[hashBucketKey].append(key)

        # remove duplicate pairs
        self.candidatePairs = set((a,b) if a<=b else (b,a) for a,b in self.candidatePairs)

        # select near-duplicates
        for candidatePair in self.candidatePairs:
            duplicateKey = candidatePair[1]
            self.nearDuplicates.add(duplicateKey)


