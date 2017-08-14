package edu.cdb.masters.ai;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import edu.cdb.masters.ai.Categorizer.Category;

public class Executor extends Thread {

	private Category cat;
	private String param;
	public Executor(Category cat, String param)
	{
		this.cat = cat;
		this.param = param;
	}
	
	@Override
	public void run()
	{
		try
		{
			if (cat == Category.COMPUTER)
			{
				param = param.replaceAll(" ", "+");
				
					//Desktop.getDesktop().browse(new URI("http://translate.google.com/translate_tts?tl=en&q="+param));
					File f = new File("resources/TestVoiceAPI.html");
					Desktop.getDesktop().open(f);
			}
			else if(cat == Category.SEARCH)
			{
				param = param.replaceAll(" ", "+");
				Desktop.getDesktop().browse(new URI("http://www.google.com/search?q="+param));			
			}
			else if(cat == Category.THERMOSTAT)
			{
				param = param.replaceAll(" ", "+");
				Desktop.getDesktop().browse(new URI("http://translate.google.com/translate_tts?tl=en&q="+"Changing+Thermostat+now"));
			}
//			else if (cat == Category.OPENTABLE)
//			{
//				//opentable.heroku.com REST API - JSON RESP
//			}
//			else if (cat == Category.W_ALPHA)
//			{
//				//w_alpha API lib
//			}
			else
			{
				System.out.println("This category is not currently supported");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
