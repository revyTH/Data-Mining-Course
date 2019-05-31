CP = ${HADOOP_CLASSPATH}
BIN = bin/
SRC = src/

all:
	javac -classpath $(CP) -d $(BIN) $(SRC)TopTenBeers.java
	jar -cvf TopTenBeers.jar -C $(BIN) .

run:
	hdfs dfs -rm -f -r -skipTrash /out /tmp
	hadoop jar TopTenBeers.jar TopTenBeers /input/ratebeerProcessed.txt /out

.phony: clean

clean:
	rm -rf $(BIN)* *.jar
