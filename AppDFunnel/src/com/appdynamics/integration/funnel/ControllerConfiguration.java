package com.appdynamics.integration.funnel;

import org.dom4j.Element;
import org.dom4j.Node;

import com.appdynamics.integration.funnel.exceptions.AppDFunnelInstantiationError;

public class ControllerConfiguration 
{
	private String username;
	private String accountName;
	private String password;
	private String hostName;
	private String portNumber;
	
	public ControllerConfiguration(Element controllerInfoNode) throws AppDFunnelInstantiationError
	{
		// username
		Node usernameNode = controllerInfoNode.selectSingleNode( "username" );
		if (usernameNode == null)
		{
			throw new AppDFunnelInstantiationError("Unable to retrieve a username node under the controllerinfo node. Cannot continue.");
		}
		else
		{
			String nodeVal = usernameNode.getText();
			if ( nodeVal.isEmpty() )
			{
				throw new AppDFunnelInstantiationError("Value for username node must not be empty. Cannot continue.");
			}
			username = nodeVal;
		}
		
		// accountname
		Node accountnameNode = controllerInfoNode.selectSingleNode( "accountname" );
		if (accountnameNode == null)
		{
			throw new AppDFunnelInstantiationError("Unable to retrieve a accountname node under the controllerinfo node. Cannot continue.");
		}
		else
		{
			String nodeVal = accountnameNode.getText();
			if ( nodeVal.isEmpty() )
			{
				throw new AppDFunnelInstantiationError("Value for accountname node must not be empty. Cannot continue.");
			}
			accountName = nodeVal;
		}
		
		// password
		Node passwordNode = controllerInfoNode.selectSingleNode( "password" );
		if (passwordNode == null)
		{
			throw new AppDFunnelInstantiationError("Unable to retrieve a password node under the controllerinfo node. Cannot continue.");
		}
		else
		{
			String nodeVal = passwordNode.getText();
			if ( nodeVal.isEmpty() )
			{
				throw new AppDFunnelInstantiationError("Value for password node must not be empty. Cannot continue.");
			}
			password = nodeVal;
		}
		
		// controllerhost
		Node controllerhostNode = controllerInfoNode.selectSingleNode( "controllerhost" );
		if (controllerhostNode == null)
		{
			throw new AppDFunnelInstantiationError("Unable to retrieve a controllerhost node under the controllerinfo node. Cannot continue.");
		}
		else
		{
			String nodeVal = controllerhostNode.getText();
			if ( nodeVal.isEmpty() )
			{
				throw new AppDFunnelInstantiationError("Value for controllerhost node must not be empty. Cannot continue.");
			}
			hostName = nodeVal;
		}
		
		// controllerport
		Node controllerportNode = controllerInfoNode.selectSingleNode( "controllerport" );
		if (controllerportNode == null)
		{
			throw new AppDFunnelInstantiationError("Unable to retrieve a controllerport node under the controllerinfo node. Cannot continue.");
		}
		else
		{
			String nodeVal = controllerportNode.getText();
			if ( nodeVal.isEmpty() )
			{
				throw new AppDFunnelInstantiationError("Value for controllerport node must not be empty. Cannot continue.");
			}
			portNumber = nodeVal;
		}
	}
	
	public String getUsername() 
	{
		return username;
	}

	public String getAccountName() 
	{
		return accountName;
	}

	public String getPassword() 
	{
		return password;
	}

	public String getHostName() 
	{
		return hostName;
	}

	public String getPortNumber() 
	{
		return portNumber;
	}
}
