/**
 * 
 */
package edu.cdb.masters.ai;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Scanner;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.util.InvalidFormatException;
import edu.cdb.masters.ai.Categorizer.Category;

/**
 * @author bonneyc
 * 
 */
public class MarvinRuntime extends Thread {

	private Categorizer categorizer;	
	private POSModel posModel;
	private String sentence;
	private String subject = "UNKNOWN";
	private String command = "UNKNOWN";
	private String param = "UNKNOWN";
	
	private static final String TXTFILE = "resources/en-doccat-maxent.txt";

	/**
	 * @param args
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static void main(String[] args) {
		
		//LinkedList<MarvinRuntime> runtimes = new LinkedList<MarvinRuntime>();
		
		MarvinRuntime marvin = new MarvinRuntime();

		InputStream posIS = null;
		try {
			posIS = new FileInputStream("resources/en-pos-maxent.bin");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		//POS load
		try {
			marvin.setPOSModel(new POSModel(posIS));
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//make sure there is always a bin
		
		//DocumentCategorizer
		
		//marvin.setSentance("can you please turn the temperature down 5 degrees please");
		//marvin.setSentance("change temperature to 75 degrees");
		marvin.setSentance("Play station channel 68 degrees");
		//marvin.setSentance("can you please change the temperature to 74 degrees please");
		//marvin.setSentance("change the temperature to 74 degrees please");
		//marvin.setSentance("What does the word ensemble mean");
		
		//boolean contRunning = true;
		
			System.out.print("Cmd? : ");
			Scanner scanner = new Scanner(System.in);
	        String input = scanner.nextLine();
	        if (input.length() > 1 && !input.equalsIgnoreCase("exit"))
	    	{
	        	marvin.setSentance(input);
	    	}
	        else if (!input.equalsIgnoreCase("exit"))
	        {
	        	//contRunning = false;
	        }
			
			//marvin.cleanSentance();
			
			marvin.start();
	}
	
	private void setPOSModel(POSModel model) {
		this.posModel = model;
	}

	private POSModel getPOSModel() {
		return this.posModel;
	}

	public void setSentance(String s) {
		this.sentence = s;
	}

	public String getSentence() {
		return this.sentence;
	}

	/**
	 * @pre sentence not null
	 * @pre model not null
	 */
	public void run()
	{
		if(categorizer == null)
		{
			setCategorizer(new Categorizer());
		}
		
		POSTaggerME tagger = new POSTaggerME(getPOSModel());
		String[] words = getSentence().split("\\s+");
		String[] tags = tagger.tag(words);
		double[] probs = tagger.probs();

		for (int i = 0; i < tags.length; i++)
		{
			System.out.println(words[i] + " => " + tags[i] + " @ " + probs[i]);
		}
		
		findResults(tags, probs, words);
		
		print(getSentence());
		print("Most likely subject: " + this.subject);
		print("Most likely subject/command: " + this.command);
		print("Most likely parameter(s): " + this.param);
		
		//get category
		Category category = getCategorizer().categorizeStatement(getSentence());
		
		//train if wrong and overwrite model
		boolean cont = true;
		while(cont)
		{
			System.out.print("ok? : ");
			Scanner scanner = new Scanner(System.in);
	        String userResponse = scanner.nextLine();
	        System.out.println(userResponse);
	        if (userResponse != null && userResponse.startsWith("config"))
	        {
	        	//System.out.println("inside");
	        	String s[] = userResponse.split(" ");
	        	//System.out.println("length " + s.length);
	        	if (s.length > 1)
	        	{
	        		Category cat = Category.valueOf(s[1].toUpperCase());
	        		//System.out.print(cat);
	        		if (cat != null)
	        		{
	        			System.out.println("Reconfiguring statement to " + cat.getValue());
	        			category = cat;
	        			appendFile(TXTFILE, cat.getValue() + " " + getSentence() + "\n");
	        			getCategorizer().initBin();
	        			cont = false;
	        		}
	        		else
	        		{
	        			System.out.println("Unknown category. Acceptable values: " + Category.values());
	        		}
	        	}
	        }
	        else
	        {
	        	cont = false;
	        }
		}
		
		System.out.println("THE FINAL CATEGORY IS: " + category.getValue());
		String parameter = getSentence();
		Executor exec = new Executor(category, parameter);
		exec.run();
		
		//loop if not exit
		
		//exit
	}


	public Categorizer getCategorizer() {
		return categorizer;
	}

	public void setCategorizer(Categorizer categorizer) {
		this.categorizer = categorizer;
	}

	private void print(String string) {
		System.out.println(string);
	}


	private void cleanSentance()
	{
		String sent = getSentence().replaceAll("please", "");
		sent = sent.replaceAll("the", "");
		sent = sent.replaceAll("you", "");
		sent = sent.replaceAll("can","");
		setSentance(sent.trim());
	}


	private void findResults(String[] tags, double[] probs, String[] words)
	{
		int i=0;
		Double highestCommand = Double.valueOf(0);
		Double highestSubject = Double.valueOf(0);
		//Double highestParam = Double.valueOf(0);
		
		for(String s : tags)
		{
			if(s.equals("VB"))
			{
				if(probs[i] > highestCommand)
				{
					highestCommand = probs[i];
					this.command = words[i];
				}
			}
			if (!Arrays.asList(tags).contains("VB") && (s.startsWith("RB") || s.startsWith("WR") || s.startsWith("WD")))
			{
				if(probs[i] > highestCommand)
				{
					highestCommand = probs[i];
					this.command = words[i];
				}
			}
			else if(s.equals("NN"))
			{
				if(probs[i] > highestCommand)
				{
					highestSubject = probs[i];
					this.subject = words[i];
				}
			}
//			else if(Arrays.asList(tags).contains(("NN")))
//			{
//				//find highest NN
//				int j = Arrays.asList(tags).indexOf("NN");
//				
//				if(probs[j] > highestSubject)
//				{
//					highestSubject = probs[j];
//					this.subject = words[j];
//				}
//			}
			else if(subject.equals("UNKNOWN") && s.startsWith("NN"))
			{
				if(probs[i] > highestSubject)
				{
					highestSubject = probs[i];
					this.subject = words[i];
				}
			}
			i++;
		}
		
		if (this.command.equals("UNKNOWN") )
		{
			int nnpIndex = Arrays.asList(tags).indexOf("NNP");
			if (nnpIndex != -1)
			{
				this.command = words[nnpIndex];
			}
		}
		
		if (this.param.equals("UNKNOWN"))
		{
			this.param = "";
			int wordIndex = 0;
			for (String tag : Arrays.asList(tags))
			{
				if(tag.equals("NN") || tag.equals("VBN") || tag.equals("JJ")  )
				{
					this.param += words[wordIndex] + " ";
				}
				wordIndex++;
			}
			int cdIndex = Arrays.asList(tags).indexOf("CD");
			if(cdIndex != -1)
			{
				this.param += words[cdIndex] + " ";
			}
			int nnsIndex = Arrays.asList(tags).indexOf("NNS");
			if(nnsIndex != -1)
			{
				this.param += words[nnsIndex] + " " ;
			}
			this.param.trim();
		}
		
	}
	
	private void appendFile(String fileLocation, String contents)
    {
        Writer out = null;
        try
        {
            out = new OutputStreamWriter(new FileOutputStream(
                fileLocation, true));
            out.write(contents);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally 
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
