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

import java.util.ArrayList;

public class CompactProjectionOutput
{
	private ArrayList<CompactProjectionStep> projectionSteps_;
	private String name_;
	private CompactDataInstanceSet dataInstanceSet_;
	
	public ArrayList<CompactProjectionStep> GetProjectionSteps()
	{
		return projectionSteps_;
	}
	
	public String GetName()
	{
		return name_;
	}
	
	public CompactProjectionOutput(String name, ArrayList<CompactProjectionStep> projectionSteps)
	{
		this(name,projectionSteps,false);
	}
	
	public CompactProjectionOutput(String name, ArrayList<CompactProjectionStep> projectionSteps, boolean displayKeys)
	{
		name_=name;
		projectionSteps_=projectionSteps;
		dataInstanceSet_=null;
	}
	
	public void NormalizeToSize(double width, double height, boolean uniformScale) throws Exception
	{
		for(int i=0;i<projectionSteps_.size();i++)
		{
			projectionSteps_.get(i).GetProjectedPointSet().NormalizeToSize(width, height, uniformScale);
		}
	}
	
	public CompactBox GetContainingBox()
	{
		var box=new CompactBox(2);
		var corner1=box.GetCorner1();
		var corner2=box.GetCorner2();
		for(int i=0;i<projectionSteps_.size();i++)
		{
			var stepBox=projectionSteps_.get(i).GetProjectedPointSet().GetContainingBox();
			var stepCorner1=stepBox.GetCorner1();
			var stepCorner2=stepBox.GetCorner2();
			
			corner1.set(0, Math.min(corner1.get(0), stepCorner1.get(0)));
			corner1.set(1, Math.min(corner1.get(1), stepCorner1.get(1)));
			
			corner2.set(0, Math.max(corner2.get(0), stepCorner2.get(0)));
			corner2.set(1, Math.max(corner2.get(1), stepCorner2.get(1)));
		}
		
		return box;
	}
	
	public void SetName(String name)
	{
		name_=name;
	}
	
	public void CheckNonFiniteValues() throws Exception
	{
		for(int i=0;i<projectionSteps_.size();i++)
		{
			var projectionStepPoints=projectionSteps_.get(i).GetProjectedPointSet().GetProjectedPoints();
			
			for(int j=0;j<projectionStepPoints.size();j++)
			{
				var projectedPoint=projectionStepPoints.get(j);
				double x=projectedPoint.GetX();
				double y=projectedPoint.GetY();
				
				if(!(Double.isFinite(x)) || !(Double.isFinite(y)))
					throw new Exception("NAN or infinite value at step "+ (i+1));
			}
		}
	}
	
	public void SetDataInstanceSet(CompactDataInstanceSet dataInstanceSet)
	{
		dataInstanceSet_=dataInstanceSet;
	}
	
	public CompactDataInstanceSet GetDataInstanceSet()
	{
		return dataInstanceSet_;
	}
}
