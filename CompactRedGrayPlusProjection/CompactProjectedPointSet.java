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
import java.util.TreeSet;

public class CompactProjectedPointSet
{
	ArrayList<CompactProjectedPoint> projectedPoints_;
	
	CompactDataInstanceSet dataInstanceSet_;
	
	private CompactProjectedPointSet()
	{
		projectedPoints_=new ArrayList<CompactProjectedPoint>();
	}
		
	public CompactProjectedPointSet(CompactDataInstanceSet dataInstanceSet)
	{
		this(dataInstanceSet, true, false);
	}
	public CompactProjectedPointSet(CompactDataInstanceSet dataInstanceSet, boolean clonePoints, boolean cloneNeighbors)
	{
		this();
		dataInstanceSet_=dataInstanceSet;
		
		var dataInstances=dataInstanceSet.GetDataInstances();
		for(int i=0;i<dataInstances.size();i++)
		{
			var dataInstance=dataInstances.get(i);
			for(int j=0;j<dataInstance.GetProjectedPoints().size();j++)
				if(clonePoints && !cloneNeighbors)
					projectedPoints_.add(dataInstance.GetProjectedPoints().get(j).Clone());
				else
					projectedPoints_.add(dataInstance.GetProjectedPoints().get(j));
		}
		
		if(cloneNeighbors)
		{
			var newProjectedPoints=new ArrayList<CompactProjectedPoint>();
			for(int i=0;i<projectedPoints_.size();i++)
			{
				projectedPoints_.get(i).SetTempCloneIndex(i);
				newProjectedPoints.add(projectedPoints_.get(i).Clone());
			}
			
			for(int i=0;i<projectedPoints_.size();i++)
			{
				var projectedPoint1=projectedPoints_.get(i);
				for(int j=0;j<projectedPoint1.GetNeighbors().size();j++)
				{
					var projectedPoint2=projectedPoint1.GetNeighbors().get(j);
					newProjectedPoints.get(i).GetNeighbors().add(newProjectedPoints.get(projectedPoint2.GetTempCloneIndex()));
				}
			}
			
			projectedPoints_=newProjectedPoints;
		}
	}
	
	public ArrayList<CompactProjectedPoint> GetProjectedPoints()
	{
		return projectedPoints_;
	}
	
	public CompactBox GetContainingBox()
	{
		var box=new CompactBox(2);
		var corner1=box.GetCorner1();
		var corner2=box.GetCorner2();
		
		corner1.set(0, Double.MAX_VALUE);
		corner1.set(1, Double.MAX_VALUE);
		corner2.set(0, Double.MIN_VALUE);
		corner2.set(1, Double.MIN_VALUE);
		
		for(int i=0;i<projectedPoints_.size();i++)
		{
			var projectedPoint=projectedPoints_.get(i);
			corner1.set(0, Math.min(projectedPoint.GetX(), corner1.get(0)));
			corner1.set(1, Math.min(projectedPoint.GetY(), corner1.get(1)));
			corner2.set(0, Math.max(projectedPoint.GetX(), corner2.get(0)));
			corner2.set(1, Math.max(projectedPoint.GetY(), corner2.get(1)));
		}
		
		return box;
	}
	
	public void NormalizeToSize(double width, double height, boolean uniformScale) throws Exception
	{
		var box = GetContainingBox();
		var corner1=box.GetCorner1();
		double boxWidth= box.GetSize(0);
		double boxHeight= box.GetSize(1);
		boxWidth=Math.max(boxWidth, 1e-9);
		boxHeight=Math.max(boxHeight, 1e-9);
		double scaleX=width/boxWidth;
		double scaleY=height/boxHeight;
		if(uniformScale)
		{
			scaleX=Math.min(scaleX, scaleY);
			scaleY=scaleX;
		}
		
		for(int i=0;i<projectedPoints_.size();i++)
		{
			var projectedPoint=projectedPoints_.get(i);
			projectedPoint.SetX((projectedPoint.GetX()-corner1.get(0)) *scaleX);
			projectedPoint.SetY((projectedPoint.GetY()-corner1.get(1)) *scaleY);
		}
	}
	
	public CompactProjectedPointSet Clone()
	{
		var projectedPointSet=new CompactProjectedPointSet();
		projectedPointSet.dataInstanceSet_=dataInstanceSet_;
		
		var projectedPoints=projectedPointSet.GetProjectedPoints();
		
		for(int i=0;i<projectedPoints_.size();i++)
			projectedPoints.add(projectedPoints_.get(i).Clone());
		
		return projectedPointSet;
	}
	
	public CompactDataInstanceSet GetDataInstanceSet()
	{
		return dataInstanceSet_;
	}
	
	public double GetMaximumDistance()
	{
		double maximumDistance=0;
		for(int i=0;i<projectedPoints_.size();i++)
		{
			for(int j=i+1;j<projectedPoints_.size();j++)
			{
				double distance=projectedPoints_.get(i).GetDistance(projectedPoints_.get(j));
				maximumDistance=Math.max(distance, maximumDistance);
			}
		}
		
		return maximumDistance;
	}
	
	public int GetPressureOutlierCount()
	{
		double mean=0;
		
		for(int i=0;i<projectedPoints_.size();i++)
			mean+=projectedPoints_.get(i).MaximumReplicationPressure();
				
		mean/=projectedPoints_.size();
		
		double standardDeviation=0;
		
		for(int i=0;i<projectedPoints_.size();i++)
			standardDeviation+=Math.pow(projectedPoints_.get(i).MaximumReplicationPressure()-mean,2);
		
		standardDeviation/=projectedPoints_.size()-1;
		standardDeviation=Math.sqrt(standardDeviation);
		
		int outlierCount=0;
		for(int i=0;i<projectedPoints_.size();i++)
			if(Math.abs(projectedPoints_.get(i).MaximumReplicationPressure()-mean)>1.2*standardDeviation)
				outlierCount++;
		
		outlierCount=Math.min(outlierCount, (int)(dataInstanceSet_.GetDataInstances().size()/4));
		
		return outlierCount;
	}
}