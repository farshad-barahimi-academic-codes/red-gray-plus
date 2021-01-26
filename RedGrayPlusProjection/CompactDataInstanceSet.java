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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;


public class CompactDataInstanceSet
{
	private ArrayList<CompactDataInstance> dataInstances_;
	private double[][] disSimilarities_;
	private boolean useDissimilarityInsteadOfDistance_;
	private boolean isDistancesPreComputed_;
	private boolean isDistancesForEvaluationPreComputed_;
	private double[][] distances_;
	private double[][] distancesForEvaluation_;
	private boolean isRedGray_;
	private boolean isImageDataInstanceSet_;
		
	public ArrayList<CompactDataInstance> GetDataInstances()
	{
		return dataInstances_;
	}
	
	public double[][] GetDisSimiliraties()
	{
		return disSimilarities_;
	}
	
	public void SetDisSimilarity(int i,int j, double disSimilarity)
	{
		disSimilarities_[i][j]=disSimilarity;
	}
	
	public CompactDataInstanceSet()
	{
		this(1);
	}
	
	public CompactDataInstanceSet(int disSimiliratiesSize)
	{
		dataInstances_=new ArrayList<CompactDataInstance>();
		
		useDissimilarityInsteadOfDistance_=false;
		isDistancesPreComputed_=false;
		
		disSimilarities_=new double[disSimiliratiesSize][disSimiliratiesSize];
		for(int i=0;i<disSimiliratiesSize;i++)
		{
			for(int j=0;j<disSimiliratiesSize;j++)
				disSimilarities_[i][j]=0;
		}
		
		isRedGray_=false;
		isImageDataInstanceSet_=false;
	}
	
	public boolean GetUseDissimilarityInsteadOfDistance()
	{
		return useDissimilarityInsteadOfDistance_;
	}
	
	public void SetUseDissimilarityInsteadOfDistance(boolean useDissimilarityInsteadOfDistance)
	{
		useDissimilarityInsteadOfDistance_=useDissimilarityInsteadOfDistance;
	}
		
	
	public CompactDataInstanceSet Clone()
	{
		var dataInstanceSet=new CompactDataInstanceSet(disSimilarities_.length);
		for(int i=0;i<dataInstances_.size();i++)
			dataInstanceSet.GetDataInstances().add(dataInstances_.get(i).Clone());
		
		for(int i=0;i<disSimilarities_.length;i++)
			for(int j=0;j<disSimilarities_.length;j++)
				dataInstanceSet.SetDisSimilarity(i, j, disSimilarities_[i][j]);
		
		dataInstanceSet.SetUseDissimilarityInsteadOfDistance(useDissimilarityInsteadOfDistance_);
		
		return dataInstanceSet;
	}
	
	
	
	public void ComputeNeighbors(int numberOfNeighbors)
	{
		for(int i=0;i<dataInstances_.size();i++)
		{
			var dataInstance1=dataInstances_.get(i);
			Comparator<CompactDataInstance> dataInstanceComparator=new Comparator<CompactDataInstance>()
			{
				
				@Override
				public int compare(CompactDataInstance arg0, CompactDataInstance arg1)
				{
					if(dataInstance1.DistanceTo(arg0)<dataInstance1.DistanceTo(arg1))
						return -1;
					else if(dataInstance1.DistanceTo(arg0)>dataInstance1.DistanceTo(arg1))
						return 1;
					else
						return 0;
				}
			};
			
			PriorityQueue<CompactDataInstance> otherDataInstances=new PriorityQueue<CompactDataInstance>(dataInstanceComparator);
			
			for(int j=0;j<dataInstances_.size();j++)
			{
				if(i==j)
					continue;
				var dataInstance2=dataInstances_.get(j);
				otherDataInstances.add(dataInstance2);
			}
			
			dataInstance1.GetNeighbors().clear();
			
			for(int j=0;j<numberOfNeighbors;j++)
				dataInstance1.GetNeighbors().add(otherDataInstances.poll());
		}
	}
	
	public void ComputeNeighborsForEvaluation(int numberOfNeighbors)
	{
		for(int i=0;i<dataInstances_.size();i++)
		{
			var dataInstance1=dataInstances_.get(i);
			Comparator<CompactDataInstance> dataInstanceComparator=new Comparator<CompactDataInstance>()
			{
				
				@Override
				public int compare(CompactDataInstance arg0, CompactDataInstance arg1)
				{
					if(dataInstance1.EvaluationDistanceTo(arg0)<dataInstance1.EvaluationDistanceTo(arg1))
						return -1;
					else if(dataInstance1.EvaluationDistanceTo(arg0)>dataInstance1.EvaluationDistanceTo(arg1))
						return 1;
					else
						return 0;
				}
			};
			
			PriorityQueue<CompactDataInstance> otherDataInstances=new PriorityQueue<CompactDataInstance>(dataInstanceComparator);
			
			for(int j=0;j<dataInstances_.size();j++)
			{
				if(i==j)
					continue;
				var dataInstance2=dataInstances_.get(j);
				otherDataInstances.add(dataInstance2);
			}
			
			dataInstance1.GetNeighborsForEvaluation().clear();
			
			for(int j=0;j<numberOfNeighbors;j++)
				dataInstance1.GetNeighborsForEvaluation().add(otherDataInstances.poll());
		}
	}
	
	public double GetMaximumDistance()
	{
		double maximumDistance=0;
		for(int i=0;i<dataInstances_.size();i++)
		{
			for(int j=i+1;j<dataInstances_.size();j++)
			{
				double distance=dataInstances_.get(i).DistanceTo(dataInstances_.get(j));
				maximumDistance=Math.max(distance, maximumDistance);
			}
		}
		
		return maximumDistance;
	}
	
	public double GetMaximumEvaluationDistance()
	{
		double maximumDistance=0;
		for(int i=0;i<dataInstances_.size();i++)
		{
			for(int j=i+1;j<dataInstances_.size();j++)
			{
				double distance=dataInstances_.get(i).EvaluationDistanceTo(dataInstances_.get(j));
				maximumDistance=Math.max(distance, maximumDistance);
			}
		}
		
		return maximumDistance;
	}
	
	
	public void SetDataInstanceIndices()
	{
		for(int i=0;i<dataInstances_.size();i++)
		{
			dataInstances_.get(i).SetIndexInDataInstanceSet(i);
			dataInstances_.get(i).SetDataInstanceSet(this);
		}
	}
	
	public void SymmetrizeDisSimmilarities()
	{
		for(int i=0;i<dataInstances_.size();i++)
			for(int j=i+1;j<dataInstances_.size();j++)
			{
				double symmetricSimilarity=(disSimilarities_[i][j]+disSimilarities_[j][i])/2.0;
				disSimilarities_[i][j]=symmetricSimilarity;
				disSimilarities_[j][i]=symmetricSimilarity;
			}
	}
	
	public boolean IsDistancesPreComputed()
	{
		return isDistancesPreComputed_;
	}
	
	public void PreComputeDistances()
	{
		isDistancesPreComputed_=false;
		distances_=new double[dataInstances_.size()][dataInstances_.size()];
		for(int i=0;i<dataInstances_.size();i++)
			for(int j=0;j<dataInstances_.size();j++)
			{
				distances_[i][j]=dataInstances_.get(i).DistanceTo(dataInstances_.get(j));
			}
		
		isDistancesPreComputed_=true;
	}
	
	public boolean IsDistancesForEvaluationPreComputed()
	{
		return isDistancesForEvaluationPreComputed_;
	}
	
	public void PreComputeDistancesForEvaluation()
	{
		isDistancesForEvaluationPreComputed_=false;
		distancesForEvaluation_=new double[dataInstances_.size()][dataInstances_.size()];
		for(int i=0;i<dataInstances_.size();i++)
			for(int j=0;j<dataInstances_.size();j++)
			{
				distancesForEvaluation_[i][j]=dataInstances_.get(i).EvaluationDistanceTo(dataInstances_.get(j));
			}
		
		isDistancesForEvaluationPreComputed_=true;
	}
	
	public void TransformPreComputedDistances(String transformation)
	{
		if (transformation.compareTo("Neighbourhood normalized for original space")==0)
		{
			
			double[] m=new double[dataInstances_.size()];
			double temp=Math.tan(1);
			int z=20;
			this.ComputeNeighbors(z);
			for(int i=0;i<dataInstances_.size();i++)
			{
				var dataInstance=dataInstances_.get(i);				
				double diz=dataInstance.GetNeighbors().get(z-1).DistanceTo(dataInstance);
				m[i]=temp/diz;
				dataInstance.GetNeighbors().clear();
			}
			
			for(int i=0;i<dataInstances_.size();i++)
				for(int j=0;j<dataInstances_.size();j++)
					distances_[i][j]=(Math.atan(distances_[i][j]*m[i])+Math.atan(distances_[i][j]*m[j]))/2.0;
		}
		else if(transformation.compareTo("Cosine for original space")==0)
		{
			for(int i=0;i<dataInstances_.size();i++)
				for(int j=0;j<dataInstances_.size();j++)
				{
					var dataInstance1=dataInstances_.get(i);
					var dataInstance2=dataInstances_.get(j);
					
					distances_[i][j]=0;
					double temp1=0;
					double temp2=0;
					for(int k=0;k<dataInstance1.GetFeatures().size();k++)
					{
						distances_[i][j]+=dataInstance1.GetFeature(k)*dataInstance2.GetFeature(k);
						temp1+=dataInstance1.GetFeature(k)*dataInstance1.GetFeature(k);
						temp2+=dataInstance2.GetFeature(k)*dataInstance2.GetFeature(k);
					}
					if(temp1<1e-7)
						temp1=1e-7;
					if(temp2<1e-7)
						temp2=1e-7;
					
					distances_[i][j]/=Math.sqrt(temp1);
					distances_[i][j]/=Math.sqrt(temp2);
					distances_[i][j]=1-distances_[i][j];
				}
					
		}
	}
	
	public void TransformEvaluationPreComputedDistances(String transformation)
	{
		if (transformation.compareTo("Neighbourhood normalized for original space")==0)
		{
			
			double[] m=new double[dataInstances_.size()];
			double temp=Math.tan(1);
			int z=20;
			this.ComputeNeighborsForEvaluation(z);
			for(int i=0;i<dataInstances_.size();i++)
			{
				var dataInstance=dataInstances_.get(i);				
				double diz=dataInstance.GetNeighborsForEvaluation().get(z-1).EvaluationDistanceTo(dataInstance);
				m[i]=temp/diz;
				dataInstance.GetNeighborsForEvaluation().clear();
			}
			
			for(int i=0;i<dataInstances_.size();i++)
				for(int j=0;j<dataInstances_.size();j++)
					distancesForEvaluation_[i][j]=(Math.atan(distancesForEvaluation_[i][j]*m[i])+Math.atan(distancesForEvaluation_[i][j]*m[j]))/2.0;
		}
		else if(transformation.compareTo("Cosine for original space")==0)
		{
			for(int i=0;i<dataInstances_.size();i++)
				for(int j=0;j<dataInstances_.size();j++)
				{
					var dataInstance1=dataInstances_.get(i);
					var dataInstance2=dataInstances_.get(j);
					
					distancesForEvaluation_[i][j]=0;
					double temp1=0;
					double temp2=0;
					var features1=dataInstance1.GetFeatures();
					var features2=dataInstance2.GetFeatures();
					if(dataInstance1.GetFeaturesForEvaluation().size()>0)
					{
						features1=dataInstance1.GetFeaturesForEvaluation();
						features2=dataInstance2.GetFeaturesForEvaluation();
					}
					
					for(int k=0;k<dataInstance1.GetFeatures().size();k++)
					{
						distancesForEvaluation_[i][j]+=features1.get(k)*features2.get(k);
						temp1+=features1.get(k)*features2.get(k);
						temp2+=features2.get(k)*features2.get(k);
					}
					if(temp1<1e-7)
						temp1=1e-7;
					if(temp2<1e-7)
						temp2=1e-7;
					
					distancesForEvaluation_[i][j]/=Math.sqrt(temp1);
					distancesForEvaluation_[i][j]/=Math.sqrt(temp2);
					distancesForEvaluation_[i][j]=1-distancesForEvaluation_[i][j];
				}
					
		}
	}
	
	public double GetPreComputedDistance(int index1,int index2)
	{
		return distances_[index1][index2];
	}
	
	public double GetPreComputedDistanceForEvaluation(int index1,int index2)
	{
		return distancesForEvaluation_[index1][index2];
	}
	
	public void SetIsRedGray(boolean isRedGray)
	{
		isRedGray_=isRedGray;
	}
	
	public boolean GetIsRedGray()
	{
		return isRedGray_;
	}
	
	public void SetIsImageDataSet(boolean isImageDataSet)
	{
		isImageDataInstanceSet_=isImageDataSet;
		
		if(isImageDataSet)
		{
			int imageHeight=(int)(Math.sqrt(dataInstances_.get(0).GetFeaturesForEvaluation().size())+0.1);
			if(imageHeight==0)
				imageHeight=(int)(Math.sqrt(dataInstances_.get(0).GetFeatures().size())+0.1);
			
			for(int i=0;i<dataInstances_.size();i++)
				dataInstances_.get(i).BuildDigitImage(imageHeight);
		}
	}
	
	public boolean GetIsImageDataSet()
	{
		return isImageDataInstanceSet_;
	}
	
	public void SetDissimilarityMatrix(double[][] newDissimilarities)
	{
		disSimilarities_=newDissimilarities;
	}
}

