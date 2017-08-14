package edu.cdb.masters.ai;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.InvalidParameterException;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

public class Categorizer {
	
	private static final String TXTFILE = "resources/en-doccat-maxent.txt";

	private static final String BINFILE = "resources/en-doccat-maxent.bin";
	
	private DoccatModel model;

	public enum Category {
		RECEIVER("RECEIVER"),
		CABLE("CABLE"),
		SEARCH("SEARCH"),
		COMPUTER("COMPUTER"),
		THERMOSTAT("THERMOSTAT");
		
		private String value;

        private Category(String value) {
                this.value = value;
        }
        
        public String getValue()
        {
        	return this.value;
        }
	};
	
	public Categorizer()
	{
		
	}

	public Category categorizeStatement(String statement)
	{
		String category;
		if(statement != null)
		{
			DocumentCategorizerME myCategorizer = new DocumentCategorizerME(getModel());
			double[] outcomes = myCategorizer.categorize(statement);
			category = myCategorizer.getBestCategory(outcomes);
			System.out.println("Using Category " + category);
		}
		else
		{
			throw new InvalidParameterException("Statement equals null");
		}
		return Category.valueOf(category);
	}
	
//	public void rewriteBin()
//	{
//		InputStream is = null;
//		try {
//			is = new FileInputStream(TXTFILE);
//
//			ObjectStream<String> txtlineStream = new PlainTextByLineStream(is,
//					"UTF-8");
//			ObjectStream<DocumentSample> docSampleStream = new DocumentSampleStream(
//					txtlineStream);
//
//			setModel( DocumentCategorizerME.train("en",
//					docSampleStream, 1, 100));
//
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			if (is != null) {
//				try {
//					is.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//	}

	public DoccatModel getModel() {
		if(this.model == null)
		{
			refreshModel();
		}
		return this.model;
	}

	private void refreshModel() {
		InputStream is;
		try {
			is = new FileInputStream(BINFILE);
			setModel(new DoccatModel(is));
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void setModel(DoccatModel m) {
		this.model = m;
	}
	
	//init the bin
	public static void main(String[] args) {
		try {
			Categorizer cat = new Categorizer();
			cat.initBin();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the model for use
	 * @param txtLocation
	 * @throws IOException
	 */
	public void initBin() {
		InputStream is = null;

		try {
			PrintStream printStreamOriginal = System.out;
			System.setOut(new PrintStream(new OutputStream() {
				public void write(int b) {
					// NO-OP
				}
			}));
			
			is = new FileInputStream(TXTFILE);

			ObjectStream<String> txtlineStream = new PlainTextByLineStream(is,
					"UTF-8");
			ObjectStream<DocumentSample> docSampleStream = new DocumentSampleStream(
					txtlineStream);

			setModel( DocumentCategorizerME.train("en",
					docSampleStream, 1, 100));

			getModel().serialize(new FileOutputStream(
					BINFILE));
			System.setOut(printStreamOriginal);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
	

}
