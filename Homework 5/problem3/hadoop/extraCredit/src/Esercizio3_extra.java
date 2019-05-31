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

public class Esercizio3_extra {
	
	private static final String TEMP_DIR = "/tmp";
	private static final int NUM_DOCS = 1017678;
	private static int count = 0;

	public static void main(String[] args) throws Exception {

		int numReducers = 1;
		List<String> otherArgs = new ArrayList<String>();
		for (int i=0; i<args.length; i++)
			if ("-r".equals(args[i])) 
				numReducers = Integer.parseInt(args[++i]);
			else otherArgs.add(args[i]);


		boolean res1 = round1(numReducers, new Path(otherArgs.get(0)), new Path(TEMP_DIR));
		if (!res1)
			System.exit(1);

		boolean res2 = round2(numReducers, new Path(TEMP_DIR), new Path(otherArgs.get(1)));
		if (!res2)
			System.exit(1);


		System.exit(0);
	}


	private static boolean round1(int numReducers, Path in, Path out) throws Exception {
		Job job = Job.getInstance();

		job.setJobName("Esercizio3_extra-Round1");

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setInputFormatClass(TextInputFormat.class);

		job.setJarByClass(Esercizio3_extra.class);
		job.setMapperClass(MyMapper1.class);
		job.setReducerClass(MyReducer1.class);
		
		// job.setNumReduceTasks(1);

		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);
		
		return job.waitForCompletion(true);
	}

	private static boolean round2(int numReducers, Path in, Path out) throws Exception {
		Job job = Job.getInstance();

		job.setJobName("Esercizio3_extra-Round1");

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setInputFormatClass(TextInputFormat.class);

		job.setJarByClass(Esercizio3_extra.class);
		job.setMapperClass(MyMapper2.class);
		job.setReducerClass(MyReducer2.class);
		
		// job.setNumReduceTasks(1);

		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);
		
		return job.waitForCompletion(true);
	}



	/*
	 *	Round 1
	 */

	public static class MyMapper1 extends Mapper<LongWritable, Text, Text, Text> {
		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

			String[] tokens = value.toString().split("\t");
			String docName = tokens[0];


			StringTokenizer tokenizer = new StringTokenizer(tokens[1], " ");
			int docLength = tokenizer.countTokens();

			while (tokenizer.hasMoreTokens()) {
				context.write(new Text(tokenizer.nextToken()), new Text(docName + ":" + docLength));
			}
		}
	}

	public static class MyReducer1 extends Reducer<Text, Text, Text, Text> {

		// map(docID, num_occurrences_stem)
		private HashMap<String, Integer> map_stemOccurrences = new HashMap<String, Integer>();
		// map(docID, docLength)
		private HashMap<String, Integer> map_docLength = new HashMap<String, Integer>();
		// map(docID, list[stem:ifidf])	
		// private HashMap<String, List<String>> map_out = new HashMap<String, List<String>>();


		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			// clear maps for each reducer iteration
			map_stemOccurrences.clear();
			map_docLength.clear();


			for (Text value : values) {
				String[] tokens = value.toString().split(":");
				String docName = tokens[0];
				int docLength = Integer.parseInt(tokens[1]);
								
				// update map_stemOccurrences
				if (!map_stemOccurrences.containsKey(docName)) {
					map_stemOccurrences.put(docName, 1);
				}
				else {
					map_stemOccurrences.put(docName, map_stemOccurrences.get(docName)+1);
				}

				//update map_docLength
				if (!map_docLength.containsKey(docName)) {
					map_docLength.put(docName, docLength);
				}
			}

			// idf = log_10 [(Num_Docs) / (Number of documents where the stem appears)]
			double idf = Math.log((double)NUM_DOCS / map_docLength.size()) / Math.log(10);


			StringBuilder sb = new StringBuilder();

			for (String docName : map_docLength.keySet()) {
				double tf = (double)map_stemOccurrences.get(docName) / map_docLength.get(docName);
				double tfidf = tf * idf;
				sb.append(docName + ":" + tfidf + ",");
			}

			context.write(key, new Text(sb.toString()));
		}
	}





	/*
	 *	Round 2
	 */

	public static class MyMapper2 extends Mapper<LongWritable, Text, Text, Text> {
		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

			StringBuilder sb = new StringBuilder();
			sb.append(value.toString().split("\t")[1]);
			StringTokenizer tokenizer = new StringTokenizer(sb.toString(), ",");

			while (tokenizer.hasMoreTokens()) {
				StringTokenizer tokenizer2 = new StringTokenizer(tokenizer.nextToken(), ":");
				while (tokenizer2.hasMoreTokens()) {
					context.write(new Text(tokenizer2.nextToken()), 
						new Text(value.toString().split("\t")[0] + ":" + tokenizer2.nextToken()));
				}				
			}
		}
	}

	public static class MyReducer2 extends Reducer<Text, Text, Text, Text> {

		private static HashMap<String, Double> map = new HashMap<String, Double>();

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			
			map.clear();
			double square_sum = 0;
			StringTokenizer tokenizer = null;
			String stem = "";
			double tfidf = 0.0;

			for (Text value : values) {
				tokenizer = new StringTokenizer(value.toString(), ":");
				if (tokenizer.hasMoreTokens())
					stem = tokenizer.nextToken();
				if (tokenizer.hasMoreTokens())
					tfidf = Double.parseDouble(tokenizer.nextToken());

				square_sum += tfidf * tfidf;
				map.put(stem, tfidf);
			}
			System.out.println("");
			square_sum = Math.sqrt(square_sum);

			for (Map.Entry<String, Double> entry : map.entrySet()) {
				map.put(entry.getKey(), entry.getValue() / square_sum);
			}

			StringBuilder sb = new StringBuilder();

			for (Map.Entry<String, Double> entry : map.entrySet()) {
				sb.append(entry.getKey() + ":" + entry.getValue() + ",");
			}

			context.write(key, new Text(sb.toString()));
		}
	}
	
}



