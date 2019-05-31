__author__ = 'ludovicofabbri'
import os.path
import re
import nltk
import string
from nltk.corpus import stopwords
from bs4 import BeautifulSoup
from gutenberg.acquire import load_etext
from gutenberg.cleanup import strip_headers
import sys
reload(sys)
sys.setdefaultencoding("iso-8859-1")


# dictionary key=document, value=language
LANG_DICT = {}

# stems
porterStemmer = nltk.PorterStemmer()

# regex for punctuation remove
pattern = re.compile('[%s]' % re.escape(string.punctuation))



def preProcessing():

    file_list = []
    fileOutput = open('ex3_preProcessed_step1.txt', 'w')
    base_path = 'Gutenberg/'


    for dir in os.listdir('Gutenberg'):
        if (dir == 'authors.html' or dir[0]=='.'):
            continue
        path = base_path + dir
        for file in os.listdir(path):
            if len(file) > 4 and file[-3:] == 'txt':
                file_path = path + '/' + file
                file_list.append(file_path)
                # print dir, file, file_path
    i = 0
    for filePath in file_list:
        fileInput = open(filePath, 'r')
        fileName = filePath.split('/')[2]#[0:-4]

        if (fileName in LANG_DICT and LANG_DICT[fileName] == 'English'):
            doc = fileInput.read()
            doc_no_header = removeHeader(doc)
            doc_lower = doc_no_header.lower()
            doc_no_punctuation = pattern.sub('', doc_lower)
            doc_no_numbers = ''.join(i for i in doc_no_punctuation if not i.isdigit())
            #remove multiple white spaces, trailings
            doc_lower_sub = ' '.join(doc_no_numbers.split())

            filtered_words = removeStopwords(fileName, doc_lower_sub.split())
            doc_stems = computeStems(filtered_words)

            result = fileName + '\t' + ' '.join(doc_stems) + '\n'
            fileOutput.write(result)
            fileInput.close()
            i += 1
            print str(i), fileName
        else:
            continue

    fileOutput.close()





def computeLanguages():
    lang_dict = {}
    DEFAULT_LANG = 'English'

    file = open('Gutenberg/authors.html', 'r')
    doc = file.read()
    soup = BeautifulSoup(doc, 'html.parser')

    links = soup.find_all('a')

    for link in links:
        if '.' in link.string:
            title = link.string#.split('.')[0]
            if title not in lang_dict:
                lang_dict[title] = DEFAULT_LANG
                font_tag_lang = link.parent.parent.next_sibling.next_sibling.next_sibling.next_sibling
                if font_tag_lang:
                    language = font_tag_lang.find('font').string
                    if language:
                        lang_dict[title] = language

    # for key in lang_dict:
    #      print key, lang_dict[key]
    # print len(lang_dict)

    file.close()
    return lang_dict


def removeHeader(doc):
    doc_no_header = strip_headers(doc).strip()
    return doc_no_header


def removeStopwords(fileName, doc):
    # language = ''
    # if fileName in LANG_DICT:
    #     language = LANG_DICT[fileName]
    # else:
    #     language = 'English'
    # stopWords = stopwords.words(language)
    stopWords = set(stopwords.words('English'))
    filtered_words = [word for word in doc if word not in stopWords and len(word) > 0]
    return filtered_words


def computeStems(words):
    stems = []
    for word in words:
        stems.append(porterStemmer.stem(word))
    return stems



if __name__ == '__main__':
    LANG_DICT = computeLanguages()
    preProcessing()
