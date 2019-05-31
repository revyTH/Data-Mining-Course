from Helpers import *

class ShingleDocument:
    def __init__(self, doc, shingleSize):
        self.shingles = set()

        if len(doc) <= shingleSize:
            return doc

        # sequences of two or more white spaces will be normalized to one white space
        docProcessed = normalizeWhiteSpaces(doc)

        for i in range(0, len(docProcessed) - (shingleSize-1)):
            shingle = docProcessed[i:i+shingleSize]
            hashedShingle = hash(shingle)
            self.shingles.add(hashedShingle)







