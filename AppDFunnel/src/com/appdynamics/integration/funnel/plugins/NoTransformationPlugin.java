package com.appdynamics.integration.funnel.plugins;

import com.appdynamics.integration.funnel.exceptions.AppDFunnelExecutionError;
import com.appdynamics.integration.funnel.funneltypes.FunnelInterface;

public class NoTransformationPlugin implements FunnelTransformationInterface 
{
	@Override
	public String transform(String content, FunnelInterface funnel) throws AppDFunnelExecutionError 
	{
		return content;
	}
}