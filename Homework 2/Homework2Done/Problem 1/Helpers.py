import hashlib
import re

# regular expression for one or more tabs
spacesRE = re.compile("\s+")


# Format a text document converting the sequences of two ore more whitespaces in one whitespace
def normalizeWhiteSpaces(doc):
    result = re.sub(spacesRE, " ", doc);
    return result


# Implement a family of hash functions. It hashes strings and takes an # integer to define the member of the family.
# Return a hash function parametrized by i
def hashFamily(i):
    resultSize = 8      # how many bytes we want back
    maxLen = 20         # how long can our i be (in decimal)
    salt = str(i).zfill(maxLen)[-maxLen:]
    def hashMember(x):
        hasher = hashlib.sha1(x + salt).digest()[-resultSize:]
        return hasher
    return hashMember


# Jaccard Similarity
def jaccardSim(shingles1, shingles2):
    intersection = len(set(shingles1).intersection(shingles2))
    return intersection / float(len(shingles1) + len(shingles2) - intersection)

