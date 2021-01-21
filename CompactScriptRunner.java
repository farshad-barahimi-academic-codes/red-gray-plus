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

package CompactRedGrayPlusProjection;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;


public class CompactScriptRunner
{
	public static String RunUmapTo30Dimensions(String fileName, HashMap<String,String> parameters, CompactDataInstanceSet dataInstanceSet) throws IOException, InterruptedException
	{
		String outputFolderName=parameters.get("OutputFolderName");
		String tempFolderName=new File(new File(outputFolderName),"/Temp/").getCanonicalPath();
		new File(tempFolderName).mkdirs();
		
		String tempFileName1=new File(new File(tempFolderName),"/input_temp.csv").getCanonicalPath();
		
		boolean isDistanceInput=false;
		if(parameters.get("InputFileType").toLowerCase().compareTo("csv_distance")==0)
			isDistanceInput=true;
		
		var dataInstances=dataInstanceSet.GetDataInstances();
		
		int numberOfDimensions=dataInstanceSet.GetDataInstances().get(0).GetFeatures().size();
		if(isDistanceInput)
			numberOfDimensions=dataInstances.size();
		
		try(var printWriter=new PrintWriter(new File(tempFileName1)))
		{
			for(int i=0;i<dataInstances.size();i++)
			{
				var dataInstance=dataInstances.get(i);
				
				if(dataInstance.GetProjectedPoints().size()>0 && dataInstance.GetProjectedPoints().get(0).IsGray())
					printWriter.write("1,");
				else
					printWriter.write("0,");
				
				if(isDistanceInput)
				{
					for(int j=0;j<dataInstances.size();j++)
						printWriter.write(dataInstance.DistanceTo(dataInstances.get(j))+",");
				}
				else
				{
					for(int j=0;j<dataInstance.GetFeatures().size();j++)
						printWriter.write(dataInstance.GetFeatures().get(j)+",");
				}
				
				printWriter.write(dataInstance.GetClass(0)+"\n");
			}
		}
		catch (Exception e)
		{
			return null;
		}
		
		String tempFileName2=new File(new File(tempFolderName),"/umap_30dim_temp.py").getCanonicalPath();
		String tempFileName3=new File(new File(tempFolderName),"/umap_30dim_temp_projected.csv").getCanonicalPath();
		
		int sampleSize=-1;
		if(parameters.containsKey("SampleSize"))
			sampleSize=Integer.parseInt(parameters.get("SampleSize"));
		
		try(var printWriter=new PrintWriter(new File(tempFileName2)))
		{
			
			printWriter.write(umapTo30DimensionsScript(tempFileName1, 
					numberOfDimensions, tempFileName3, sampleSize, isDistanceInput));
		}
		catch (Exception e)
		{
			return null;
		}
		
		
		try
		{
			System.out.println("");
			System.out.println("Running script that calls UMAP to 30 dimensions");
			
			Process process = Runtime.getRuntime().exec(new String[]{"python", tempFileName2});
			
			var bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line=null;

	        while((line=bufferReader.readLine()) != null)
	            System.out.println(line);
	        
			int exitCode=process.waitFor();
			System.out.println("Finished running script that calls UMAP to 30 dimensions");
			System.out.println("");
		}
		catch (Exception e) {
			System.out.println("Error");
			System.out.println(e.getMessage());
		}
				
		return tempFileName3;
	}
		
	private static String umapTo30DimensionsScript(String inputFileName,int numberOfInputDimensions, String outputFileName,int sampleSize, boolean isDistanceInput)
	{
		var stringBuilder=new StringBuilder();
		stringBuilder.append("import numpy\n");
		stringBuilder.append("import pandas\n");
		stringBuilder.append("import sklearn\n");
		stringBuilder.append("import sklearn.datasets\n");
		stringBuilder.append("import sklearn.manifold\n");
		stringBuilder.append("import umap\n");
		stringBuilder.append("    \n");
		
		stringBuilder.append("def applyProjectionMethodAsInput(dataIsGray,dataFeatures,dataClass,projectionMethod):\n");
		stringBuilder.append("    dataIsGrayDataFrame=pandas.DataFrame({'dataIsGray':dataIsGray})\n");
		stringBuilder.append("    dataClassDataFrame=pandas.DataFrame({'dataClass':dataClass})\n");
		stringBuilder.append("    transformedData= projectionMethod.fit_transform(dataFeatures)\n");
		stringBuilder.append("    transformedDataColumns=[]\n");
		stringBuilder.append("    for i in range(0,transformedData.shape[1]):\n");
		stringBuilder.append("        transformedDataColumns.append('ProjectedFeature_'+str(i+1))\n");
		stringBuilder.append("    originalDataColumns=[]\n");
		stringBuilder.append("    for i in range(0,dataFeatures.shape[1]):\n");
		stringBuilder.append("        originalDataColumns.append('Feature_'+str(i+1))\n");
		stringBuilder.append("    transformedDataFrame=pandas.DataFrame(data=transformedData, columns=transformedDataColumns);\n");
		stringBuilder.append("    originalDataFrame=pandas.DataFrame(data=dataFeatures, columns=originalDataColumns);\n");
		stringBuilder.append("    outputDataFrame=pandas.concat([dataIsGrayDataFrame,dataClassDataFrame,originalDataFrame,transformedDataFrame],axis=1)\n");
		if(sampleSize!=-1)
		{
			stringBuilder.append("    outputDataFrame=outputDataFrame.head("+sampleSize+")\n");
		}
		stringBuilder.append("    return outputDataFrame\n");
		stringBuilder.append("    \n");
		
		stringBuilder.append("dataSetFileName=r'"+inputFileName+"'\n");
		stringBuilder.append("data = pandas.read_csv(dataSetFileName, header=None)\n");
		stringBuilder.append("dataIsGray=data.iloc[:,:1].to_numpy().reshape(data.shape[0])\n");
		stringBuilder.append("dataFeatures=data.iloc[:,1:"+(numberOfInputDimensions+1)+"].to_numpy()\n");
		stringBuilder.append("dataClass=data.iloc[:,"+(numberOfInputDimensions+1)+":].to_numpy().reshape(data.shape[0])\n");
		if(isDistanceInput)
			stringBuilder.append("projectionMethod= umap.UMAP(random_state=10, n_components=30, metric=\"precomputed\")\n");
		else
			stringBuilder.append("projectionMethod= umap.UMAP(random_state=10, n_components=30)\n");
		stringBuilder.append("outputDataFrame=applyProjectionMethodAsInput(dataIsGray,dataFeatures,dataClass,projectionMethod)\n");
		stringBuilder.append("outputDataFrame.to_csv(r'"+outputFileName+"', float_format='%.20f', index=False)\n");
		
												
		return stringBuilder.toString();
	}
}
