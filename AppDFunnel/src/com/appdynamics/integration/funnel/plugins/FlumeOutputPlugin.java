package com.appdynamics.integration.funnel.plugins;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.appdynamics.integration.funnel.exceptions.AppDFunnelExecutionError;
import com.appdynamics.integration.funnel.funneltypes.FunnelInterface;

public class FlumeOutputPlugin implements FunnelOutputInterface 
{
	@Override
	public void output(String content, FunnelInterface funnel) throws AppDFunnelExecutionError 
	{
		if (!funnel.getOutputType().equals("JSON"))
		{
			throw new AppDFunnelExecutionError("Flume HTTP output only works with funnel output type JSON.");
		}
		
		// load the configuration file FileOutputPlugin.xml
		// check and parse the configuration xml file
		File confFile = null;
		String conf_file_path = System.getProperty("user.dir") + File.separator + "config" + File.separator + "FlumeOutputPlugin.xml";
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
		
		// get the flume server from the configuration file
		Node flumeServerNode = doc.getRootElement().selectSingleNode("flume_agent_server");
		if (flumeServerNode == null)
		{
			throw new AppDFunnelExecutionError("flume_agent_server node not found");
		}
		String flumeServer = flumeServerNode.getText();
		
		// get the flume server port from the configuration file
		Node flumeServerPortNode = doc.getRootElement().selectSingleNode("flume_agent_port");
		if (flumeServerPortNode == null)
		{
			throw new AppDFunnelExecutionError("flume_agent_prt node not found");
		}
		String flumeServerPort = flumeServerPortNode.getText();
		
		// sending data to flume agent via HTTP is as simple as posting the JSON content
		// to the server.  Send the data via HTTP POST.
		String url = "http://" + flumeServer + ":" + flumeServerPort;
		CloseableHttpClient httpclient = HttpClients.custom().build();
        
        try 
        {
        	HttpPost httppost = new HttpPost(url);
        	httppost.setHeader("Content-Type", "application/json");
        	httppost.setEntity(new StringEntity(content, ContentType.create("application/json")));

        	CloseableHttpResponse response = httpclient.execute(httppost);
            try 
            {
                HttpEntity entity = response.getEntity();
                if (response.getStatusLine().getStatusCode() != 200)
                {
                	throw new AppDFunnelExecutionError("Flume send failed. Cannot continue. (" + response.getStatusLine() + ")");
                }
                EntityUtils.consume(entity);
            } 
            finally 
            {
                response.close();
            }
        } 
        catch (ClientProtocolException cpe) 
        {
			// TODO Auto-generated catch block
			cpe.printStackTrace();
		} 
        catch (IOException ioe) 
        {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		} 
        finally 
        {
        	try
        	{
        		httpclient.close();
        	}
        	catch (IOException ioe)
        	{
        		// TODO do the right thing
        		ioe.printStackTrace();
        	}
        }
	}
}
