
import urllib2
import json
import time

def run():
    API_KEY = '6f203fd6f9c4a07f65d37ad947e63893:16:72247136'
    BASE_URL = 'http://api.nytimes.com/svc/news/v3/content/all/all?api-key=' + API_KEY + '&offset='
    OFFSET = 20
    NUM_DOCS = 30000


    documentsDict = {}
    docID = 0
    file = open('nyTimes.txt', 'w')


    i = 0
    while len(documentsDict) < NUM_DOCS:

        try:
            result = urllib2.urlopen(BASE_URL+str(i*OFFSET)).read()
            dict = json.JSONDecoder().decode(result)
        except:
            print("Error connection")
            time.sleep(0.1)
            continue

        for key in dict['results']:

            if len(documentsDict) >= NUM_DOCS:
                break

            url = key['url']
            abstract = key['abstract'].encode('ascii', 'ignore')

            if url not in documentsDict:
                documentsDict[url] = docID
                docID += 1
                s = str(docID) + '\t' + str(abstract) + '\t' + url + '\n'
                print s
                file.write(s)
        i += 1

    file.close()