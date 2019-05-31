import java.io.IOException;
import java.util.*;
import java.io.*;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;


public class Esercizio3_kmeans {
	
	private static final String TEMP_DIR = "/centroids";
	private static final String TEMP_DIR_2 = "/newCentroids";
	private static final int NUM_DOCS = 561;
	private static boolean convergence = false;
	private static int NUM_CLUSTERS = 10;	//default, use make arg[2] of make file to change
	private static int ITERATIONS = 1;

	public static void main(String[] args) throws Exception {

		int numReducers = 1;
		boolean result = false;
		List<String> otherArgs = new ArrayList<String>();
		for (int i=0; i<args.length; i++)
			if ("-r".equals(args[i])) 
				numReducers = Integer.parseInt(args[++i]);
			else otherArgs.add(args[i]);

		try {
			NUM_CLUSTERS = Integer.parseInt(otherArgs.get(2));
		}
		catch (Exception e) {
			// e.printStackTrace();
			System.out.println("** WARNING: no argument for number of clusters. Default = 10");
			NUM_CLUSTERS = 10;
		}

		// inititialization centroids
		result = initCentroids(numReducers, new Path(otherArgs.get(0)), new Path(TEMP_DIR));
		if (!result) System.exit(1);

		// update centroids and re-assign documents to clusters
		while (!convergence) {
			result = updateCentroids(numReducers, new Path(otherArgs.get(0)), new Path(TEMP_DIR_2));
			if (!result) System.exit(1);
			convergence = checkConvergence();
			ITERATIONS += 1;
		}

		// write output
		result = writeOutput(numReducers, new Path(TEMP_DIR), new Path(otherArgs.get(1)));
			if (!result) System.exit(1);

		System.out.println("\n** Convergence after " + ITERATIONS + " iterations.\n");
		System.exit(0);
	}

	
	private static boolean checkConvergence() {
		boolean convergence = false;
		try {
            FileSystem fs = FileSystem.get(new Configuration());
            Path homeDir = fs.getHomeDirectory();
         	
         	Path centroidsPath = new Path(homeDir, TEMP_DIR);
         	Path new_centroidsPath = new Path(homeDir, TEMP_DIR_2);

            FileStatus[] centroids_status = fs.listStatus(centroidsPath);
            FileStatus[] new_centroids_status = fs.listStatus(new_centroidsPath);

            SortedMap<Integer, HashMap<String, Double>> centroids = new TreeMap<Integer, HashMap<String, Double>>();
            SortedMap<Integer, HashMap<String, Double>> new_centroids = new TreeMap<Integer, HashMap<String, Double>>();
            HashMap<Integer, String> cluster_docs_map = new HashMap<Integer, String>();	//stores for each cluster index the list(string) of the docs in that cluster


            // centroids
            for (int i = 0; i < centroids_status.length; i++) {
                BufferedReader br1 = new BufferedReader(new InputStreamReader(fs.open(centroids_status[i].getPath())));
                int index = 1;
                String line = null;			
				while ((line = br1.readLine()) != null)
			    {
			    	HashMap<String, Double> centroid_map = new HashMap<String, Double>();
	            	String[] stems_tfidf = line.split("\t")[1].split(",");
	            	for (String stem_ifidf : stems_tfidf) {
						String[] parts = stem_ifidf.split(":");
						centroid_map.put(parts[0], Double.parseDouble(parts[1]));
					}
					centroids.put(index, centroid_map);
					index += 1;
				}
			}

			// new_centroids 
            for (int i = 0; i < new_centroids_status.length; i++) {
                BufferedReader br1 = new BufferedReader(new InputStreamReader(fs.open(new_centroids_status[i].getPath())));
                int index = -1;
                String line = null;			
				while ((line = br1.readLine()) != null)
			    {
			    	HashMap<String, Double> new_centroid_map = new HashMap<String, Double>();
			    	String[] tokens = line.split("\t");
			    	index = Integer.parseInt(tokens[0]);
	            	String[] stems_tfidf = tokens[1].split(",");
	            	for (String stem_ifidf : stems_tfidf) {
						String[] parts = stem_ifidf.split(":");
						new_centroid_map.put(parts[0], Double.parseDouble(parts[1]));
					}
					new_centroids.put(index, new_centroid_map);
					cluster_docs_map.put(index, tokens[2]);
				}
			}

			// check if new centroids and current centroids are the same
			convergence = new_centroids.equals(centroids);

			if (fs.exists(centroidsPath))
			{
		      	fs.delete(centroidsPath, true); //Delete centroids Directory
			}
			if (fs.exists(new_centroidsPath))
			{
		      	fs.delete(new_centroidsPath, true); //Delete new_centroids Directory
			}

			fs.mkdirs(centroidsPath);	//Create new Directory for centroids

			StringBuilder sb=new StringBuilder();

			for(Map.Entry<Integer, HashMap<String,Double>> entry : new_centroids.entrySet()) {
				int index = entry.getKey();
				String cluster_docs = cluster_docs_map.get(index);
				HashMap<String, Double> tmp = entry.getValue();
				// String result = index + "\t";
				StringBuilder result = new StringBuilder();
				result.append(index + "\t");

				for (Map.Entry<String, Double> map_entry : tmp.entrySet()) {
					result.append(map_entry.getKey() + ":" + map_entry.getValue() + ",");
				}

				// result = result.substring(0, result.length()-1);
				result.append("\t" + cluster_docs + "\n");
				sb.append(result.toString()); 
			}

			byte[] byt=sb.toString().getBytes();
			Path newFilePath = new Path(homeDir, TEMP_DIR + "/newFile.txt");
			//System.out.println("\n** filepath: " + newFilePath + "\n");
			fs.createNewFile(newFilePath); // create new file
			FSDataOutputStream fsOutStream = fs.create(newFilePath);
			fsOutStream.write(byt);
			fsOutStream.close();

			centroids.clear();
			new_centroids.clear();
			cluster_docs_map.clear();

			if (convergence)				
				return true;			
			else 				
				return false;
			
		} catch (Exception e) {
			//file not found
			return false;
		} 
	}
	
	

	private static boolean initCentroids(int numReducers, Path in, Path out) throws Exception {
		Job job = Job.getInstance();

		job.setJobName("Esercizio3_kmeans-initCentroids");

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setInputFormatClass(TextInputFormat.class);

		job.setJarByClass(Esercizio3_kmeans.class);
		job.setMapperClass(Mapper_initCentroids.class);
		job.setReducerClass(Reducer_initCentroids.class);
		
		//job.setNumReduceTasks(3);

		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);
		
		return job.waitForCompletion(true);
	}

	private static boolean updateCentroids(int numReducers, Path in, Path out) throws Exception {
		Job job = Job.getInstance();

		job.setJobName("Esercizio3_kmeans-updateCentroids");

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setInputFormatClass(TextInputFormat.class);

		job.setJarByClass(Esercizio3_kmeans.class);
		job.setMapperClass(Mapper_updateCentroids.class);
		job.setReducerClass(Reducer_updateCentroids.class);
		
		//job.setNumReduceTasks(3);

		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);
		
		return job.waitForCompletion(true);
	}

	private static boolean writeOutput(int numReducers, Path in, Path out) throws Exception {
		Job job = Job.getInstance();

		job.setJobName("Esercizio3_kmeans-writeOutput");

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setInputFormatClass(TextInputFormat.class);

		job.setJarByClass(Esercizio3_kmeans.class);
		job.setMapperClass(Mapper_writeOutput.class);
		job.setReducerClass(Reducer_writeOutput.class);
		
		//job.setNumReduceTasks(3);

		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);
		
		return job.waitForCompletion(true);
	}



	public static class Mapper_initCentroids extends Mapper<LongWritable, Text, Text, Text> {

		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			int randomValue = randInt(0, NUM_DOCS);
			context.write(new Text("" + randomValue), value);
		}

		private static int randInt(int min, int max) {
		    Random rand = new Random();
		    int randomNum = rand.nextInt((max - min) + 1) + min;
		    return randomNum;
		}
	}

	public static class Reducer_initCentroids extends Reducer<Text, Text, Text, Text> {

		private static int num_centroid = 1;
		private SortedMap<Integer, String> map = new TreeMap<Integer, String>(Collections.reverseOrder());

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			for (Text value : values) {
				map.put(Integer.parseInt(key.toString()), value.toString());
				break;
			}			
		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException  {			
			for (int key : map.keySet()) {
				if (num_centroid > NUM_CLUSTERS)
					break;
				context.write(new Text("" + num_centroid), new Text(map.get(key).split("\t")[1]));
				num_centroid += 1;
			}
			System.out.println("** END OF: INIT_CENTROIDS\n\n");
			map.clear();
		}
	}



	/* Mapper_updateCentroids */
	public static class Mapper_updateCentroids extends Mapper<LongWritable, Text, IntWritable, Text> {

		// temporary map(<stem, tfidf>) for the current document (line) in input
		private HashMap<String, Double> docMap;
		private List<HashMap<String, Double>> centroids;
		private static int map_count;

		@Override
		protected void setup(Context context) throws IOException {

			docMap = new HashMap<String, Double>();
			centroids = new ArrayList<HashMap<String, Double>>();
			map_count = 1;

			FileSystem hdfs =FileSystem.get(new Configuration());
         	Path homeDir=hdfs.getHomeDirectory();
         	
         	Path centroidsPath = new Path(homeDir, TEMP_DIR);
         	Path filePath = new Path(homeDir, TEMP_DIR + "/part-r-00000" );
         	// System.out.println("\n\n** " + centroidsPath + "\n");

			try {
                FileSystem fs = FileSystem.get(new Configuration());
                FileStatus[] status = fs.listStatus(centroidsPath);

                for (int i = 0; i < status.length; i++) {
                	// System.out.println("\n** LIST_STATUS: " + status[i].getPath());
                    BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(status[i].getPath())));
                    String str = null;			
					while ((str = br.readLine())!= null)
				    {
				    	HashMap<String, Double> tmp = new HashMap<String, Double>();
				    	//System.out.println("\n** " + str.split("\t")[0] + "\n");
		            	String[] stems_tfidf = str.split("\t")[1].split(",");
		            	for (String stem_ifidf : stems_tfidf) {
							String[] parts = stem_ifidf.split(":");
							tmp.put(parts[0], Double.parseDouble(parts[1]));
						}
						centroids.add(tmp);				
            		}            		
            	}
            	// System.out.println("\n** NUM_CENTROIDS: " + centroids.size() + "\n");
            } 
            catch(Exception e) {
                System.out.println("\n** File not found \n");
            }
		}

		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

			docMap.clear();
			String[] tokens = value.toString().split("\t")[1].split(",");

			for (String stem_tfidf : tokens) {
				String[] parts = stem_tfidf.split(":");
				docMap.put(parts[0], Double.parseDouble(parts[1]));
			}

			int cluster_index = 1;
			int i = 1;					
			double minDistance = Double.POSITIVE_INFINITY;


			for (HashMap<String, Double> centroid : centroids) {

				double currentDistance = 0.0;
				Set<String> tmp1 = new HashSet<String>();
				tmp1.addAll(docMap.keySet());
				Set<String> tmp2 = new HashSet<String>();
				tmp2.addAll(tmp1);					
				Set<String> tmp3 = new HashSet<String>();;
				tmp3.addAll(centroid.keySet());

				// intersection stored in tmp1
				tmp1.retainAll(tmp3);

				// union stored in tmp2
				tmp2.addAll(tmp3);

				// union - intersection stored in tmp2
				tmp2.removeAll(tmp1);	

				for (String stem : tmp1) {						
					double difference = docMap.get(stem) - centroid.get(stem);
					currentDistance += difference * difference;											
				}

				for (String stem : tmp2) {
					if (docMap.containsKey(stem)) {
						double tfidf = docMap.get(stem);
						currentDistance += tfidf * tfidf;
					}
					else {
						double tfidf = centroid.get(stem);
						currentDistance += tfidf * tfidf;
					}						
				}

				tmp1.clear();
				tmp2.clear();
				tmp3.clear();

				if (currentDistance < minDistance) {				
					minDistance = currentDistance;
					cluster_index = i;
				}

				// next centroid (cluster)
				i += 1;
			}
			context.write(new IntWritable(cluster_index), value);
		}
	}


	/* Reducer_updateCentroids */
	public static class Reducer_updateCentroids extends Reducer<IntWritable, Text, Text, Text> {
		
		private HashMap<String, Double> tempMap = new HashMap<String, Double>();

		@Override
		protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			
			tempMap.clear();
			int cluster_dim = 0;
			System.out.print("\n** CLUSTER_NUMBER: " + key.get() + " ");
			String docList = "";

			for (Text v : values) {
				// System.out.println(v.toString().split("\t")[0]);
				docList += v.toString().split("\t")[0] + " ";
				String[] stems_tfidf = v.toString().split("\t")[1].split(",");
				for (String stem_tfidf : stems_tfidf) {
					String[] parts = stem_tfidf.split(":");
					String stem = parts[0];
					Double tfidf = Double.parseDouble(parts[1]);
					if (tempMap.containsKey(stem)) {
						tempMap.put(stem, tempMap.get(stem) + tfidf);
					}
					else {
						tempMap.put(stem, tfidf);
					}
				}
				cluster_dim += 1;
			}

			// String newCentroid = "";
			StringBuilder newCentroid = new StringBuilder();

			for (String stem : tempMap.keySet()) {
				newCentroid.append(stem + ":" + (tempMap.get(stem) / cluster_dim) + ",");
			}

			// newCentroid = newCentroid.substring(0, newCentroid.length() - 1);
			newCentroid.append("\t" + docList.trim());

			context.write(new Text("" + key.get()), new Text(newCentroid.toString()));
		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {			
			tempMap.clear();	
		}
	}


	/* Mapper_updateCentroids */
	public static class Mapper_writeOutput extends Mapper<LongWritable, Text, Text, Text> {
		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] tokens = value.toString().split("\t");
			context.write(new Text(tokens[0]), new Text(tokens[2]));
		}
	}

	public static class Reducer_writeOutput extends Reducer<Text, Text, Text, Text> {
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			for (Text v : values) {
				context.write(key, v);
				break;
			}
		}
	}

}
