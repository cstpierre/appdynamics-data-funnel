package com.appdynamics.integration.funnel;

// collections classes
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// core funnel classes
import com.appdynamics.integration.funnel.exceptions.AppDFunnelExecutionError;
import com.appdynamics.integration.funnel.funneltypes.FunnelInterface;

// http classes
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class FunnelExecutor 
{
	public static void execute(AppDFunnelConfiguration funnelsConfiguration) throws AppDFunnelExecutionError
	{
		if (funnelsConfiguration.isMultithreadedExecution())
		{
			ArrayList <FunnelInterface> funnels = funnelsConfiguration.getFunnels();
			List<Thread> threads = new ArrayList<Thread>();
			for (int i = 0; i < funnels.size(); i++)
			{
				Runnable runnable = (Runnable)funnels.get(i);
				Thread worker = new Thread(runnable);
		        // We can set the name of the thread
				worker.setName("FUNNEL " + String.valueOf(i));
		        // Start the thread, never call method run() direct
		        worker.start();
		        // Remember the thread for later usage
		        threads.add(worker);
			}
			
			// wait until all the threads complete
			int running = 0;
		    do 
		    {
		    	running = 0;
		    	for (Thread thread : threads) 
		    	{
		    		if (thread.isAlive())
		    		{
		    			running++;
		    		}
		    	}
		    } 
		    while (running > 0);
		}
		else
		{
			// serial execution, just execute each funnel one at a time
			ArrayList <FunnelInterface> funnels = funnelsConfiguration.getFunnels();
			
			for (int i = 0; i < funnels.size(); i++)
			{
				FunnelInterface funnel = funnels.get(i);
				funnel.executeFunnel();
			}
		}
	}
	
	public static String executRESTRequest(String username, String password, String controllerHost, String controllerPort, String URL) throws AppDFunnelExecutionError
	{
		//System.out.println("REST Request URL: " + URL);
		String rest_results = null;
		
		// send the rest request
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(controllerHost, Integer.parseInt(controllerPort)), new UsernamePasswordCredentials(username, password));
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        
        try 
        {
            HttpGet httpget = new HttpGet(URL);

            CloseableHttpResponse response = httpclient.execute(httpget);
            try 
            {
                HttpEntity entity = response.getEntity();

                if (response.getStatusLine().getStatusCode() != 200)
                {
                	throw new AppDFunnelExecutionError("REST REQUEST FAILED. Cannot continue.");
                }
                
                rest_results = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                
            } 
            finally 
            {
                response.close();
            }
        } 
        catch (ClientProtocolException cpe) 
        {
			cpe.printStackTrace();
		} 
        catch (IOException ioe) 
        {
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
        		ioe.printStackTrace();
        	}
        }
        
		return rest_results;
	}
}
