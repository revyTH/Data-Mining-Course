#!/usr/bin/env python

from ShingleDoc import ShingleDocument
from MinHash import MinHashSets
from LSH import LSH
from ShingleSimilarity import ShingleSimilarity
import time

print""
print ("START")

k = 10      # shingle size
n = 100     # number of permutations (minhash functions)
b = 10      # number of bands
r = 10      # number of rows for band (bandridth)

print""
print " ------------------------ "
print "Shingle size: " + str(k)
print "Number of minhash permutations: " + str(n)
print "Number of bands: " + str(b)
print "Number of rors for band: " + str(r)
print " ------------------------ "
print""


file = open("problem6output.tsv", 'r')
jobs = file.read().split('\n')
file.close()

shingleDocuments = []
end = len(jobs)-1



print "Computing shingles..."
start = time.time()
for i in range(0, end):
    job = jobs[i]
    jobDescription = job.split('\t')[5]
    shingleDoc = ShingleDocument(jobDescription, k)
    shingleDocuments.append(shingleDoc)
shiglesTime = time.time() - start
print "DONE. Time: " + str(shiglesTime)


print""
print " ------------------------ "
print""


print "Computing minhashings..."
minhashingTimeStart = time.time()
minHashSets = MinHashSets(shingleDocuments, n).minHashSets
minhashingTimeEnd = time.time() - minhashingTimeStart
print "DONE. Time: " + str(minhashingTimeEnd)
#for key in minHashSets:
#    print len(minHashSets[key]), minHashSets[key]


print""
print " ------------------------ "
print""


print "Computing LSH..."
lshTimeStart = time.time()
lsh = LSH(minHashSets, b, r, n)
lshTimeEnd = time.time() - lshTimeStart
print "DONE. Time: " + str(lshTimeEnd)
candidatePairs = lsh.candidatePairs
nearDuplicatesLSH = lsh.nearDuplicates
print "Number of pairs: " + str(len(candidatePairs))
print "Number of duplicates: " + str(len(nearDuplicatesLSH))


print""
print " ------------------------ "
print""


print "Computing Jaccard Similarity..."
jaccartTimeStart = time.time()
shingleSimilarity = ShingleSimilarity(shingleDocuments)
jaccartTimeEnd = time.time() - jaccartTimeStart
print ("DONE. Time: " + str(jaccartTimeEnd))
neighboursPairs = shingleSimilarity.neighboursPairs
nearDuplicatesJS = shingleSimilarity.nearDuplicates
#print shingleSimilarity.neighboursPairs
#print shingleSimilarity.nearDuplicates
print "Number of pairs: " + str(len(neighboursPairs))
print "Number of duplicates: " + str(len(nearDuplicatesJS))


print""
print " ------------------------ "
print""

print "Intersection between Jaccard and LSH near-duplicates: " + str(len(set(nearDuplicatesJS).intersection(nearDuplicatesLSH)))


print""
print "END"


#print neighboursPairs
#print candidatePairs
# print nearDuplicatesJS
# print nearDuplicatesLSH

