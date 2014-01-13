package com.appdynamics.integration.funnel.plugins;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.appdynamics.integration.funnel.exceptions.AppDFunnelExecutionError;
import com.appdynamics.integration.funnel.exceptions.AppDFunnelInstantiationError;
import com.appdynamics.integration.funnel.funneltypes.FunnelInterface;

public class FileOutputPlugin implements FunnelOutputInterface 
{
	@Override
	public void output(String content, FunnelInterface funnel) throws AppDFunnelExecutionError 
	{
		// load the configuration file FileOutputPlugin.xml
		// check and parse the configuration xml file
		File confFile = null;
		String conf_file_path = System.getProperty("user.dir") + File.separator + "config" + File.separator + "FileOutputPlugin.xml";
		confFile = new File(conf_file_path);
		if ( !confFile.isFile() || !confFile.canRead() )
		{
			throw new AppDFunnelExecutionError("Configuration file " + conf_file_path + " can't be used");
		}
		
		// load the xml into a DOM
		SAXReader reader = new SAXReader();
		Document doc = null;
		Element docRoot = null;

		try
		{
			doc = reader.read(confFile);
			docRoot = doc.getRootElement();
		} 
		catch (DocumentException de) 
		{
			de.printStackTrace();
			throw new AppDFunnelExecutionError("Error in the configuration XML (" + conf_file_path + ")");
		}
		
		// get the output directory from the configuration file
		Node outputDirNode = doc.getRootElement().selectSingleNode("outputdirectory");
		if (outputDirNode == null)
		{
			throw new AppDFunnelExecutionError("outputdirectory node not found");
		}
		
		String outputDirectory = outputDirNode.getText();
		
		// build the full path to the outputfile to create
		Date date = new Date();
		String filenameToCreate = funnel.getFunnelType() + "." + date.getTime() + "." + funnel.getOutputType();
		String fullFileToCreate = outputDirectory + File.separator + filenameToCreate;
		
		// write the data to the output file
		try
		{
			PrintWriter writer = new PrintWriter(fullFileToCreate, "UTF-8");
			writer.println(content);
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new AppDFunnelExecutionError("Error writing content to file");
		}
		
	}

}
