'''
Data Mining 2015 - Homework1 - Problem 5
Ludovico Fabbri
1197400
'''

import re;
import requests;
#from tabulate import tabulate;


# class for a beer entity..
class BeerEntity:
    def __init__(self, _name, numReviews, _avgScore):
        self.name = _name
        self.numReviews = numReviews
        self.avgScore = _avgScore


with open("ratebeerProcessed.txt", mode='r') as file:

    def topBeersWithReviews(numReviews):

        # regular expression for one or more tabs
        match = re.compile("\t+")

        resultList = []
        currentReviews = 0
        currentScore = 0.0
        avgScore = 0.0
        line = file.readline()
        indexes = match.search(line).span()
        currentReview = line[0:indexes[0]]
        temp = currentReview
        file.seek(0,0)

        while True:

            line = file.readline()

            # if we are at the end of file..
            if (line == ""):
                if (currentReviews > numReviews):
                    avgScore = currentScore / currentReviews
                    beerEntity = BeerEntity(temp, currentReviews, avgScore)
                    resultList.append(beerEntity)
                print ("EOF")
                break

            # search indexes for the multi-tab white space in line
            indexes = match.search(line).span()
            currentReview = line[0:indexes[0]]

            # if we are reading a review of a new beer..
            if (currentReview != temp):
                if (currentReviews > numReviews):
                    avgScore = currentScore / currentReviews
                    beerEntity = BeerEntity(temp, currentReviews, avgScore)
                    resultList.append(beerEntity)

                temp = currentReview
                currentReviews = 0
                currentScore = 0

            currentScore += float(line[indexes[1]:])
            currentReviews += 1

        # sort the result list by the avgScore attribute..
        resultList.sort(key= lambda beerEntity: beerEntity.avgScore, reverse=True)

        return resultList



    def printList(resultList):
        # print the top 10 beers..
        file = open ("problem5output.tsv", "w")
        resultString = ""
        print ""
        for i in range(10):
            if (i >= len(resultList)):
                break
            beerEntity = resultList[i]
            #tab separated: name <tab> numReviews <tab> avgScore
            resultString += beerEntity.name + "\t" + str(beerEntity.numReviews) + "\t" + str(beerEntity.avgScore) + "\n"

        print""
        file.write(resultString)
        file.close()
        print resultString


    resultList = topBeersWithReviews(100)
    print ""
    printList(resultList)
