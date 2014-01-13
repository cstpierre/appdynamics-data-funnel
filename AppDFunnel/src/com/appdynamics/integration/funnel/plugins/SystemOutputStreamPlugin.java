package com.appdynamics.integration.funnel.plugins;

import com.appdynamics.integration.funnel.exceptions.AppDFunnelExecutionError;
import com.appdynamics.integration.funnel.funneltypes.FunnelInterface;

public class SystemOutputStreamPlugin implements FunnelOutputInterface 
{
	@Override
	public void output(String content, FunnelInterface funnel) throws AppDFunnelExecutionError 
	{
		System.out.println(content);
	}

}
