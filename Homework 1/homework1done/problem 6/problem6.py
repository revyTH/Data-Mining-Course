'''
Data Mining 2015 - Homework1 - Problem 6
Ludovico Fabbri
1197400
'''


import requests;
import time;
from bs4 import BeautifulSoup;
import re;

baseUrl = 'http://www.kijiji.it/offerte-di-lavoro/offerta/annunci-lazio/informatica-e-web/'
escapeRegExp = re.compile("[\n\r]")
tabRegExp = re.compile("\t+")

def getData (url):

    file = open("problem6output.tsv", "w+")
    request = requests.get(url)
    soup = BeautifulSoup(request.content)
    pages = getPages(soup)


    for pageNumber in range(pages):

        if (pageNumber > 0):
            url = baseUrl + '?p=' + str(pageNumber+1)
            request = requests.get(url)
            soup = BeautifulSoup(request.content)

        advertisements = soup.find_all("div", class_="item-content")

        for ads in advertisements:
            #print ads
            title = ads.find(class_="title").text
            shortDescription = ads.find(class_="description").text
            locale = ads.find(class_="locale").text
            timestamp = ads.find(class_="timestamp").text


            adsLink = ads.parent.get("href")
            request = requests.get(adsLink)
            soup = BeautifulSoup(request.content)
            longDescription = soup.find("p", class_="ki-view-ad-description").text

            title = format(title)
            shortDescription = format(shortDescription)
            locale = format(locale)
            timestamp = format(timestamp)
            adsLink = format(adsLink)
            longDescription = format(longDescription)

            stringResult = title + "\t" + shortDescription + "\t" + locale + "\t" + timestamp + "\t" + adsLink + "\t" + longDescription + "\n"
            stringResult = stringResult.encode('utf-8')

            print stringResult, "------------------------------------"

            file.write(stringResult)

            time.sleep(0.1)

    file.close()


# helper: find the number of pages (paging) on the website for the current query
def getPages(soup):
    pages = 0
    lastPage = soup.find(class_="last-page")
    if lastPage:
        pages = int(lastPage.text)
        #print pages
    else:
        pages = len(soup.find("div", class_="page-list").find_all("a"))
        #print pages
    return int(pages)


# helper: remove tab and escape characters form the string input
def format(string):
    result = re.sub(escapeRegExp, "", string)
    result = re.sub(tabRegExp, "", result)
    return result




getData(baseUrl)
