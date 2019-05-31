import random
from itertools import combinations


class RandomApriori:
    def __init__(self, fileInput, fileOutput, threshold, p, falseNegative):

        self.frequentItemSetsCountWithFP = 0
        self.frequentItemSetsCountWithoutFP = 0
        #self.resultDict = {}



        def randomApriori(fileInput, fileOutput, threshold, p, falseNegative):

            if falseNegative:
                newThreshold = threshold * p * 0.9
            else:
                newThreshold = threshold * p

            input = computeInputData(fileInput, p)
            k = 1
            frequentItemSets = set()
            filtersDict = {}
            resultDict = {}


            while True:

                print "Round k = " + str(k),
                flag = 0
                tempDict = {}
                frequentItemSetsCount = 0
                resultDict[k] = []


                for line in input:
                    basket = line.split()
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
                    if itemSetCount >= newThreshold:
                        flag = 1
                        frequentItemSetsCount += 1
                        resultDict[k].append(tuplaKey)
                        for value in tuplaKey:
                            frequentItemSets.add(value)
                            #outputFile.write(str(value) + " ")
                        filtersDict[k-1] = frequentItemSets.copy()
                        #outputFile.write("\n")


                print "Found " + str(frequentItemSetsCount) + " frequent itemsets"
                self.frequentItemSetsCountWithFP += frequentItemSetsCount

                if (flag == 0):
                    #self.resultDict = resultDict.copy()
                    falsePositives(fileInput, fileOutput, threshold, resultDict, k)

                    break
                else:
                    k += 1





        # compute input data
        def computeInputData(fileName, p):
            result = []
            with open(fileName,mode='r') as file:
                for line in file:
                    if random.random() <= p:
                        result.append(line)
            return result



        # false positives detection
        def falsePositives(fileInput, fileOutput, threshold, resultDict, maxK):

            outputFile = open(fileOutput, mode="w")
            tempDict = {}

            with open(fileInput, mode="r") as file:

                for line in file:
                    basket = set(line.split())

                    for k in range(1,maxK):
                        for tupla in resultDict[k]:
                            count = 0
                            for value in tupla:
                                if value not in basket:
                                    break
                                else:
                                    count += 1
                            if count == len(tupla):
                                if tupla in tempDict:
                                    tempDict[tupla] += 1
                                else:
                                    tempDict[tupla] = 1


            for tuplaKey in tempDict:
                if tempDict[tuplaKey] >= threshold:
                    self.frequentItemSetsCountWithoutFP += 1
                    for value in tuplaKey:
                        outputFile.write(str(value) + " ")
                    outputFile.write("\n")


            #print tempDict
            outputFile.close()








        randomApriori(fileInput, fileOutput, threshold, p, falseNegative)



