package com.appdynamics.integration.funnel.plugins;

import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.appdynamics.integration.funnel.exceptions.AppDFunnelExecutionError;
import com.appdynamics.integration.funnel.funneltypes.FunnelInterface;

public class XmlToHtmlTransformationPlugin implements FunnelTransformationInterface
{
	@Override
	public String transform(String content, FunnelInterface funnel) throws AppDFunnelExecutionError 
	{
		if (!funnel.getOutputType().equals("XML"))
		{
			throw new AppDFunnelExecutionError("XmlToHtml transformation only works on funnels with output type XML.");
		}
		// load the content into a DOM
		Document doc = null;
		try
		{
			doc = DocumentHelper.parseText(content);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new AppDFunnelExecutionError("Error loading REST results into DOM");
		}
		
		Element docElement = doc.getRootElement();
		
		String returnHTML = "";
		returnHTML += ("<html><body>");
		returnHTML += (nodeToHTML(docElement));
		returnHTML += ("</body></html>");
		return returnHTML;
	}
	
	// recursive function that translates a node to an HTML representation of the node
	// this does not work on attributes, it only works on node text content
	private String nodeToHTML(Element elem) throws AppDFunnelExecutionError
	{
		String node_html = "";
		boolean hasChildNodes = false;
		if (!elem.selectNodes("./*").isEmpty())
		{
			hasChildNodes = true;
		}
		
		// if the node has children, just output a table with one table row, with one td that contains the node name
		// and call this function on each child node and a tr with one td before calling this, and /tr /td /table 
		// after the recursive call
		if (hasChildNodes)
		{
			// select all the child nodes
			List <Node> childNodes = elem.selectNodes("./*");
			
			node_html += ("<table border='1'>");
			node_html += ("<tr><td>" + elem.getName() + "</td></tr>");
			node_html += ("<tr><td>");
			node_html += ("<table border='1'>");
			for (int i = 0; i < childNodes.size(); i++)
			{
				node_html += (nodeToHTML((Element)childNodes.get(i)));
			}
			node_html += ("</table>");
			node_html += ("</td></tr>");
			node_html += ("</table>");
		}
		// if the node does not have have children output a table row with this nodes info and txt
		else
		{
			node_html += ("<tr><td>" + elem.getName() + "</td><td>" + elem.getText() + "</td></tr>");
		}
		return node_html;
	}
}
