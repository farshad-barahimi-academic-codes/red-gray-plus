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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class CompactDataInstanceReader
{	
	
	public CompactDataInstanceSet ReadExternalProjectedDataSetAsInput(String fileName) throws Exception
	{
		
		CompactDataInstanceSet dataInstanceSet=null;
		dataInstanceSet=new CompactDataInstanceSet();
		
		int numberOfOriginalFeatures=0;
		
		if (true)
		{
			var bufferReader = new BufferedReader(new FileReader(fileName));

			String line = "";
			
			var dataInstances=dataInstanceSet.GetDataInstances();
			
			boolean firstLine=true;
			
			while ((line = bufferReader.readLine()) != null)
			{	
				String delimiterRegex = ",";
				var tokens = line.split(delimiterRegex);

				if (tokens.length == 1)
					break;
				
				if(firstLine)
				{
					firstLine=false;
					
					for (int i = 0; i < tokens.length; i++)
					{
						var token = tokens[i];
						if(token.startsWith("ProjectedFeature_"))
						{
							numberOfOriginalFeatures=i-2;
							break;
						}
					}
					continue;
				}
				
				var dataInstance=new CompactDataInstance();

				for (int i = 0; i < tokens.length; i++)
				{
					var token = tokens[i];
					
					if(i==0)
					{
						
					}
					else if(i==1)
					{
						int classNumber=Integer.parseInt(token);
						dataInstance.GetClasses().add(classNumber);
					}
					else if(i>=2 && i<2+numberOfOriginalFeatures)
					{
						double feature=Double.parseDouble(token);
						dataInstance.GetFeaturesForEvaluation().add(feature);
					}
					else if(i>1)
					{
						double feature=Double.parseDouble(token);
						dataInstance.GetFeatures().add(feature);
					}
				}
				
				dataInstances.add(dataInstance);
			}

			bufferReader.close();
		}
		
		return dataInstanceSet;
	}

	public CompactDataInstanceSet ReadDataSetCsv(String fileName,int maxInputRows, int ignoreRows, boolean isClassColumnText, boolean isClassColumnFirst) throws Exception
	{	
		var dataInstanceSet=new CompactDataInstanceSet();
		
		var bufferReader = new BufferedReader(new FileReader(fileName));

		String line = "";
		
		var categoryToInt=new HashMap<String,Integer>();
					
		
		var dataInstances=dataInstanceSet.GetDataInstances();
		
		int lineNumber=0;
		
		while ((line = bufferReader.readLine()) != null)
		{
			lineNumber++;
			
			CompactDataInstance dataInstance=null;
			
			String delimiterRegex = ",";
			var tokens = line.split(delimiterRegex);

			if (tokens.length == 1)
				break;
			
			if(maxInputRows!=-1 && dataInstances.size()==maxInputRows)
				break;
			
			if(ignoreRows!=-1 && lineNumber<=ignoreRows)
				continue;
			
			dataInstance = new CompactDataInstance();

			for (int i = 0; i < tokens.length; i++)
			{
				var token = tokens[i];
				if((!isClassColumnFirst && i==tokens.length-1) || (isClassColumnFirst && i==0))
				{
					if(isClassColumnText)
					{
						if(!categoryToInt.containsKey(token))
							categoryToInt.put(token, categoryToInt.size());
						
						dataInstance.GetClasses().add(categoryToInt.get(token));
					}
					else
						dataInstance.GetClasses().add(Integer.parseInt(token));
				}
				else
				{
					dataInstance.GetFeatures().add(Double.parseDouble(token));
				}
								
			}
			
			dataInstances.add(dataInstance);
		}

		bufferReader.close();
		
		
		return dataInstanceSet;
	}
	
	public CompactDataInstanceSet ReadDataSetCsvDistance(String fileName, boolean isClassColumnText, boolean isClassColumnFirst) throws Exception
	{	
		
				
		var bufferReader = new BufferedReader(new FileReader(fileName));

		String line = "";
		
		var categoryToInt=new HashMap<String,Integer>();
					
		CompactDataInstanceSet dataInstanceSet=null;
		
		ArrayList<CompactDataInstance> dataInstances=null;
		
		int lineNumber=0;
		
		while ((line = bufferReader.readLine()) != null)
		{
			lineNumber++;
			
			CompactDataInstance dataInstance=null;
			if(lineNumber==1)
			{ 
				var tokens = line.split(",");
				
				dataInstanceSet=new CompactDataInstanceSet(tokens.length-1);
				dataInstances=dataInstanceSet.GetDataInstances();
				
				for(int i=0;i<tokens.length-1;i++)
				{
					dataInstance=new CompactDataInstance();
					dataInstances.add(dataInstance);
				}
			}
			
			dataInstance=dataInstances.get(lineNumber-1);
						
			String delimiterRegex = ",";
			var tokens = line.split(delimiterRegex);

			if (tokens.length == 1)
				break;
			

			for (int i = 0; i < tokens.length; i++)
			{
				var token = tokens[i];
				if((!isClassColumnFirst && i==tokens.length-1) || (isClassColumnFirst && i==0))
				{
					if(isClassColumnText)
					{
						if(!categoryToInt.containsKey(token))
							categoryToInt.put(token, categoryToInt.size());
						
						dataInstance.GetClasses().add(categoryToInt.get(token));
					}
					else
						dataInstance.GetClasses().add(Integer.parseInt(token));
				}
				else
				{
					dataInstanceSet.SetDisSimilarity(lineNumber-1, i, Double.parseDouble(token));
				}		
			}
		}

		bufferReader.close();
		
		dataInstanceSet.SetDataInstanceIndices();
		dataInstanceSet.SetUseDissimilarityInsteadOfDistance(true);
		dataInstanceSet.SymmetrizeDisSimmilarities();
		
		return dataInstanceSet;
	}
	
	public CompactDataInstanceSet ReadDataSetSparse(String fileName) throws Exception
	{
		
		CompactDataInstanceSet dataInstanceSet=null;
		dataInstanceSet=new CompactDataInstanceSet();
		
		
		var bufferReader = new BufferedReader(new FileReader(fileName));
		
		bufferReader.readLine();
		bufferReader.readLine();
		int numberOfAttributes = Integer.parseInt(bufferReader.readLine());
		bufferReader.readLine();
		

		String line = "";
		var dataInstances=dataInstanceSet.GetDataInstances();
		
		var dataInstance=new CompactDataInstance();
		for(int i=0;i<numberOfAttributes;i++)
			dataInstance.GetFeatures().add(0.0);
		
		while ((line = bufferReader.readLine()) != null)
		{	
			
			String delimiterRegex = ";";
			var tokens = line.split(delimiterRegex);

			if (tokens.length == 1)
				break;

			for (int i = 1; i < tokens.length-1; i++)
			{
				var token = tokens[i];
				
				int index=Integer.parseInt(token.split(":")[0]);
				double value=Double.parseDouble(token.split(":")[1]);
				dataInstance.GetFeatures().set(index,value);
			}
			
			dataInstance.GetClasses().add((int)(Double.parseDouble(tokens[tokens.length-1])+0.1));
			
			dataInstances.add(dataInstance);
			dataInstance = new CompactDataInstance();
			for(int i=0;i<numberOfAttributes;i++)
				dataInstance.GetFeatures().add(0.0);
		}

		bufferReader.close();
		
		return dataInstanceSet;
	}
}
