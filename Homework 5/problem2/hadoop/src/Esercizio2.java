import java.io.IOException;
import java.util.*;
import java.util.TreeMap;
import java.io.DataOutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.OutputFormat;

public class Esercizio2 {
	
	private static final String TEMP_DIR = "/tmp";
	private static final int NUM_DOCS = 7886;

	public static void main(String[] args) throws Exception {

		int numReducers = 1;
		List<String> otherArgs = new ArrayList<String>();
		for (int i=0; i<args.length; i++)
			if ("-r".equals(args[i])) 
				numReducers = Integer.parseInt(args[++i]);
			else otherArgs.add(args[i]);


		boolean res1 = round1(numReducers, new Path(otherArgs.get(0)), new Path(otherArgs.get(1)));
		if (!res1)
			System.exit(1);


		System.exit(0);
	}


	private static boolean round1(int numReducers, Path in, Path out) throws Exception {
		Job job = Job.getInstance();

		job.setJobName("Esercizio2-Round1");

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);

		job.setInputFormatClass(TextInputFormat.class);

		job.setJarByClass(Esercizio2.class);
		job.setMapperClass(MyMapper1.class);
		job.setReducerClass(MyReducer1.class);
		
		job.setNumReduceTasks(1);

		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);
		
		return job.waitForCompletion(true);
	}


	public static class MyMapper1 extends Mapper<LongWritable, Text, Text, Text> {
		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

			String[] tokens = value.toString().split("\t");
			int docID = Integer.parseInt(tokens[0]);
			String url = tokens[1];

			try {
				String[]	stems = tokens[2].split(" ");	
				int docLength = stems.length;

				// for each stem write on context: <stem, docID:docLength>	
				for (String stem: stems)
					context.write(new Text(stem), new Text(docID + ":" + docLength));
			}
			catch (ArrayIndexOutOfBoundsException e) {
				//e.printStackTrace();
			}
			
		}
	}

	public static class MyReducer1 extends Reducer<Text, Text, Text, NullWritable> {

		// map(docID, num_occurrences_stem)
		private SortedMap<Integer, Integer> map_stemOccurrences = new TreeMap<Integer, Integer>();
		// map(docID, docLength)
		private HashMap<Integer, Integer> map_docLength = new HashMap<Integer, Integer>();	


		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			// clear maps for each reducer iteration
			map_stemOccurrences.clear();
			map_docLength.clear();


			for (Text value : values) {
				String[] tokens = value.toString().split(":");
				int docID = Integer.parseInt(tokens[0]);
				int docLength = Integer.parseInt(tokens[1]);
								
				// update map_stemOccurrences
				if (!map_stemOccurrences.containsKey(docID)) {
					map_stemOccurrences.put(docID, 1);
				}
				else {
					map_stemOccurrences.put(docID, map_stemOccurrences.get(docID)+1);
				}

				//update map_docLength
				if (!map_docLength.containsKey(docID)) {
					map_docLength.put(docID, docLength);
				}
			}

			// idf = log_10 [(Num_Docs) / (Number of documents where the stem appears)]
			double idf = Math.log((double)NUM_DOCS / map_docLength.size()) / Math.log(10);

			StringBuilder sb = new StringBuilder();
			sb.append(key.toString() + ":");

			for (int docID : map_docLength.keySet()) {
				double tf = (double)map_stemOccurrences.get(docID) / map_docLength.get(docID);
				double tfidf = tf * idf;
				sb.append(docID + "," + tfidf + " ");
			}

			context.write(new Text(sb.toString()), NullWritable.get());

		}
	}

}



