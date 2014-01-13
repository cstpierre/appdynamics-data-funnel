package com.appdynamics.integration.funnel.plugins;

import com.appdynamics.integration.funnel.exceptions.AppDFunnelExecutionError;
import com.appdynamics.integration.funnel.funneltypes.FunnelInterface;

public interface FunnelTransformationInterface 
{
	public abstract String transform(String content, FunnelInterface funnel) throws AppDFunnelExecutionError;
}
