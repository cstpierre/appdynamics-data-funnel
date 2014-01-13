package com.appdynamics.integration.funnel.funneltypes;

import com.appdynamics.integration.funnel.ControllerConfiguration;
import com.appdynamics.integration.funnel.exceptions.AppDFunnelInstantiationError;

import org.dom4j.Element;

public class FunnelFactory 
{
	public static FunnelInterface instantiate(ControllerConfiguration conf, long startEpoch, long endEpoch, Element funnelNode) throws AppDFunnelInstantiationError
	{
		// get the core properties off of the funnel node
		String funnelName = funnelNode.attributeValue("name");
		String funnelType = funnelNode.attributeValue("funneltype");
		String outputType = funnelNode.attributeValue("outputtype");
		
		if (funnelName == null ||
			funnelType == null ||
			outputType == null ||
			funnelName.length() < 1 ||
			funnelType.length() < 1 ||
			outputType.length() < 1)
		{
			throw new AppDFunnelInstantiationError("One or more of the required funnel attribures (name, funneltype, outputtype) was missing. Cannot continue.");
		}
		
		// validate that output type is a know type
		if ( ! (outputType.equals("XML") || outputType.equals("JSON")) )
		{
			throw new AppDFunnelInstantiationError("Unknown outputtype. Only types XML or JSON are supported. Cannot continue.");
		}
		
		// instantiate the funnel based on the funnel type
		if (funnelType.equals("metric"))
		{
			MetricFunnel funnel = new MetricFunnel();
			funnel.initialize(funnelName, outputType, startEpoch, endEpoch, conf, funnelNode);
			return funnel;
		}
		else if (funnelType.equals("businesstransaction"))
		{
			BTFunnel funnel = new BTFunnel();
			funnel.initialize(funnelName, outputType, startEpoch, endEpoch, conf, funnelNode);
			return funnel;
		}
		else if (funnelType.equals("applications"))
		{
			ApplicationFunnel funnel = new ApplicationFunnel();
			funnel.initialize(funnelName, outputType, startEpoch, endEpoch, conf, funnelNode);
			return funnel;
		}
		else if (funnelType.equals("nodes"))
		{
			NodesFunnel funnel = new NodesFunnel();
			funnel.initialize(funnelName, outputType, startEpoch, endEpoch, conf, funnelNode);
			return funnel;
		}
		else if (funnelType.equals("tiers"))
		{
			TiersFunnel funnel = new TiersFunnel();
			funnel.initialize(funnelName, outputType, startEpoch, endEpoch, conf, funnelNode);
			return funnel;
		}
		else if (funnelType.equals("tiersnodes"))
		{
			NodesInTierFunnel funnel = new NodesInTierFunnel();
			funnel.initialize(funnelName, outputType, startEpoch, endEpoch, conf, funnelNode);
			return funnel;
		}
		else if (funnelType.equals("problems"))
		{
			ProblemsFunnel funnel = new ProblemsFunnel();
			funnel.initialize(funnelName, outputType, startEpoch, endEpoch, conf, funnelNode);
			return funnel;
		}
		else if (funnelType.equals("events"))
		{
			EventsFunnel funnel = new EventsFunnel();
			funnel.initialize(funnelName, outputType, startEpoch, endEpoch, conf, funnelNode);
			return funnel;
		}
		else
		{
			throw new AppDFunnelInstantiationError("Unknown funneltype. Cannot continue.");
		}
	}
}
