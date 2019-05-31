CP = ${HADOOP_CLASSPATH}
BIN = bin/
SRC = src/

all:
	javac -classpath $(CP) -d $(BIN) $(SRC)Esercizio3_extra.java
	jar -cvf Esercizio3_extra.jar -C $(BIN) .

run:
	hdfs dfs -rm -f -r -skipTrash /out /tmp
	hadoop jar Esercizio3_extra.jar Esercizio3_extra /input/ex3_preProcessed_step1.txt /out

.phony: clean

clean:
	rm -rf $(BIN)* *.jar
