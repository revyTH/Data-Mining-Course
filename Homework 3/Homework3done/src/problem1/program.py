from SimpleApriori2 import SimpleApriori
from RandomApriori3 import RandomApriori
import time


if __name__ == "__main__":


    print "SIMPLE APRIORI ON retail.dat"
    print "START"
    timeStart = time.time()
    retailSimple = SimpleApriori("retail.dat", "simpleOutputRetail.txt", 500)
    timeEnd = time.time() - timeStart
    print "Found " + str(retailSimple.frequentItemSetsCount) + " frequent itemsets"
    print "TIME END: " + str(timeEnd)


    print ""


    print "RANDOM APRIORI ON retail.dat, threshold = t * p = 50"
    print "START"
    timeStart = time.time()
    retailRandom50 = RandomApriori("retail.dat","randomOutputRetail50.txt", 500, 0.1, False)
    timeEnd = time.time() - timeStart
    print "Found " + str(retailRandom50.frequentItemSetsCountWithFP) + " frequent itemsets with false positives"
    print "Found " + str(retailRandom50.frequentItemSetsCountWithoutFP) + " frequent itemsets without false positives"
    print "False positives removed: " + str(retailRandom50.frequentItemSetsCountWithFP - retailRandom50.frequentItemSetsCountWithoutFP)
    print "TIME END: " + str(timeEnd)


    print ""


    print "RANDOM APRIORI ON retail.dat, threshold = t * p * 0.9 = 45"
    print "START"
    timeStart = time.time()
    retailRandom45 = RandomApriori("retail.dat","randomOutputRetail45.txt", 500, 0.1, True)
    timeEnd = time.time() - timeStart
    print "Found " + str(retailRandom45.frequentItemSetsCountWithFP) + " frequent itemsets with false positives"
    print "Found " + str(retailRandom45.frequentItemSetsCountWithoutFP) + " frequent itemsets without false positives"
    print "False positives removed: " + str(retailRandom45.frequentItemSetsCountWithFP - retailRandom45.frequentItemSetsCountWithoutFP)
    print "TIME END: " + str(timeEnd)





    


    print ""


    print "SIMPLE APRIORI ON webdocs.dat"
    print "START"
    timeStart = time.time()
    webdocsSimple = SimpleApriori("webdocs.dat", "simpleOutputWebdocs.txt", 500000)
    timeEnd = time.time() - timeStart
    print "Found " + str(webdocsSimple.frequentItemSetsCount) + " frequent itemsets"
    print "TIME END: " + str(timeEnd)


    print ""


    print "RANDOM APRIORI ON webdocs.dat, threshold = t * p = 50"
    print "START"
    timeStart = time.time()
    webdocsRandom50 = RandomApriori("webdocs.dat","randomOutputWebdocs50.txt", 500000, 0.0001, False)
    timeEnd = time.time() - timeStart
    print "Found " + str(webdocsRandom50.frequentItemSetsCountWithFP) + " frequent itemsets with false positives"
    print "Found " + str(webdocsRandom50.frequentItemSetsCountWithoutFP) + " frequent itemsets without false positives"
    print "False positives removed: " + str(webdocsRandom50.frequentItemSetsCountWithFP - webdocsRandom50.frequentItemSetsCountWithoutFP)
    print "TIME END: " + str(timeEnd)


    print ""


    print "RANDOM APRIORI ON webdocs.dat, threshold = t * p * 0.9 = 45"
    print "START"
    timeStart = time.time()
    webdocsRandom45 = RandomApriori("webdocs.dat","randomOutputWebdocs45.txt", 500000, 0.0001, True)
    timeEnd = time.time() - timeStart
    print "Found " + str(webdocsRandom45.frequentItemSetsCountWithFP) + " frequent itemsets with false positives"
    print "Found " + str(webdocsRandom45.frequentItemSetsCountWithoutFP) + " frequent itemsets without false positives"
    print "False positives removed: " + str(webdocsRandom45.frequentItemSetsCountWithFP - webdocsRandom45.frequentItemSetsCountWithoutFP)
    print "TIME END: " + str(timeEnd)





    print ""


    print "*** Validating.. ***"

    file = open("simpleOutputRetail.txt", mode="r")
    simpleOutputRetail = set(file)
    file.close()

    file = open("randomOutputRetail50.txt", mode="r")
    randomOutputRetail50 = set(file)
    file.close()

    file = open("randomOutputRetail45.txt", mode="r")
    randomOutputRetail45 = set(file)
    file.close()

    isSubset = len(simpleOutputRetail.intersection(randomOutputRetail50)) == len(randomOutputRetail50)

    print "randomOutputRetail50 is subset of simpleOutputRetail: " + str(isSubset)
    print "False negatives found: " + str(len(simpleOutputRetail) - len(randomOutputRetail50))

    isSubset = len(simpleOutputRetail.intersection(randomOutputRetail45)) == len(randomOutputRetail45)

    print "randomOutputRetail45 is subset of simpleOutputRetail: " + str(isSubset)
    print "False negatives found: " + str(len(simpleOutputRetail) - len(randomOutputRetail45))


    print ""


    file = open("simpleOutputWebdocs.txt", mode="r")
    simpleOutputWebdocs = set(file)
    file.close()

    file = open("randomOutputWebdocs50.txt", mode="r")
    randomOutputWebdocs50 = set(file)
    file.close()

    file = open("randomOutputWebdocs45.txt", mode="r")
    randomOutputWebdocs45 = set(file)
    file.close()

    isSubset = len(simpleOutputWebdocs.intersection(randomOutputWebdocs50)) == len(randomOutputWebdocs50)

    print "randomOutputWebdocs50 is subset of simpleOutputWebdocs: " + str(isSubset)
    print "False negatives found: " + str(len(simpleOutputWebdocs) - len(randomOutputWebdocs50))

    isSubset = len(simpleOutputWebdocs.intersection(randomOutputWebdocs45)) == len(randomOutputWebdocs45)

    print "randomOutputWebdocs45 is subset of simpleOutputWebdocs: " + str(isSubset)
    print "False negatives found: " + str(len(simpleOutputWebdocs) - len(randomOutputWebdocs45))







