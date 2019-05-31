

class InterestPairs:
    
    @staticmethod
    def interestPairs(resultDict, numBaskets):

        interestDict = {}
    
        for tuplaKey in resultDict:
    
            if len(tuplaKey) == 2:
    
                key_i = (tuplaKey[0],)
                key_j = (tuplaKey[1],)
    
    
                support_i = float(resultDict[key_i])
                support_j = float(resultDict[key_j])
    
                confidence1 = resultDict[tuplaKey] / support_i
                confidence2 = resultDict[tuplaKey] / support_j
    
                interest1 = confidence1 - (support_j / numBaskets)
                interest2 = confidence2 - (support_i / numBaskets)
    
    
                relationString1 = "[" + str(tuplaKey[0]) + " -> " + str(tuplaKey[1]) + "]: "
                relationString2 = "[" + str(tuplaKey[1]) + " -> " + str(tuplaKey[0]) + "]: "
    
                interestDict[relationString1] = interest1
                interestDict[relationString2] = interest2
    
        return interestDict



    '''
    @staticmethod
    def interestsTotal():
        interestDict = {}
        for iteration in range(iterations):
            dataSetElements = set()
            for basket in dataSetsDict[iteration]:
                for item in basket:
                    dataSetElements.add(item)

            tempDict = resultPairsDict[0]

            for frequentItemSet in tempDict:
                for elem in dataSetElements:
                    if elem not in frequentItemSet:
                        support_union = 0.0
                        support_elem = 0.0

                        for basket in dataSetsDict[iteration]:
                            basketSet = set(basket)
                            if elem in basketSet:
                                support_elem += 1

                                tuplaSet = set()
                                for item in frequentItemSet:
                                    tuplaSet.add(item)

                                if tuplaSet in basket:
                                    support_union += 1

                        confidence = support_union / tempDict[frequentItemSet]
                        interest = confidence - (support_elem / len(dataSetsDict[iteration]))
                        keyString = str(frequentItemSet) + " to " + str(elem)
                        interestDict[keyString] = interest



        print interestDict
    '''
