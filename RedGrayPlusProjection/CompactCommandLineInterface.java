/**
 * This file belongs to the Red Gray Plus projection tool project.
 * 
 * Code author: Farshad Barahimi
 * Research contributors: Farshad Barahimi and Dr. Fernando Paulovich
 * 
 * © 2019-2021 Dr. Fernando Paulovich and Farshad Barahimi. Licensed under the Academic Free License version 3.0 (https://opensource.org/licenses/AFL-3.0).
 * 
 * The Red Gray Plus projection tool project is an academic research project reflecting the Red Gray Projection method which is described in the following preprint on arXiv:
 * Farshad Barahimi and Fernando Paulovich, “Multi-point dimensionality reduction to improve projection layout reliability.” , arXiv preprint (2021).
 */

package RedGrayPlusProjection;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;

public class CompactCommandLineInterface
{
	public static void ProcessCommandLine(String[] args) throws Exception
	{
		System.out.println("");
		System.out.println("Welcome to the Red Gray Plus projection tool version 1.4");

		var printWriter=new PrintWriter( new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);
		printWriter.println("Copyright 2019-2021 Dr. Fernando Paulovich and Mr. Farshad Barahimi.");
		printWriter.println("Licensed under the Academic Free License version 3.0 (https://opensource.org/licenses/AFL-3.0).");
		System.out.println("");
		System.out.println("Look at the following preprint on arXiv for more information about Red Gray Plus projection method:");
		System.out.println("Farshad Barahimi and Fernando Paulovich, \"Multi-point dimensionality reduction to improve projection layout reliability.\" , arXiv preprint (2021).");
		System.out.println("");
		
		if(args.length!=1)
		{
			System.out.println("Usage guide:");
			System.out.println("This command line tool only accepts one argument which is the path to a confiugration file or --help to show more info.");
			System.out.println("");
			return;
		}
		else if(args[0].compareTo("--help")==0)
		{
			System.out.println("Usage guide:");
			System.out.println("This command line tool only accepts one argument which is the path to a confiugration file or --help to show more info.");
			System.out.println("");
			System.out.println("The structure of the configuration file is shown below but not all attributes are necessary.");
			System.out.println("");
			
			System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			System.out.println("<Configuration");
			System.out.println("	Name=\"\"");
			System.out.println("	ProjectionMethod=\"\"");
			System.out.println("	InputFileType=\"\"");
			System.out.println("	InputFileName=\"\"");
			System.out.println("	OutputFolderName=\"\"");
			System.out.println("	InputFileClassColumnType=\"\"");
			System.out.println("	EvaluationNeighborhoodSize=\"\"");
			System.out.println("	UmapTo30DimensionsFirst=\"\"");
			System.out.println("	NumberOfNeighboursForBuildingGraph=\"\"");
			System.out.println("	VisualDensityAdjustmentParameter=\"\"");
			System.out.println("	CosineNeighborhoodNormalization=\"\"");
			System.out.println("	AfterUmapTo30DimensionsMaxRows=\"\"");
			System.out.println("	MaxInputRows=\"\"");
			System.out.println("	NumberOfThreads=\"\"");
			System.out.println("	OverrideMaxNumberOfReplicates=\"\"");
			System.out.println("/>");
			System.out.println("<!-- The attribute EvaluationNeighborhoodSize is optional. Default value: 10 -->");
			System.out.println("<!-- The attribute UmapTo30DimensionsFirst is optional. Default value: false -->");
			System.out.println("<!-- The attribute NumberOfNeighboursForBuildingGraph is optional. Default value: one-third of number of points -->");
			System.out.println("<!-- The attribute VisualDensityAdjustmentParameter is optional. Default value: 0.9 -->");
			System.out.println("<!-- The attribute CosineNeighborhoodNormalization is optional. Default value: false -->");
			System.out.println("<!-- The attribute AfterUmapTo30DimensionsMaxRows is optional. Not effective if no value is specified -->");
			System.out.println("<!-- The attribute MaxInputRows is optional. Not effective if no value is specified -->");
			System.out.println("<!-- The attribute NumberOfThreads is optional. Default value: 1 less than cpu cores -->");
			System.out.println("<!-- The attribute OverrideMaxNumberOfReplicates is optional. Does not override if no value is specified -->");
			
			System.out.println("");
			return;
		}
		
		String configurationFileName=args[0];
		var parameters = processConfigurationFile(configurationFileName);
		
		if(parameters==null)
		{
			System.out.println("Bad configuration file:"+configurationFileName);
			return;
		}
		
		if(parameters.containsKey("NumberOfNeighboorsForBuildingGraph") && !(parameters.containsKey("NumberOfNeighboursForBuildingGraph")))
		{
			parameters.put("NumberOfNeighboursForBuildingGraph", parameters.get("NumberOfNeighboorsForBuildingGraph"));
			parameters.remove("NumberOfNeighboorsForBuildingGraph");
			System.out.println("Warning: The attribute NumberOfNeighboorsForBuildingGraph is misspelled in the given configuration file. Continuing by using it as NumberOfNeighboursForBuildingGraph");
			System.out.println("");
		}
		
		var validAttributes=new String[] {"Name","ProjectionMethod","InputFileType","InputFileName",
				"OutputFolderName","InputFileClassColumnType","EvaluationNeighborhoodSize",
				"UmapTo30DimensionsFirst","NumberOfNeighboursForBuildingGraph","VisualDensityAdjustmentParameter",
				"CosineNeighborhoodNormalization","AfterUmapTo30DimensionsMaxRows","MaxInputRows",
				"NumberOfThreads","OverrideMaxNumberOfReplicates"};
		for(String parameter : parameters.keySet())
		{
			boolean validParameter=false;
			for(int i=0;i<validAttributes.length;i++)
				if(parameter.compareTo(validAttributes[i])==0)
					validParameter=true;
			
			if(!validParameter)
			{
				System.out.println("Error: Invalid attribute in the configuration file.");
				return;
			}
		}
		
		System.out.println("Configuration:"+parameters);
		
		boolean assortedNumberOfNeighboursForBuildingGraph=false;
		if(parameters.containsKey("NumberOfNeighboursForBuildingGraph"))
			if(parameters.get("NumberOfNeighboursForBuildingGraph").compareTo("assorted")==0)
				assortedNumberOfNeighboursForBuildingGraph=true;
		
		boolean assortedVisualDensityAdjustmentParameter=false;
		if(parameters.containsKey("VisualDensityAdjustmentParameter"))
			if(parameters.get("VisualDensityAdjustmentParameter").compareTo("assorted")==0)
				assortedVisualDensityAdjustmentParameter=true;
		
		String[] assortedList1=new String[] {"10","20","one-third","one-fifth"};
		String[] assortedList2=new String[] {"0.9","0.5","0.1","-0.1","-0.5","-0.9"};
		
		String outputFolderName=parameters.get("OutputFolderName");
		
		if(!assortedNumberOfNeighboursForBuildingGraph && !assortedVisualDensityAdjustmentParameter)
		{
			runBasedOnParameters(parameters);
			
			System.out.println("");
			System.out.println("Finished the configuration: "+ parameters.get("Name"));
			System.out.println("Output folder: "+ parameters.get("OutputFolderName"));
			System.out.println("");
		}
		else
		{
			String assortedFolderName=new File(new File(outputFolderName),"/assorted/").getCanonicalPath();
			new File(assortedFolderName).mkdirs();
			
			for(int i=0;i<assortedList1.length;i++)
			{	
				String folderName1=new String(assortedFolderName);
				
				if(assortedNumberOfNeighboursForBuildingGraph)
				{
					parameters.put("NumberOfNeighboursForBuildingGraph", assortedList1[i]);
					
					folderName1=new File(new File(assortedFolderName),"/NumberOfNeighboursForBuildingGraph_"+assortedList1[i]+"/").getCanonicalPath();
					new File(folderName1).mkdirs();
				}
				
				
				for(int j=0;j<assortedList2.length;j++)
				{
					String folderName2=new String(folderName1);
					
					if(assortedVisualDensityAdjustmentParameter)
					{
						parameters.put("VisualDensityAdjustmentParameter", assortedList2[j]);
						
						folderName2=new File(new File(folderName1),"/VisualDensityAdjustmentParameter_"+assortedList2[j]+"/").getCanonicalPath();
						new File(folderName2).mkdirs();	
					}
					
					parameters.put("OutputFolderName", folderName2);
					
					System.out.println("");
					System.out.println("Starting one of the assorted configurations of the configuration: "+ parameters.get("Name"));
										
					runBasedOnParameters((HashMap<String,String>)(parameters.clone()));
					
					System.out.println("");
					System.out.println("Finished one of the assorted configurations of the configuration: "+ parameters.get("Name"));
					System.out.println("Output folder: "+ parameters.get("OutputFolderName"));
					
					if(!assortedVisualDensityAdjustmentParameter)
						break;
				}
				
				if(!assortedNumberOfNeighboursForBuildingGraph)
					break;
			}
			
			System.out.println("");
			System.out.println("Finished the assorted configuration: "+ parameters.get("Name"));
			System.out.println("Output folder: "+ outputFolderName);
			System.out.println("");
		}
		
	}
	
	private static void runBasedOnParameters(HashMap<String,String> parameters) throws Exception
	{
		String projectionMethodName=parameters.get("ProjectionMethod");
		String inputFileType=parameters.get("InputFileType");
		String inputFileName=parameters.get("InputFileName");
		String outputFolderName=parameters.get("OutputFolderName");
		
		int neighborhoodSizeForEvaluation=10;
		if(parameters.containsKey("EvaluationNeighborhoodSize"))
			neighborhoodSizeForEvaluation=Integer.parseInt(parameters.get("EvaluationNeighborhoodSize"));
		
		CompactProjectionEvaluator.SetStaticNeighborhoodSize(neighborhoodSizeForEvaluation);
		
		if(projectionMethodName.toLowerCase().compareTo("red_gray_plus")==0 || projectionMethodName.toLowerCase().compareTo("redgrayplus")==0)
			projectionMethodName="RedGrayPlus";
		else
		{
			System.out.println("Bad input file:"+inputFileName);
			return;
		}
					
		
		boolean useUmapTo30DimensionsFirst=false;
		if(parameters.containsKey("UmapTo30DimensionsFirst"))
			if(parameters.get("UmapTo30DimensionsFirst").toLowerCase().compareTo("true")==0)
				useUmapTo30DimensionsFirst=true;
		
		boolean isClassColumnText=false;
		boolean isClassColumnFirst=false;
		
		if(parameters.get("InputFileClassColumnType").toLowerCase().compareTo("text")==0)
			isClassColumnText=true;
		
		if(parameters.get("InputFileClassColumnType").toLowerCase().compareTo("text_first_column")==0)
		{
			isClassColumnText=true;
			isClassColumnFirst=true;
		}
		
		if(parameters.get("InputFileClassColumnType").toLowerCase().compareTo("number_first_column")==0)
			isClassColumnFirst=true;
		
		
		CompactDataInstanceSet dataInstanceSet=null;
		
		if(inputFileType.toLowerCase().compareTo("csv_distance")==0)
		{
			var dataInstanceReader=new CompactDataInstanceReader();
			try
			{
				dataInstanceSet=dataInstanceReader.ReadDataSetCsvDistance(inputFileName, isClassColumnText,isClassColumnFirst);
			}
			catch (Exception e) {
				dataInstanceSet=null;
			}
		}
		else if(inputFileType.toLowerCase().compareTo("csv")==0 || inputFileType.toLowerCase().compareTo("csv_image")==0)
		{
			int ignoreRows=-1;
			if(parameters.containsKey("IgnoreRows"))
				ignoreRows=Integer.parseInt(parameters.get("IgnoreRows"));
			
			int maxInputRows=-1;
			if(parameters.containsKey("MaxInputRows"))
				maxInputRows=Integer.parseInt(parameters.get("MaxInputRows"));
			
			boolean isImageCsv=false;
			if(inputFileType.toLowerCase().compareTo("csv_image")==0)
				isImageCsv=true;
			
			
			var dataInstanceReader=new CompactDataInstanceReader();
			try
			{
				dataInstanceSet=dataInstanceReader.ReadDataSetCsv(inputFileName, maxInputRows, ignoreRows, isClassColumnText,isClassColumnFirst);
				if(isImageCsv)
					dataInstanceSet.SetIsImageDataSet(true);
			}
			catch (Exception e) {
				dataInstanceSet=null;
			}
		}
		else if(inputFileType.toLowerCase().startsWith("sparse"))
		{	
			var dataInstanceReader=new CompactDataInstanceReader();
			try
			{
				dataInstanceSet=dataInstanceReader.ReadDataSetSparse(inputFileName);
			}
			catch (Exception e) {
				dataInstanceSet=null;
			}
		}
		
		if(dataInstanceSet==null)
		{
			System.out.println("Bad input file:"+inputFileName);
			return;
		}
		
		if(useUmapTo30DimensionsFirst)
		{
			inputFileName=CompactScriptRunner.RunUmapTo30Dimensions(inputFileName, parameters, dataInstanceSet);
			parameters.put("InputFileName", inputFileName);
			var dataInstanceReader=new CompactDataInstanceReader();
			boolean isImageCsv=false;
			if(inputFileType.toLowerCase().compareTo("csv_image")==0)
				isImageCsv=true;
			dataInstanceSet=dataInstanceReader.ReadExternalProjectedDataSetAsInput(inputFileName);
			if(isImageCsv)
				dataInstanceSet.SetIsImageDataSet(isImageCsv);
		}
				
		CompactProjectionMethod projectionMethod=null;
		
		if(parameters.containsKey("NumberOfThreads") &&
				Integer.parseInt(parameters.get("NumberOfThreads"))==1)
			projectionMethod=new RedGrayPlusProjectionMethodSingleThreaded();
		else
			projectionMethod=new RedGrayPlusProjectionMethodMultiThreaded();
		
		dataInstanceSet.SetIsRedGray(true);
		
		new File(outputFolderName).mkdirs();
		
		dataInstanceSet.SetDataInstanceIndices();
		
		dataInstanceSet.PreComputeDistances();
		dataInstanceSet.PreComputeDistancesForEvaluation();
		
		dataInstanceSet.ComputeNeighborsForEvaluation(neighborhoodSizeForEvaluation);
		
		if(parameters.getOrDefault("CosineNeighborhoodNormalization", "false").toLowerCase().compareTo("true")==0)
			dataInstanceSet.TransformPreComputedDistances("Cosine for original space");
					
		dataInstanceSet.TransformPreComputedDistances("Neighbourhood normalized for original space");
		
		
		var projectionOutput=projectionMethod.Project(dataInstanceSet, parameters);
		
		double projectionFrameWidth=1500;
		
		projectionOutput.NormalizeToSize(projectionFrameWidth, projectionFrameWidth, true);
		projectionOutput.CheckNonFiniteValues();
		
		
		var projectionSteps=projectionOutput.GetProjectionSteps();
		CompactProjectionStep lastStep,bestRedAndGrayTrustworthinessStep,bestRedTrustworthinessStep;
		lastStep=projectionSteps.get(projectionSteps.size()-1);
		int bestRedAndGrayTrustworthinessStepIndex=0;
		bestRedAndGrayTrustworthinessStep=projectionSteps.get(0);
		int bestRedTrustworthinessStepIndex=0;
		bestRedTrustworthinessStep=projectionSteps.get(0);
				
		for(int i=0;i<projectionSteps.size();i++)
		{
			var projectionStep=projectionSteps.get(i);
			if(projectionStep.GetRedAndGrayTrustworthiness()>bestRedAndGrayTrustworthinessStep.GetRedAndGrayTrustworthiness())
			{
				bestRedAndGrayTrustworthinessStep=projectionStep;
				bestRedAndGrayTrustworthinessStepIndex=i;
			}
			
			if(projectionStep.GetRedTrustworthiness()>bestRedTrustworthinessStep.GetRedTrustworthiness())
			{
				bestRedTrustworthinessStep=projectionStep;
				bestRedTrustworthinessStepIndex=i;
			}			
		}
		
		projectionOutput.SetDataInstanceSet(dataInstanceSet);
		
		
		outputProjectionStep(lastStep,outputFolderName,"RedGrayPlus_Iteration"+(projectionSteps.size()-1)+"_LastIteration", projectionOutput);
		outputProjectionStep(bestRedAndGrayTrustworthinessStep,outputFolderName,"RedGrayPlus_Iteration"+bestRedAndGrayTrustworthinessStepIndex+"_BestRedAndGrayTrustworthiness", projectionOutput);
		outputProjectionStep(bestRedTrustworthinessStep,outputFolderName,"RedGrayPlus_Iteration"+bestRedTrustworthinessStepIndex+"_BestRedTrustworthiness", projectionOutput);
		
		
		String outputConfigurationFileName=new File(new File(outputFolderName),"/configuration.txt").getCanonicalPath();
		try(var printWriter=new PrintWriter(new File(outputConfigurationFileName)))
		{
			printWriter.write(parameters.toString());
		}
		catch (Exception e)
		{
			throw e;
		}
	}
	
	private static HashMap<String,String> processConfigurationFile(String configurationFileName)
	{
		try
		{
			// parse XML configuration file
			var parametersNamedNodeMap= DocumentBuilderFactory.newInstance().newDocumentBuilder().
					parse(new File( configurationFileName )).getDocumentElement().getAttributes();
			
			var parameters=new HashMap<String,String>();
			
			for(int i=0;i<parametersNamedNodeMap.getLength();i++)
				parameters.put(parametersNamedNodeMap.item(i).getNodeName(), parametersNamedNodeMap.item(i).getNodeValue());
			
			if(!(parameters.containsKey("Name")))
				return null;
			
			if(!(parameters.containsKey("ProjectionMethod")))
				return null;
			
			if(!(parameters.containsKey("InputFileType")))
				return null;
			
			if(!(parameters.containsKey("InputFileName")))
				return null;
			
			if(!(parameters.containsKey("OutputFolderName")))
				return null;
			
			if(!(parameters.containsKey("InputFileClassColumnType")))
				return null;
			
			
			parameters.put("InputFileName",new File(new File(configurationFileName).getParent(),parameters.get("InputFileName")).getCanonicalPath());
			parameters.put("OutputFolderName",new File(new File(configurationFileName).getParent(),parameters.get("OutputFolderName")).getCanonicalPath());
			
			return parameters;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	private static void outputProjectionStep(CompactProjectionStep projectionStep, String outputFolderName, String prefix, CompactProjectionOutput projectionOutput) throws IOException
	{
		var box=projectionOutput.GetContainingBox();
		int imageWidth=(int)box.GetSize(0)+100;
		int imageHeight=(int)box.GetSize(1)+100;
		
		var dataInstanceSet=projectionOutput.GetDataInstanceSet();
		
		String outputFileName=new File(new File(outputFolderName),"/"+prefix+".csv").getCanonicalPath();
		CompactProjectionStepWriter.WriteProjectionStep(projectionStep, outputFileName);
		
		outputFileName=new File(new File(outputFolderName),"/"+prefix+"_metrics.csv").getCanonicalPath();
		CompactProjectionStepWriter.WriteProjectionStepMetrics(projectionStep, outputFileName);
		
		if(dataInstanceSet.GetIsRedGray())
		{
			outputFileName=new File(new File(outputFolderName),"/"+prefix+"_coloring1.jpg").getCanonicalPath();
			
			var bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
			var graphics = bufferedImage.createGraphics();
			graphics.setColor(new Color(190,190,195));
			graphics.fillRect(0, 0, imageWidth, imageHeight);
			projectionStep.DrawOnGraphics(graphics, 50, imageWidth, imageHeight,CompactProjectionDisplayOptions.RED_GRAY, false, false, false, false);
			graphics.dispose();
			bufferedImage.flush();
	
			ImageIO.write(bufferedImage, "jpg", new File(outputFileName));
			
			outputFileName=new File(new File(outputFolderName),"/"+prefix+"_coloring2.jpg").getCanonicalPath();
			
			bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
			graphics = bufferedImage.createGraphics();
			graphics.setColor(new Color(190,190,195));
			graphics.fillRect(0, 0, imageWidth, imageHeight);
			projectionStep.DrawOnGraphics(graphics, 50, imageWidth, imageHeight,CompactProjectionDisplayOptions.RED_GRAY_CLASS_COLORED, false, false, false, false);
			graphics.dispose();
			bufferedImage.flush();
	
			ImageIO.write(bufferedImage, "jpg", new File(outputFileName));
			
			outputFileName=new File(new File(outputFolderName),"/"+prefix+"_coloring3.jpg").getCanonicalPath();
			
			bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
			graphics = bufferedImage.createGraphics();
			graphics.setColor(new Color(190,190,195));
			graphics.fillRect(0, 0, imageWidth, imageHeight);
			projectionStep.DrawOnGraphics(graphics, 50, imageWidth, imageHeight,CompactProjectionDisplayOptions.NORMAL, false, false, false, false);
			graphics.dispose();
			bufferedImage.flush();
	
			ImageIO.write(bufferedImage, "jpg", new File(outputFileName));
			
			if(dataInstanceSet.GetIsImageDataSet())
			{
				outputFileName=new File(new File(outputFolderName),"/"+prefix+"_coloring4.jpg").getCanonicalPath();
				
				bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
				graphics = bufferedImage.createGraphics();
				graphics.setColor(new Color(190,190,195));
				graphics.fillRect(0, 0, imageWidth, imageHeight);
				projectionStep.DrawOnGraphics(graphics, 50, imageWidth, imageHeight,CompactProjectionDisplayOptions.DIGITS_RED_GRAY, false, false, false, false);
				graphics.dispose();
				bufferedImage.flush();
		
				ImageIO.write(bufferedImage, "jpg", new File(outputFileName));
				
				outputFileName=new File(new File(outputFolderName),"/"+prefix+"_coloring5.jpg").getCanonicalPath();
				
				bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
				graphics = bufferedImage.createGraphics();
				graphics.setColor(new Color(190,190,195));
				graphics.fillRect(0, 0, imageWidth, imageHeight);
				projectionStep.DrawOnGraphics(graphics, 50, imageWidth, imageHeight,CompactProjectionDisplayOptions.DIGITS, false, false, false, false);
				graphics.dispose();
				bufferedImage.flush();
		
				ImageIO.write(bufferedImage, "jpg", new File(outputFileName));
			}	
		}		
	}
}
