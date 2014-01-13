package com.appdynamics.integration.funnel.funneltypes;
// DOM parsing
import org.dom4j.Element;


// core funnel classes
import com.appdynamics.integration.funnel.ControllerConfiguration;
import com.appdynamics.integration.funnel.exceptions.AppDFunnelExecutionError;
import com.appdynamics.integration.funnel.exceptions.AppDFunnelInstantiationError;

public interface FunnelInterface 
{
	public abstract void initialize(String funnelName, String outputType, long startEpoch, long endEpoch, ControllerConfiguration conf, Element funnelDefinition) throws AppDFunnelInstantiationError;
	public abstract String getRESTURL() throws AppDFunnelExecutionError;
	public abstract void executeFunnel() throws AppDFunnelExecutionError;
	public abstract String getName();
	public abstract String getOutputType();
	public abstract long getStartTimeStamp();
	public abstract long getEndTimeStamp();
	public abstract ControllerConfiguration getControllerConfiguration();
	public abstract String getFunnelType();
}
