import java.io.IOException;
import java.util.*;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;

public class TopTenBeers {
	
	private static final String TEMP_DIR = "/tmp";

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

		job.setJobName("TopTenBeers-Round1");

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(DoubleWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);

		job.setInputFormatClass(TextInputFormat.class);

		job.setJarByClass(TopTenBeers.class);
		job.setMapperClass(MyMapper1.class);
		job.setReducerClass(MyReducer1.class);
		
		job.setNumReduceTasks(1);

		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);
		
		return job.waitForCompletion(true);
	}

	private static boolean round2(int numReducers, Path in, Path out) throws Exception {
		Job job = Job.getInstance();

		job.setJobName("TopTenBeers-Round2");

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setInputFormatClass(KeyValueTextInputFormat.class);

		job.setJarByClass(TopTenBeers.class);
		job.setMapperClass(MyMapper2.class);
		job.setReducerClass(MyReducer2.class);
		
		job.setNumReduceTasks(1);

		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);
		
		return job.waitForCompletion(true);
	}





	public static class MyMapper1 extends Mapper<LongWritable, Text, Text, DoubleWritable> {
		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

			String[] tokens = value.toString().split("\t");

			String beerName = tokens[0];
			double score = (double)Integer.parseInt(tokens[1]);

			context.write(new Text(beerName), new DoubleWritable(score));
		}
	}

	public static class MyReducer1 extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
		@Override
		protected void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {

			double sum = 0;
			int numReviews = 0;
			for (DoubleWritable value : values) {				
				sum += value.get();
				numReviews += 1;
			}

			double average = sum / numReviews;

			if (numReviews >= 100) {
				context.write(key, new DoubleWritable(average));
			}

			
		}
	}






	public static class MyMapper2 extends Mapper<Text, Text, Text, Text> {
		@Override
		protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {

			// System.out.println("value: "+value);
			// System.out.println("key: "+key+"\n");
			context.write(value, key);
		}
	}

	public static class MyReducer2 extends Reducer<Text, Text, Text, Text> {
		
		protected SortedMap<Double, List<String>> map;

		@Override
		protected void setup(Context context) {
			map = new TreeMap<Double, List<String>>(Collections.reverseOrder());
		}

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			
			List<String> list = new ArrayList<String>();
			// System.out.println("Frequenza "+key.toString());
			for (Text t : values){
				list.add(t.toString());			
			}
			map.put(Double.parseDouble(key.toString()), list);
		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			
			int k = 0;

			for (Map.Entry<Double, List<String>> entry : map.entrySet()) {
				double key = entry.getKey();
				List<String> values = entry.getValue();

				String result = "";

				for (String beer : values) {
					result += beer+" ";
				}
				
				if (k < 10) {
					context.write(new Text(Double.toString(key)), new Text(result));
				}
				k += 1;
			}

		}
	}
}
