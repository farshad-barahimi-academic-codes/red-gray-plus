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
import java.util.Comparator;
import java.util.PriorityQueue;

public class CompactProjectedPoint
{
	private double x_;
	private double y_;
	private CompactDataInstance dataInstance_;
	private int projectionIndex_;
	private double additionalX_;
	private double additionalY_;
	private ArrayList<CompactProjectedPoint> neighbors_;
	private ArrayList<Double> positivePressures_;
	private ArrayList<Double> negativePressures_;
	private int tempCloneIndex_;
	boolean isFrozen_;
	boolean isInEffective_;
	boolean hasReplicationFailed_;
	boolean isGray_;
	double effectiveWeight_;
	
	public double GetDistance(CompactProjectedPoint projectedPoint)
	{
		return Math.sqrt(this.GetDistanceSquared(projectedPoint));
	}
	
	public double GetDistanceSquared(CompactProjectedPoint projectedPoint)
	{
		return Math.pow(projectedPoint.GetX()-this.GetX(), 2)+Math.pow(projectedPoint.GetY()-this.GetY(), 2);
	}
	
	public double GetX()
	{
		return x_;
	}
	
	public double GetY()
	{
		return y_;
	}
	
	public void SetX(double x) throws Exception
	{
		x_=x;
		
		//if(!(Double.isFinite(x_))) throw new Exception("NAN or infinite value");
	}
	
	public void SetY(double y) throws Exception
	{
		y_=y;
		
		//if(!(Double.isFinite(x_))) throw new Exception("NAN or infinite value");
	}
	
	public CompactProjectedPoint(double x, double y, CompactDataInstance dataInstance)
	{
		this(x,y,dataInstance,-1);
	}
	
	public CompactProjectedPoint(double x, double y, CompactDataInstance dataInstance, int projectionIndex)
	{
		x_=x;
		y_=y;
		dataInstance_=dataInstance;
		projectionIndex_=projectionIndex;
		additionalX_=0;
		additionalY_=0;
		neighbors_=new ArrayList<CompactProjectedPoint>();
		positivePressures_=new ArrayList<Double>();
		negativePressures_=new ArrayList<Double>();
		tempCloneIndex_=0;
		isFrozen_=false;
		isInEffective_=false;
		hasReplicationFailed_=false;
		isGray_=false;
		effectiveWeight_=1.0;
	}
	
	public CompactDataInstance GetDataInstance()
	{
		return dataInstance_;
	}
	
	public CompactProjectedPoint Clone()
	{
		return this.Clone(false);
	}
	
	public CompactProjectedPoint Clone(boolean cloneNeighborsShallow)
	{
		var projectedPoint=new CompactProjectedPoint(x_, y_, dataInstance_, projectionIndex_);
		for(int i=0;i<positivePressures_.size();i++)
			projectedPoint.positivePressures_.add(positivePressures_.get(i));
		
		for(int i=0;i<negativePressures_.size();i++)
			projectedPoint.negativePressures_.add(negativePressures_.get(i));
		
		projectedPoint.SetIsInEffective(isInEffective_);
		projectedPoint.isGray_=isGray_;
		projectedPoint.hasReplicationFailed_=hasReplicationFailed_;
		
		if(cloneNeighborsShallow)
			projectedPoint.neighbors_.addAll(neighbors_);
		
		return projectedPoint;
	}
	
	public double GetDistanceExtended(CompactProjectedPoint projectedPoint)
	{
		double minimumDistance=this.GetDistance(projectedPoint);
		var siblingProjectedPoints=projectedPoint.GetDataInstance().GetProjectedPoints();
		
		for(int i=0;i<siblingProjectedPoints.size();i++)
		{
			var siblingProjectedPoint=siblingProjectedPoints.get(i);
			double distance=this.GetDistance(siblingProjectedPoint);
			minimumDistance=Math.min(minimumDistance,distance);
		}
		
		return minimumDistance;
	}
	
	public int GetProjectionIndex()
	{
		return projectionIndex_;
	}
	
	public void SetProjectionIndex(int projectionIndex)
	{
		projectionIndex_=projectionIndex;
	}
	
	public double GetAdditionalX()
	{
		return additionalX_;
	}
	
	public double GetAdditionalY()
	{
		return additionalY_;
	}
	
	public void SetAdditionalX(double additionalX)
	{
		additionalX_=additionalX;
	}
	
	public void SetAdditionalY(double additionalY)
	{
		additionalY_=additionalY;
	}
	
	public ArrayList<CompactProjectedPoint> GetNeighbors()
	{
		return neighbors_;
	}
	
	
	public void AddToPressures(double pressure,int angleIndex)
	{
		while(positivePressures_.size()<=angleIndex)
			positivePressures_.add(0.0);
		
		while(negativePressures_.size()<=angleIndex)
			negativePressures_.add(0.0);
		
		if(pressure>0)
			positivePressures_.set(angleIndex, positivePressures_.get(angleIndex)+pressure);
		else
			negativePressures_.set(angleIndex, negativePressures_.get(angleIndex)-pressure);
	}
	
	public void ResetPressures()
	{
		for(int i=0;i<positivePressures_.size();i++)
			positivePressures_.set(i, 0.0);
		
		for(int i=0;i<negativePressures_.size();i++)
			positivePressures_.set(i, 0.0);
	}
	
	public double ReplicationPressure(int angleIndex)
	{
		while(positivePressures_.size()<=angleIndex)
			positivePressures_.add(0.0);
		
		while(negativePressures_.size()<=angleIndex)
			negativePressures_.add(0.0);
		
		if(dataInstance_.GetProjectedPoints().size()>1)
			return -1;
		
		//double pressure=Math.min(positivePressures_.get(angleIndex), negativePressures_.get(angleIndex));
		double pressure=positivePressures_.get(angleIndex)+ negativePressures_.get(angleIndex);
		return pressure;
	}
	
	public double MaximumReplicationPressure()
	{
		double maximumReplicationPressure=0;
		
		for(int i=0;i<positivePressures_.size();i++)
			maximumReplicationPressure=Math.max(positivePressures_.get(i)+ negativePressures_.get(i),maximumReplicationPressure);
		return maximumReplicationPressure;
	}
	
	public CompactProjectedPoint ReplicateBasedOnAngles() throws Exception
	{
		double maxSum=0;
		double selectedAngle1=0;
		double selectedAngle2=0;
		
		boolean failed=true;
		
		for(int angleIndex1=0;angleIndex1<36;angleIndex1++)
		{
			double angle1=(Math.PI/18.0)*angleIndex1;
			
			int angleIndex2=(angleIndex1+18)%36;
			
			double angle2=(Math.PI/18.0)*angleIndex2;
			
			if(negativePressures_.get(angleIndex1)+negativePressures_.get(angleIndex2)>maxSum)
			{
				failed=false;
				selectedAngle1=angle1;
				selectedAngle2=angle2;
				maxSum=negativePressures_.get(angleIndex1)+negativePressures_.get(angleIndex2);
			}
		}
		
		if(failed)
		{
			hasReplicationFailed_=true;
			return null;
		}
		
		double intialCount=neighbors_.size();
		
		var firstNeighbors=this.GetNeighborsBasedOnAngle(selectedAngle1, false, neighbors_.size(),null);
		var secondNeighbors=this.GetNeighborsBasedOnAngle(selectedAngle2, false, neighbors_.size(),null);
		
		
		if(secondNeighbors.size()<1 || firstNeighbors.size()<1)
		{
			hasReplicationFailed_=true;
			return null;
		}
		
		var newProjectedPoint=this.Clone();
		newProjectedPoint.GetDataInstance().GetProjectedPoints().add(newProjectedPoint);
		
		neighbors_.clear();
		neighbors_.addAll(firstNeighbors);
		newProjectedPoint.GetNeighbors().addAll(secondNeighbors);
		newProjectedPoint.SetProjectionIndex(projectionIndex_+1);
		
		
		double newX=0;
		double newY=0;
		for(int i=0;i<secondNeighbors.size();i++)
		{
			newX+=secondNeighbors.get(i).GetX();
			newY+=secondNeighbors.get(i).GetY();
		}
		newX/=secondNeighbors.size();
		newY/=secondNeighbors.size();
		
		newProjectedPoint.SetX(newX);
		newProjectedPoint.SetY(newY);
		
		var newNeighbors=new ArrayList<CompactProjectedPoint>();
		
		for(int i=0;i<neighbors_.size();i++)
		{
			var projectedPoint=neighbors_.get(i);
			if(projectedPoint.GetDistance(this)>projectedPoint.GetDistance(newProjectedPoint))
				newProjectedPoint.GetNeighbors().add(projectedPoint);
			else
				newNeighbors.add(projectedPoint);
		}
		
		neighbors_.clear();
		neighbors_.addAll(newNeighbors);
		
		newProjectedPoint.effectiveWeight_=(effectiveWeight_*intialCount)/secondNeighbors.size();
		effectiveWeight_*=intialCount/neighbors_.size();
				
		for(int i=0;i<newProjectedPoint.GetNeighbors().size();i++)
		{
			var projectedPoint1=newProjectedPoint.GetNeighbors().get(i);
			for(int j=0;j<projectedPoint1.GetNeighbors().size();j++)
			{
				var projectedPoint2=projectedPoint1.GetNeighbors().get(j);
				if(projectedPoint2==this)
					projectedPoint1.GetNeighbors().set(j, newProjectedPoint);
			}
		}
		
		return newProjectedPoint;
	}
	
	ArrayList<CompactProjectedPoint> GetNeighborsBasedOnAngle(double angle,boolean positive,int count, CompactDataInstanceSet dataInstanceSet)
	{
		var newNeighbors=new ArrayList<CompactProjectedPoint>();
		
		double vec1X=Math.cos(angle);
		double vec1Y=Math.sin(angle);
		
		for(int i=0;i<neighbors_.size();i++)
		{
			var projectedPoint=neighbors_.get(i);
			
			double vec2X=projectedPoint.GetX()-this.GetX();
			double vec2Y=projectedPoint.GetY()-this.GetY();
			
			double pressure1=(vec1X*vec2X+vec1Y*vec2Y);
			if(pressure1>0 && positive)
				newNeighbors.add(projectedPoint);
			else if(pressure1<0 && !positive)
				newNeighbors.add(projectedPoint);	
		}
		
		return newNeighbors;
	}
	
		
	public void SetTempCloneIndex(int cloneIndex)
	{
		tempCloneIndex_=cloneIndex;
	}
	
	public int GetTempCloneIndex()
	{
		return tempCloneIndex_;
	}
	
	public boolean IsFrozen()
	{
		return isFrozen_;
	}
	
	public void SetIsFrozen(boolean isFrozen)
	{
		isFrozen_=isFrozen;
	}
	
	public boolean IsInEffective()
	{
		return isInEffective_;
	}
	
	public void SetIsInEffective(boolean isIneffective)
	{
		isInEffective_=isIneffective;
		if(isIneffective)
			isGray_=true;
	}
	
	public boolean HasReplicationFailed()
	{
		return hasReplicationFailed_;
	}
	
	public boolean IsGray()
	{
		return isGray_;
	}
	
	public double GetEffectiveWeight()
	{
		return effectiveWeight_;
	}
	
	public ArrayList<Double> GetNegativePressures()
	{
		return negativePressures_;
	}
	
	public void SetReplicationHasFailed()
	{
		hasReplicationFailed_=true;
	}
}
