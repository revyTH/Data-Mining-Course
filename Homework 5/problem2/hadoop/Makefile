CP = ${HADOOP_CLASSPATH}
BIN = bin/
SRC = src/

all:
	javac -classpath $(CP) -d $(BIN) $(SRC)Esercizio2.java
	jar -cvf Esercizio2.jar -C $(BIN) .

run:
	hdfs dfs -rm -f -r -skipTrash /out /tmp
	hadoop jar Esercizio2.jar Esercizio2 /input/nyTimes_processed.txt /out

.phony: clean

clean:
	rm -rf $(BIN)* *.jar
