


file1 = open('kmeansOut50_3.txt', 'r')
file2 = open('kmeansOut50_4.txt', 'r')



docs1 = set()
docs2 = set()

i = 1
j = 1

for line1 in file1:
	docs1 = set(line1.replace('\n', '').split('\t')[1].split(' '))
	
	for line2 in file2:
		docs2 = set(line2.replace('\n', '').split('\t')[1].split(' '))
		print '** intersection (' + str(i) + ',' + str(j) + '): ', set.intersection(docs1, docs2)
		j += 1

	print '\n'

	i += 1
	j = 1
	file2.seek(0,0)


file1.close()
file2.close()