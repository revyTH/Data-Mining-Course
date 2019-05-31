
from itertools import combinations

class SimpleApriori:

    def __init__(self, fileInput, fileOutput, threshold):
        self.frequentItemSetsCount = 0
        self.resultDict = {}


        def run(fileInput, fileOutput, threshold):
            with open(fileInput, mode='r') as file:

                k = 1
                frequentItemSets = set()
                outputFile = open(fileOutput, mode="w")

                while True:

                    print "Round k = " + str(k),
                    flag = 0
                    tempDict = {}
                    currentFrequentItemSetsCount = 0

                    for line in file:
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
                        if itemSetCount >= threshold:
                            flag = 1
                            currentFrequentItemSetsCount += 1
                            self.frequentItemSetsCount += 1
                            self.resultDict[tuplaKey] = itemSetCount
                            for value in tuplaKey:
                                frequentItemSets.add(value)
                                outputFile.write(str(value) + " ")
                            outputFile.write("\n")

                    #print frequentItemSets
                    print "Found " + str(currentFrequentItemSetsCount) + " frequent itemsets"

                    if (flag == 0):
                        outputFile.close()
                        break
                    else:
                        k += 1
                        file.seek(0,0)




        run(fileInput, fileOutput, threshold)





