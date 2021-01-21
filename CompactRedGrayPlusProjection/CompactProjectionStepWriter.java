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

import java.io.File;
import java.io.PrintWriter;

public class CompactProjectionStepWriter
{
	public static boolean WriteProjectionStep(CompactProjectionStep projectionStep, String fileName)
	{
		try(var printWriter=new PrintWriter(new File(fileName)))
		{
			var projectedPoints=projectionStep.GetProjectedPointSet().GetProjectedPoints();
			for(int i=0;i<projectedPoints.size();i++)
			{
				var projectedPoint=projectedPoints.get(i);
				printWriter.write(projectedPoint.GetX()+",");
				printWriter.write(projectedPoint.GetY()+",");
				if(projectedPoint.IsGray())
					printWriter.write("gray,");
				else
					printWriter.write("red,");
				
				
				var dataInstance=projectedPoint.GetDataInstance();
				
				if(dataInstance.GetFeaturesForEvaluation().size()>0)
				{
					for(int j=0;j<dataInstance.GetFeaturesForEvaluation().size();j++)
						printWriter.write(dataInstance.GetFeaturesForEvaluation().get(j)+",");
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
			return false;
		}
		
		return true;
	}
	
	public static boolean WriteProjectionStepMetrics(CompactProjectionStep projectionStep, String fileName)
	{
		try(var printWriter=new PrintWriter(new File(fileName)))
		{
			printWriter.write("RedAndGrayTrustworthiness,RedTrustworthiness\n");
			
			printWriter.write(String.format("%.3f",projectionStep.GetRedAndGrayTrustworthiness())+",");	
			printWriter.write(String.format("%.3f",projectionStep.GetRedTrustworthiness())+",");
		}
		catch (Exception e)
		{
			return false;
		}
		
		return true;
	}

}
