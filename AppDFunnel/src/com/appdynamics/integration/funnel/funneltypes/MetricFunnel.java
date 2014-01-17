package com.appdynamics.integration.funnel.funneltypes;

//DOM parsing classes
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;






//core appdynamics funnel types
import com.appdynamics.integration.funnel.ControllerConfiguration;
import com.appdynamics.integration.funnel.FunnelExecutor;
import com.appdynamics.integration.funnel.exceptions.AppDFunnelExecutionError;
import com.appdynamics.integration.funnel.exceptions.AppDFunnelInstantiationError;
import com.appdynamics.integration.funnel.plugins.FunnelOutputInterface;
import com.appdynamics.integration.funnel.plugins.FunnelTransformationInterface;

public class MetricFunnel implements FunnelInterface, Runnable
{
	// common funnel variables
	String 					name;
	String 					output;
	long 					startTimestamp;
	long 					endTimestamp;
	ControllerConfiguration controllerConf;
	
	// metric funnel specific variables
	String 					rollup;
	String					applicationid;
	ArrayList <String>		metricpaths;
	String					transformPluginClasspath;
	String					funneloutPluginClasspath;
	
	// interfaces for performing output and data transformation
	FunnelOutputInterface   		out;
	FunnelTransformationInterface	xform;
    
	@Override
	public void initialize(String funnelName, String outputType, long startEpoch, long endEpoch, ControllerConfiguration conf, Element funnelDefinition) throws AppDFunnelInstantiationError 
	{
		// common elements
		name = funnelName;
		output = outputType;
		controllerConf = conf;
		metricpaths = new ArrayList <String> ();
		
		if (startEpoch < 0)
		{
			throw new AppDFunnelInstantiationError("Invalid start timestamp");
		}
		else
		{
			startTimestamp = startEpoch;
		}
		
		if (endEpoch < 0)
		{
			throw new AppDFunnelInstantiationError("Invalid end timestamp");
		}
		else
		{
			endTimestamp = endEpoch;
		}
		
		// get the metric funnel specific elements
		Node rollupNode = funnelDefinition.selectSingleNode( "rollup" );
		if (rollupNode == null)
		{
			throw new AppDFunnelInstantiationError("Unable to retrieve a rollup node under this funnel definition. Cannot continue.");
		}
		else
		{
			String nodeVal = rollupNode.getText();
			if ( ! (nodeVal.equals("true") || nodeVal.equals("false")) )
			{
				throw new AppDFunnelInstantiationError("Value for rollup node must be true of false. Cannot continue.");
			}
			rollup = nodeVal;
		}
		
		List <Node> metricPathNodes = funnelDefinition.selectNodes( "metricpath" );
		if (metricPathNodes.size() < 1)
		{
			throw new AppDFunnelInstantiationError("Unable to retrieve a metricpath node under this funnel definition. At least one metricpath node required. Cannot continue.");
		}
		else
		{
			for (int i = 0; i < metricPathNodes.size(); i++)
			{
				Node metricPathNode = metricPathNodes.get(i);
				String nodeVal = metricPathNode.getText();
				if ( nodeVal.isEmpty() )
				{
					throw new AppDFunnelInstantiationError("Value for metricpath must not be empty. Cannot continue.");
				}
				metricpaths.add(nodeVal);
			}
		}
		
		/*
		Node metricPathNode = funnelDefinition.selectSingleNode( "metricpath" );
		if (metricPathNode == null)
		{
			throw new AppDFunnelInstantiationError("Unable to retrieve a metricpath node under this funnel definition. Cannot continue.");
		}
		else
		{
			String nodeVal = metricPathNode.getText();
			if ( nodeVal.isEmpty() )
			{
				throw new AppDFunnelInstantiationError("Value for metricpath must not be empty. Cannot continue.");
			}
			metricpath = nodeVal;
		}
		*/
		
		Node applicationIdNode = funnelDefinition.selectSingleNode( "applicationid" );
		if (applicationIdNode == null)
		{
			throw new AppDFunnelInstantiationError("Unable to retrieve a applicationid node under this funnel definition. Cannot continue.");
		}
		else
		{
			String nodeVal = applicationIdNode.getText();
			if ( nodeVal.isEmpty() )
			{
				throw new AppDFunnelInstantiationError("Value for applicationid must not be empty. Cannot continue.");
			}
			applicationid = nodeVal;
		}
		
		Node xformPluginNode = funnelDefinition.selectSingleNode( "transformationplugin" );
		if (xformPluginNode == null)
		{
			throw new AppDFunnelInstantiationError("Unable to retrieve a transformationplugin node under this funnel definition. Cannot continue.");
		}
		else
		{
			String nodeVal = xformPluginNode.getText();
			if ( nodeVal.isEmpty() )
			{
				throw new AppDFunnelInstantiationError("Value for transformationplugin must not be empty. Cannot continue.");
			}
			transformPluginClasspath = nodeVal;
		}
		
		Node outputPluginNode = funnelDefinition.selectSingleNode( "funneloutplugin" );
		if (outputPluginNode == null)
		{
			throw new AppDFunnelInstantiationError("Unable to retrieve a funneloutplugin node under this funnel definition. Cannot continue.");
		}
		else
		{
			String nodeVal = outputPluginNode.getText();
			if ( nodeVal.isEmpty() )
			{
				throw new AppDFunnelInstantiationError("Value for funneloutplugin must not be empty. Cannot continue.");
			}
			funneloutPluginClasspath = nodeVal;
		}
		
		// load the classes specified by the classpaths given by the plugins
		try
		{
			Class cls = Class.forName(transformPluginClasspath);
			xform = (FunnelTransformationInterface)cls.newInstance();
		}
		catch ( Exception e )
		{
			throw new AppDFunnelInstantiationError("Could not instantiate " + transformPluginClasspath + " by reflection. Cannot continue.");
		}
		
		try
		{
			Class cls = Class.forName(funneloutPluginClasspath);
			out = (FunnelOutputInterface)cls.newInstance();
		}
		catch ( Exception e )
		{
			throw new AppDFunnelInstantiationError("Could not instantiate " + funneloutPluginClasspath + " by reflection. Cannot continue.");
		}
	}

	@Override
	public String getRESTURL() throws AppDFunnelExecutionError 
	{
		StringBuilder builder = new StringBuilder();
		String restURL = null;
		try
		{
			builder.append("http://");
			builder.append(controllerConf.getHostName());
			builder.append(":");
			builder.append(controllerConf.getPortNumber());
			builder.append("/controller/rest/applications/");
			builder.append(java.net.URLEncoder.encode(applicationid, "ISO-8859-1"));
			builder.append("/metric-data?metric-path=");
			builder.append("REPLACE_WITH_METRIC_PATH");
			builder.append("&time-range-type=BETWEEN_TIMES");
			builder.append("&start-time=");
			builder.append(startTimestamp);
			builder.append("&end-time=");
			builder.append(endTimestamp);
			builder.append("&rollup=");
			builder.append(rollup);
			builder.append("&output=");
			builder.append(output);
			
			// hack in the %20 instead of "+" for spaces, both are valid but the controller likes %20
			restURL = builder.toString();
			restURL = restURL.replaceAll("\\+", "%20");
		}
		catch (Exception e)
		{
			throw new AppDFunnelExecutionError("Problem encoding REST URL components");
		}
		return restURL;
	}

	@Override
	public void executeFunnel() throws AppDFunnelExecutionError 
	{
		//System.out.println("Executing Metric Funnel: " + name);
		String baseRESTURL = getRESTURL();
		String funnelData = "<MetricFunnelResults>";
		for (int i = 0; i < metricpaths.size(); i++)
		{
			// get the data from controller
			String metricPathCurrent = metricpaths.get(i);
			try
			{
				metricPathCurrent = java.net.URLEncoder.encode(metricPathCurrent, "ISO-8859-1");
				metricPathCurrent = metricPathCurrent.replaceAll("\\+", "%20");
			}
			catch (Exception e)
			{
				throw new AppDFunnelExecutionError("Problem encoding REST URL components");
			}
			String restURLCurrent = baseRESTURL.replaceAll("REPLACE_WITH_METRIC_PATH", metricPathCurrent);
			String funnelDataCurrent = FunnelExecutor.executRESTRequest(controllerConf.getUsername() + "@" + controllerConf.getAccountName(), controllerConf.getPassword(), controllerConf.getHostName(), controllerConf.getPortNumber(), restURLCurrent);
			funnelData += "<MetricFunnelResult>";
			funnelData += funnelDataCurrent;
			funnelData += "</MetricFunnelResult>";
		}
		funnelData += "</MetricFunnelResults>";
		// transform the data
		funnelData = xform.transform(funnelData, this);
		
		// out the data
		out.output(funnelData, this);
	}

	@Override
	public void run() 
	{
		try
		{
			executeFunnel();
		}
		catch (Exception e)
		{
			System.err.println("Error in thread execution");
			e.printStackTrace();
		}
	}

	@Override
	public String getName() 
	{
		return name;
	}

	@Override
	public String getOutputType() 
	{
		return output;
	}

	@Override
	public long getStartTimeStamp() 
	{
		return startTimestamp;
	}

	@Override
	public long getEndTimeStamp() 
	{
		return endTimestamp;
	}

	@Override
	public ControllerConfiguration getControllerConfiguration() 
	{
		return controllerConf;
	}

	@Override
	public String getFunnelType() 
	{
		return "Metric";
	}
}
