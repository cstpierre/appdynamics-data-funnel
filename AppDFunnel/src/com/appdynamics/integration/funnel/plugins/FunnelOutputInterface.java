package com.appdynamics.integration.funnel.plugins;

import com.appdynamics.integration.funnel.exceptions.AppDFunnelExecutionError;
import com.appdynamics.integration.funnel.funneltypes.FunnelInterface;

public interface FunnelOutputInterface 
{
	public abstract void output(String content, FunnelInterface funnel) throws AppDFunnelExecutionError;
}
