package com.appdynamics.integration.funnel.funneltypes;

//DOM parsing classes
import org.dom4j.Element;
import org.dom4j.Node;




//core appdynamics funnel types
import com.appdynamics.integration.funnel.ControllerConfiguration;
import com.appdynamics.integration.funnel.FunnelExecutor;
import com.appdynamics.integration.funnel.exceptions.AppDFunnelExecutionError;
import com.appdynamics.integration.funnel.exceptions.AppDFunnelInstantiationError;
import com.appdynamics.integration.funnel.plugins.FunnelOutputInterface;
import com.appdynamics.integration.funnel.plugins.FunnelTransformationInterface;

public class ProblemsFunnel implements FunnelInterface, Runnable 
{
	// common funnel variables
	String 					name;
	String 					output;
	long 					startTimestamp;
	long 					endTimestamp;
	ControllerConfiguration controllerConf;
	
	// metric funnel specific variables
	String					applicationid;
	String					problemType;
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
		Node problemtypeNode = funnelDefinition.selectSingleNode( "problemtype" );
		if (problemtypeNode == null)
		{
			throw new AppDFunnelInstantiationError("Unable to retrieve a problemtype node under this funnel definition. Cannot continue.");
		}
		else
		{
			String nodeVal = problemtypeNode.getText();
			if ( nodeVal.isEmpty() )
			{
				throw new AppDFunnelInstantiationError("Value for problemtype must not be empty. Cannot continue.");
			}
			
			if ( ! (nodeVal.equals("policy-violations") || nodeVal.equals("healthrule-violations")) )
			{
				throw new AppDFunnelInstantiationError("Value for problemtype must be either 'policy-violations' OR 'healthrule-violations'. Cannot continue.");
			}
			problemType = nodeVal;
		}
		
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
			builder.append("/problems/");
			builder.append(problemType);
			builder.append("?time-range-type=BETWEEN_TIMES");
			builder.append("&start-time=");
			builder.append(startTimestamp);
			builder.append("&end-time=");
			builder.append(endTimestamp);
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
		//System.out.println("Executing Problems Funnel: " + name);
		
		// get the data from controller
		String funnelData = FunnelExecutor.executRESTRequest(controllerConf.getUsername() + "@" + controllerConf.getAccountName(), controllerConf.getPassword(), controllerConf.getHostName(), controllerConf.getPortNumber(), getRESTURL());
		
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
		return "Problems";
	}

}
