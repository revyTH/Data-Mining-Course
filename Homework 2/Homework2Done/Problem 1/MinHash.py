from Helpers import *

class MinHashSets:
    def __init__(self, collectionShigleDocs, n):

        # Dictionary (key=index of document, value=signature of the document)
        self.minHashSets = {}

        # Generating minhash family functions
        hashFunctions = []
        for i in range(n):
            hashFunctions.append(hashFamily(i))

        # LSH alg
        i = 0
        for shingleDoc in collectionShigleDocs:
            j = 0
            for hashFunc in hashFunctions:
                shingles = set()
                for shingle in shingleDoc.shingles:
                    shingles.add(hashFunc(str(shingle)))

                if j == 0:
                    self.minHashSets[i] = [min(shingles)]
                else:
                    self.minHashSets[i].append(min(shingles))
                j = j + 1
            i = i + 1




