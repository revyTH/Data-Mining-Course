__author__ = 'ludovicofabbri'

import re
import nltk
from nltk.corpus import stopwords


def preProcessing(fileName):

    # pattern for punctuation and numbers
    pattern = re.compile('[^a-z]+')

    # stems & stopwords processing
    porterStemmer = nltk.PorterStemmer()
    stopWords = set(stopwords.words('english'))

    # i/o
    fileInput = open(fileName, 'r')
    fileOutput = open('nyTimes_processed.txt', 'w')


    for line in fileInput:
        # process the abstract only
        tokens = line.split('\t')
        abstract_lower = tokens[1].lower()
        abstract_subbed = pattern.sub(' ', abstract_lower)
        filtered_words = removeStopwords(abstract_subbed.split(' '), stopWords)
        stems = computeStems(filtered_words, porterStemmer)

        url = ''
        try:
            url = tokens[2].lower().replace('\n', '').strip(' ')
        except:
            url = 'no-url'
            print('** No Url'), tokens[0]

        result = tokens[0].strip(' ') + '\t' + url + '\t' + ' '.join(stems) + '\n'
        fileOutput.write(result)


    fileInput.close()
    fileOutput.close()


def removeStopwords(s, stopWords):
    filtered_words = [word for word in s if word not in stopWords and len(word) > 0]
    return filtered_words

def computeStems(words, porterStemmer):
    stems = []
    for word in words:
        stems.append(porterStemmer.stem(word))
    return stems












