import java.io.IOException;
import java.util.*;

import java.io.DataInput;
import java.io.DataOutput;
        
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Sales {

 public static class CountrySalesStatsWritable implements Writable {
 	   private IntWritable productCount;
	   private LongWritable priceSum;
	 
	
	   public CountrySalesStatsWritable() 
	   {
			this.productCount = new IntWritable();
			this.priceSum = new LongWritable();
	   }
	 
	   public CountrySalesStatsWritable(IntWritable productCount, LongWritable priceSum) 
	   {
			this.productCount = productCount;
			this.priceSum = priceSum;
	   }
	 
	   @Override

	   public void readFields(DataInput in) throws IOException 
	   {
			productCount.readFields(in);
			priceSum.readFields(in);
	   }
	 
	   @Override

	   public void write(DataOutput out) throws IOException 
	   {
			productCount.write(out);
			priceSum.write(out);
	   }	   
	   
	   @Override

	   public String toString() {
		   return productCount.toString() + " " + priceSum.toString();
		}
 }

 public static class Map extends Mapper<LongWritable, Text, Text, LongWritable> { 
    private Text country = new Text();
    private LongWritable price; 
    
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        String line = value.toString();
        String[] columns = line.split(",");
        
        country.set(columns[7]);
        price = new LongWritable(Long.parseLong(columns[2]));
     
        context.write(country, price); 
        
    }
 } 

 public static class Reduce extends Reducer<Text, LongWritable, Text, CountrySalesStatsWritable> {

    public void reduce(Text key, Iterable<LongWritable> values, Context context)
      throws IOException, InterruptedException {
        int productCount = 0;
        long priceSum = 0;
        
        for (LongWritable price : values) {
        	productCount++;
        	priceSum += price.get();
        }
        

        CountrySalesStatsWritable countrySalesStatsWritable = new CountrySalesStatsWritable(new IntWritable(productCount), new LongWritable (priceSum));
        context.write(key, countrySalesStatsWritable); 
    }
    
 }
        
 public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
   
    Job job = Job.getInstance(conf, "sales"); 
    job.setJarByClass(Sales.class);
    
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(LongWritable.class); 
        
    job.setMapperClass(Map.class);
    job.setReducerClass(Reduce.class);
        
    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
        
    FileInputFormat.addInputPath(job, new Path("hdfs://localhost:54310/user/csdeptucy/input/SalesJan2009.csv"));
    FileOutputFormat.setOutputPath(job, new Path("hdfs://localhost:54310/user/csdeptucy/output_sales"));
    
    System.exit(job.waitForCompletion(true) ? 0 : 1);
 }        
}