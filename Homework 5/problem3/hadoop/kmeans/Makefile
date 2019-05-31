CP = ${HADOOP_CLASSPATH}
BIN = bin/
SRC = src/

all:
	javac -classpath $(CP) -d $(BIN) $(SRC)Esercizio3_kmeans.java
	jar -cvf Esercizio3_kmeans.jar -C $(BIN) .

run:
	hdfs dfs -rm -f -r -skipTrash /out /tmp /centroids /newCentroids
	hadoop jar Esercizio3_kmeans.jar Esercizio3_kmeans /input/ex3_preProcessed_step2.txt /out 20

.phony: clean

clean:
	rm -rf $(BIN)* *.jar
