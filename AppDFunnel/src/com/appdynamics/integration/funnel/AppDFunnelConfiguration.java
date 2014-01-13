package com.appdynamics.integration.funnel;

// funnel error classes
import com.appdynamics.integration.funnel.exceptions.AppDFunnelInstantiationError;



// xml parsing classes
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;


// core funnel classes
import com.appdynamics.integration.funnel.funneltypes.FunnelFactory;
import com.appdynamics.integration.funnel.funneltypes.FunnelInterface;


// collection classes
import java.util.ArrayList;
import java.util.List;

public class AppDFunnelConfiguration 
{
	private ControllerConfiguration controllerConf;
	private ArrayList <FunnelInterface> funnels;
	private boolean multithreadedExecution = false;
	long startTimestamp = -1;
	long endTimestamp = -1;
	
	public AppDFunnelConfiguration(Document configDocument, long startEpoch, long endEpoch) throws AppDFunnelInstantiationError
	{
		// initialize the array list and start / end
		funnels = new ArrayList <FunnelInterface> ();
		startTimestamp = startEpoch;
		endTimestamp = endEpoch;
		
		// validate the configuration document at the top level
		// check that document root is as expected
		Element funnelConfigRoot = configDocument.getRootElement();
		if (!funnelConfigRoot.getName().equals("funnelconfig"))
		{
			throw new AppDFunnelInstantiationError("Funnel configuration root is not proper.  Expected 'funnelconfig', got " + funnelConfigRoot.getName());
		}
		
		// get the configuration node
		Element controllerInfoNode = (Element) configDocument.selectSingleNode( "/funnelconfig/controllerinfo" );
		if ( controllerInfoNode == null )
		{
			throw new AppDFunnelInstantiationError("Did not find controllerinfo node. Cannot continue.");
		}
		
		// instantiation the Controller configuration object
		controllerConf = new ControllerConfiguration(controllerInfoNode);
		
		// get the funnels node and check its attributes
		Element funnelsNode = (Element) configDocument.selectSingleNode( "/funnelconfig/funnels" );
		if ( funnelsNode == null )
		{
			throw new AppDFunnelInstantiationError("Did not find funnels node. Cannot continue.");
		}
		
		String executionType = funnelsNode.attributeValue("execution");
		if ( executionType == null || executionType.length() == 0)
		{
			throw new AppDFunnelInstantiationError("funnels node exeuction attribute was not specified or was empty. Cannot continue.");
		}
		else
		{
			if (executionType.equals("SERIAL"))
			{
				multithreadedExecution = false;
			}
			else if (executionType.equals("PARALLEL"))
			{
				multithreadedExecution = true;
			}
			else
			{
				throw new AppDFunnelInstantiationError("Value of execution attribute on funnels node must be either SERIAL or PARALLEL. Cannot continue.");
			}
		}
		
		// get all the funnel nodes and instantiate funnel executors based on them
		List <Node> funnelNodes = configDocument.selectNodes("/funnelconfig/funnels/funnel");
		if ( funnelNodes == null || funnelNodes.isEmpty() )
		{
			throw new AppDFunnelInstantiationError("There must be at least one funnel defined in the configuration. Cannot continue.");
		}
		else
		{
			for ( int i = 0; i < funnelNodes.size(); i++ )
			{
				Element funnelNode = (Element)funnelNodes.get(i);
				funnels.add(FunnelFactory.instantiate(controllerConf, startTimestamp, endTimestamp, funnelNode));
			}
		}
	}
	
	// core getters
	public ControllerConfiguration getControllerConf() 
	{
		return controllerConf;
	}
	
	public ArrayList<FunnelInterface> getFunnels() 
	{
		return funnels;
	}
	
	public boolean isMultithreadedExecution() 
	{
		return multithreadedExecution;
	}
	
	public long getStartTimestamp() 
	{
		return startTimestamp;
	}

	public long getEndTimestamp() 
	{
		return endTimestamp;
	}
}
