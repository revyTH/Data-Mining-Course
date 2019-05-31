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

public class Esercizio3_preProcess {
	
	private static final String TEMP_DIR = "/tmp";
	private static final int NUM_DOCS = 561;
	private static int count = 0;

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

		job.setJobName("Esercizio3_preProcess-Round1");

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);

		job.setInputFormatClass(TextInputFormat.class);

		job.setJarByClass(Esercizio3_preProcess.class);
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
			String docName = tokens[0];
			String[] stems = tokens[1].split(" ");	
			int docLength = stems.length;

			for (String stem : stems) {
				context.write(new Text(stem), new Text(docName + ":" + docLength));
			}	
		}
	}

	public static class MyReducer1 extends Reducer<Text, Text, Text, NullWritable> {

		// map(docID, num_occurrences_stem)
		private HashMap<String, Integer> map_stemOccurrences = new HashMap<String, Integer>();
		// map(docID, docLength)
		private HashMap<String, Integer> map_docLength = new HashMap<String, Integer>();
		// map(docID, list[stem:ifidf])	
		private HashMap<String, List<String>> map_out = new HashMap<String, List<String>>();


		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			// clear maps for each reducer iteration
			map_stemOccurrences.clear();
			map_docLength.clear();


			for (Text value : values) {
				String[] tokens = value.toString().split(":");
				String docID = tokens[0];
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

			

			for (String docID : map_docLength.keySet()) {
				double tf = (double)map_stemOccurrences.get(docID) / map_docLength.get(docID);
				double tfidf = tf * idf;
				String result = key.toString() + ":" + tfidf;

				if (map_out.containsKey(docID)) {
					map_out.get(docID).add(result);
				}
				else {
					map_out.put(docID, new ArrayList<String>());
					map_out.get(docID).add(result);
				}
			}
		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {

			// clear maps
			map_stemOccurrences.clear();
			map_docLength.clear();

			for (String docID : map_out.keySet()) {
				
				double square_sum = 0;
				ArrayList<String> list = (ArrayList<String>)map_out.get(docID);

				for (String s : list) {
					double tfidf = Double.parseDouble(s.split(":")[1]);
					square_sum +=  tfidf * tfidf ;
				}

				square_sum = Math.sqrt(square_sum);

				// l2-normalization
				for (int i=0; i<list.size(); ++i) {
					String[] tokens = list.get(i).split(":");
					double tfidf = Double.parseDouble(tokens[1]) / square_sum;
					list.set(i, tokens[0] + ":" + tfidf);
				}
			}

			for (String docID : map_out.keySet()) {
				
				String result = docID + "\t";

				for (String s : map_out.get(docID)) {
					result += s + ",";
				}
				result = result.substring(0, result.length() - 1);
				context.write(new Text(result), NullWritable.get());
				
			}
		}
	}
}



