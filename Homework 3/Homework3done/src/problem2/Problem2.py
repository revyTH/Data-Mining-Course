from itertools import combinations
from InterestPairs import *
from SimpleApriori2 import SimpleApriori
import random




# Generation of the dataset
def generateDataSet():
    basketList = []

    for i in range(100000):
        basket = []
        #basketSize = random.randint(1,2001)
        for j in range (2000):
            if (random.random() <= 0.005):  # aggiungiamo un elemento al basket corrente con prob = 1/200 = 0.005
                basket.append(j)
        basketList.append(basket)
    return basketList





def simpleApriori(basketList, threshold, maxK):


    frequentItemSets = set()
    resultDict = {}

    for k in range(1,maxK+1):

        tempDict = {}
        frequentItemSetsCount = 0

        for basket in basketList:
            if k > 1:
                filteredBasket = [val for val in basket if val in frequentItemSets]
            else:
                filteredBasket = basket

            itemSets = list(combinations(filteredBasket, k))

            for itemSet in itemSets:
                if itemSet in tempDict:
                    tempDict[itemSet] += 1
                else:
                    tempDict[itemSet] = 1


        set.clear(frequentItemSets)

        for tuplaKey in tempDict:
            itemSetCount = tempDict[tuplaKey]
            if itemSetCount >= threshold:
                frequentItemSetsCount += 1

                if maxK == 1 and k == 1:
                    resultDict[tuplaKey] = itemSetCount     # ritorna un dizionario solo con singletons

                if maxK == 2:
                    resultDict[tuplaKey] = itemSetCount     # ritorna un dizionario sia con i singletons che con le coppie


                for value in tuplaKey:
                    frequentItemSets.add(value)

    return resultDict





def interestPairs(resultPairsDict, numBaskets):

    interestDict = {}

    for tuplaKey in resultPairsDict:

        if len(tuplaKey) == 2:

            key_i = (tuplaKey[0],)
            key_j = (tuplaKey[1],)


            support_i = float(resultPairsDict[key_i])
            support_j = float(resultPairsDict[key_j])

            confidence1 = resultPairsDict[tuplaKey] / support_i
            confidence2 = resultPairsDict[tuplaKey] / support_j

            interest1 = confidence1 - (support_j / numBaskets)
            interest2 = confidence2 - (support_i / numBaskets)


            relationString1 = "[" + str(tuplaKey[0]) + " -> " + str(tuplaKey[1]) + "]"
            relationString2 = "[" + str(tuplaKey[1]) + " -> " + str(tuplaKey[0]) + "]"

            interestDict[relationString1] = interest1
            interestDict[relationString2] = interest2

    return interestDict











# *** Main ***
if __name__ == "__main__":

    iterations = 10
    resultSingletonsDict = {}       # for each iteration stores a dictionary of the frequent singletons and their support
    resultPairsDict = {}            # for each iteration stores a dictionary of the frequent pairs and their support
    dataSetsDict = {}               # for each iteration stores the generated dataset

    for i in range (iterations):

        print "Generating dataset..."
        dataSet = generateDataSet()
        dataSetsDict[i] = dataSet
        print "Done"

        singletons = simpleApriori(dataSet, 550, 1)
        pairs = simpleApriori(dataSet, 12.5, 2)

        resultSingletonsDict[i] = singletons
        resultPairsDict[i] = pairs



    print""


    # singletons results and average
    print "*** Singletons ***"
    total = 0
    for iteration in resultSingletonsDict:
        currentDict = resultSingletonsDict[iteration]
        print "Simulation " + str(iteration+1) + ": found " + str(len(currentDict)) + " frequent singletons"
        total += len(currentDict)
    print "Average: " + str(total/10)


    print ""

    # singletons results and average
    print "*** Singletons ***"
    total = 0
    for iteration in resultPairsDict:
        currentDict = resultPairsDict[iteration]
        count = 0
        for tuplaKey in currentDict:
            if len(tuplaKey) == 2:
                count += 1
        print "Simulation " + str(iteration+1) + ": found " + str(count) + " pairs"
        total += count
    print "Average: " + str(total/10)


    print ""
    print "*** Computing interests ***"
    print""


    print "Interests for frequent pairs on random dataset:"
    interestDict = InterestPairs.interestPairs(resultPairsDict[0], 100000)

    for key in interestDict:
        print key, interestDict[key]


    print ""

    print "Interests for frequent pairs on retail.dat:"
    retailSimple = SimpleApriori("retail.dat", "simpleOutputRetail.txt", 500)


    interestDict = InterestPairs.interestPairs(retailSimple.resultDict, 88162)
    for key in interestDict:
        print key, interestDict[key]


    print ""





























