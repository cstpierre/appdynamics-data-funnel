package com.appdynamics.integration.funnel;

// command line parsing classes
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

// funnel exception handling classes




import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

//xml parsing libraries
import org.xml.sax.SAXException;








import com.appdynamics.integration.funnel.exceptions.AppDFunnelExecutionError;
import com.appdynamics.integration.funnel.exceptions.AppDFunnelInstantiationError;





//file io libraries
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppDFunnel 
{
	// configuration object for the funnel to execute
	private AppDFunnelConfiguration appdFunnelConf;
	private long startTimestamp;
	private long endTimestamp;
	
	// constructor for the primary execution class
	public AppDFunnel (String configFilePath, long startEpoch, long endEpoch) throws AppDFunnelInstantiationError
	{
		// set the basic timestamp variables
		startTimestamp = startEpoch;
		endTimestamp = endEpoch;
		
		// check and parse the configuration xml file
		File confFile = null;
		
		// read the file, and perform basic tests to make sure it can be used
		if (configFilePath == null)
		{
			throw new AppDFunnelInstantiationError("File path passed to constructor is null. (coding error)");
		}
		
		confFile = new File(configFilePath);
		if ( !confFile.isFile() || !confFile.canRead() )
		{
			throw new AppDFunnelInstantiationError("File path passed is either not full path to XML file or is not readable");
		}
		
		// load the xml into a DOM
		SAXReader reader = new SAXReader();
		Document doc = null;
		Element docRoot = null;

		try
		{
			doc = reader.read(confFile);
			docRoot = doc.getRootElement();
		} 
		catch (DocumentException de) 
		{
			de.printStackTrace();
			throw new AppDFunnelInstantiationError("Error in the configuration XML");
		}
		
		// instantiate the class variable that holds the configuration
		appdFunnelConf = new AppDFunnelConfiguration(doc, startTimestamp, endTimestamp);
	}
	
	// main method
	public static void main(String[] args) 
	{
		// set up the options that should be passed via the command line
		Options options = new Options();
		
		// FUNNELCONFIG OPTION
		// option value     = configxml
		// has an arg value = true
		// is required      = yes
		// description      = path to the file that contains the funnel configuration
		// option name      = configfile
		Option configfile = OptionBuilder.withArgName( "configxml" )
                                         .hasArg()
                                         .isRequired()
                                         .withDescription( "path to the file that that contains the funnel configuration" )
                                         .create( "configfile" );
		
		// STARTTIME
		// option value     = start_time
		// has an arg value = true
		// description      = the start time to pull metrics from in YYYY-MM-DD_HH:MM:SS format
		// option name      = start
		Option startTime = OptionBuilder.withArgName( "start_time" )
                                        .hasArg()
                                        .withDescription( "the start time to pull metrics from in YYYY-MM-DD_HH:MM:SS format" )
                                        .create( "start" );

		// ENDTIME
		// option value     = end_time
		// has an arg value = true
		// description      = the end time to pull metrics from in YYYY-MM-DD_HH:MM:SS format
		// option name      = end
		Option endTime = OptionBuilder.withArgName( "end_time" )
                                      .hasArg()
                                      .withDescription( "the end time to pull metrics from in YYYY-MM-DD_HH:MM:SS format" )
                                      .create( "end" );

		// add to the options collection
		options.addOption(configfile);
		options.addOption(startTime);
		options.addOption(endTime);
		
		// create the parser object
		CommandLineParser parser = new GnuParser();
		
		// parse the arguments, instantiate the funnel, and execute
		try
		{
			// parse the arguments
			CommandLine cmdLine = parser.parse(options, args);
			
			// get the path of the config xml from the command line
			String configXmlPath = cmdLine.getOptionValue("configfile");
			
			// validate the start and end date times
			String startTimeStr = cmdLine.getOptionValue("start");
			long startEpoch = -1;
			if (startTimeStr != null)
			{
				Date startDate = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").parse(startTimeStr);
				startEpoch = startDate.getTime();
			}
			
			String endTimeStr = cmdLine.getOptionValue("end");
			long endEpoch = -1;
			if (endTimeStr != null)
			{
				Date endDate = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").parse(endTimeStr);
				endEpoch = endDate.getTime();
			}
			
			// if one date is specified, both dates have to be specified
			if ( (startEpoch == -1) || (endEpoch == -1) )
			{
				if ( ! ((startEpoch == -1) && (endEpoch == -1)) )
				{
					System.err.println("Both start and end date must be speficied if one is specified");
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp( "AppDFunnel", options );
					System.exit(0);
				}
			}
			// instantiate the funnel with the configuration file path
			AppDFunnel funnel = new AppDFunnel(configXmlPath, startEpoch, endEpoch);
			
			// execute the funnel
			funnel.execute();
		}
		catch (ParseException pe)
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "AppDFunnel", options );
		}
		catch (AppDFunnelInstantiationError adfie)
		{
			adfie.printStackTrace();
		}
		catch (AppDFunnelExecutionError adfee)
		{
			adfee.printStackTrace();
		} catch (java.text.ParseException e) 
		{
			System.err.println("Either start or end date not specified correctly");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "AppDFunnel", options );
		}
	}
	
	// core execution method for the funnel
	public void execute () throws AppDFunnelExecutionError
	{
		FunnelExecutor.execute(appdFunnelConf);
	}

}
