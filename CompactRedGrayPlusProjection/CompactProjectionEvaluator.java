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

public class CompactProjectionEvaluator
{
	private int neighborhoodSize_;
	private static int staticNeighborhoodSize_=10;
	
	public CompactProjectionEvaluator()
	{
		neighborhoodSize_=staticNeighborhoodSize_;
	}
	
	public double EvaluateProjectionTrustworthinessForStrictRedGrayProjection(CompactProjectedPointSet projectedPointSet, boolean forRedLayer)
	{
		// Look at the following preprint on arXiv for more information about the extended definition of trustworthiness and Strict Red Gray projections:
		// Farshad Barahimi and Fernando Paulovich, “Multi-point dimensionality reduction to improve projection layout reliability.” , arXiv preprint (2021).
		// Look at the following paper for more information about the old fashioned definition of trustworthiness:
		// Jarkko Venna and Samuel Kaski, "Neighborhood Preservation in Nonlinear Projection Methods: An Experimental Study", (2001).
		
		var dataInstanceSet=projectedPointSet.GetDataInstanceSet();
		var dataInstances=dataInstanceSet.GetDataInstances();
		var projectedPoints=projectedPointSet.GetProjectedPoints();
		
		int k=neighborhoodSize_;
		int N=dataInstances.size();
				
		for(int i=0;i<dataInstances.size();i++)
		{
			var dataInstance=dataInstances.get(i);
			dataInstance.GetEvaluationProjectedPoints().clear();
		}
		
		for(int i=0;i<projectedPoints.size();i++)
		{
			var projectedPoint=projectedPoints.get(i);
			projectedPoint.GetDataInstance().GetEvaluationProjectedPoints().add(projectedPoint);
		}
		
		if(forRedLayer)
		{
			for(int i=0;i<dataInstances.size();i++)
			{
				boolean hasRed=false;
				for(int j=0;j<dataInstances.get(i).GetEvaluationProjectedPoints().size();j++)
				{
					if(!(dataInstances.get(i).GetEvaluationProjectedPoints().get(j).IsGray()))
						hasRed=true;
				}
				if(!hasRed)
					N--;
			}
		}
		
		double sum=0;
		
		int[][] visualRanks=new int[dataInstances.size()][dataInstances.size()];
		for(int i=0;i<dataInstances.size();i++)
			for(int j=0;j<dataInstances.size();j++)
				visualRanks[i][j]=-1;
		
		for(int i=0;i<dataInstances.size();i++)
		{
			var dataInstance1=dataInstances.get(i);
			
			for(int t=0;t<dataInstance1.GetEvaluationProjectedPoints().size();t++)
			{
				var projectedPoint1=dataInstance1.GetEvaluationProjectedPoints().get(t);
				
				Comparator<CompactProjectedPoint> projectedPointComparator=new Comparator<CompactProjectedPoint>()
				{
					
					@Override
					public int compare(CompactProjectedPoint arg0, CompactProjectedPoint arg1)
					{
						if(projectedPoint1.GetDistance(arg0)<projectedPoint1.GetDistance(arg1))
							return -1;
						else if(projectedPoint1.GetDistance(arg0)>projectedPoint1.GetDistance(arg1))
							return 1;
						else
							return 0;
					}
				};
				
				var otherProjectedPoints=new PriorityQueue<CompactProjectedPoint>(projectedPointComparator);
				
				for(int j=0;j<projectedPoints.size();j++)
				{
					var projectedPoint2=projectedPoints.get(j);
					
					if(projectedPoint2==projectedPoint1)
						continue;
					
					if(forRedLayer && projectedPoint2.IsGray())
						continue;
					
					otherProjectedPoints.add(projectedPoint2);
				}
				
				int rank=1;
				while(!otherProjectedPoints.isEmpty())
				{
					var projectedPoint2=otherProjectedPoints.poll();
					var index=projectedPoint2.GetDataInstance().GetIndexInDataInstanceSet();
					if(visualRanks[i][index]==-1)
						visualRanks[i][index]=rank;
					else
						visualRanks[i][index]=Math.min(rank,visualRanks[i][index]);
					
					rank++;
				}
			}
		}
		
		for(int i=0;i<dataInstances.size();i++)
		{
			var dataInstance1=dataInstances.get(i);
			for(int j=0;j<dataInstances.size();j++)
			{
				if(visualRanks[i][j]>k)
					continue;
				
				var dataInstance2=dataInstances.get(j);
				
				int minRank=N;
				
				boolean hasProjection=false;
				boolean isVisualNeighbour=false;
				
				for(int t=0;t<dataInstance1.GetEvaluationProjectedPoints().size();t++)
				{
					var projectedPoint1=dataInstance1.GetEvaluationProjectedPoints().get(t);
						
					for(int s=0;s<dataInstance2.GetEvaluationProjectedPoints().size();s++)
					{
						var projectedPoint2=dataInstance2.GetEvaluationProjectedPoints().get(s);
						
						if(!forRedLayer)
							hasProjection=true;
						else if(!(projectedPoint1.IsGray() || projectedPoint2.IsGray()))
							hasProjection=true;
						
						int visualRank=1;
						
						for(int u=0;u<projectedPoints.size();u++)
						{
							var projectedPoint3=projectedPoints.get(u);
							
							if(projectedPoint3==projectedPoint1 || projectedPoint3==projectedPoint2)
								continue;
							
							if(projectedPoint3.GetDistance(projectedPoint1)<projectedPoint2.GetDistance(projectedPoint1))
								if(!forRedLayer || !(projectedPoint3.IsGray()))
									visualRank++;
							
							if(visualRank>k)
								break;
						}
						
						if(visualRank>k)
							continue;
												
						isVisualNeighbour=true;
						
						int rank=1;
						for(int u=0;u<dataInstances.size();u++)
						{
							var dataInstance3=dataInstances.get(u);
							
							if(dataInstance3==dataInstance1 || dataInstance3==dataInstance2)
								continue;
							
							if(dataInstance3.DistanceTo(dataInstance1)<dataInstance2.DistanceTo(dataInstance1))
								if(!forRedLayer || !(dataInstance3.GetProjectedPoints().get(0).IsGray()))
									rank++;
						}
						
						minRank=Math.min(minRank, rank);
					}
				}
				
				if(minRank>k && hasProjection && isVisualNeighbour)
					sum+=minRank-k;
			}
		}
		
		return 1- (2*sum)/(N*((double)k)*(2*N-3*k-1));
	}
	
	
	public static void SetStaticNeighborhoodSize(int staticNeighborhoodSize)
	{
		staticNeighborhoodSize_=staticNeighborhoodSize;
	}
	
	public double EvaluateProjectionTrustworthinessMultiThreadedForStrictRedGrayProjection(CompactProjectedPointSet projectedPointSet, boolean forRedLayer,int numberOfThreads) throws InterruptedException
	{
		// Look at the following preprint on arXiv for more information about the extended definition of trustworthiness and Strict Red Gray projections:
		// Farshad Barahimi and Fernando Paulovich, “Multi-point dimensionality reduction to improve projection layout reliability.” , arXiv preprint (2021).
		// Look at the following paper for more information about the old fashioned definition of trustworthiness:
		// Jarkko Venna and Samuel Kaski, "Neighborhood Preservation in Nonlinear Projection Methods: An Experimental Study", (2001).
		
		var dataInstanceSet=projectedPointSet.GetDataInstanceSet();
		var dataInstances=dataInstanceSet.GetDataInstances();
		var projectedPoints=projectedPointSet.GetProjectedPoints();
		
		int k=neighborhoodSize_;
		int N=dataInstances.size();
				
		for(int i=0;i<dataInstances.size();i++)
		{
			var dataInstance=dataInstances.get(i);
			dataInstance.GetEvaluationProjectedPoints().clear();
		}
		
		for(int i=0;i<projectedPoints.size();i++)
		{
			var projectedPoint=projectedPoints.get(i);
			projectedPoint.GetDataInstance().GetEvaluationProjectedPoints().add(projectedPoint);
		}
		
		if(forRedLayer)
		{
			for(int i=0;i<dataInstances.size();i++)
			{
				boolean hasRed=false;
				for(int j=0;j<dataInstances.get(i).GetEvaluationProjectedPoints().size();j++)
				{
					if(!(dataInstances.get(i).GetEvaluationProjectedPoints().get(j).IsGray()))
						hasRed=true;
				}
				if(!hasRed)
					N--;
			}
		}
		
		int[][] visualRanks=new int[dataInstances.size()][dataInstances.size()];
		
		double sum=0;
		double[] sums=new double[numberOfThreads];
		for(int i=0;i<numberOfThreads;i++)
			sums[i]=0;
		
		var threads=new ArrayList<Thread>();
		for(int threadId=0;threadId<numberOfThreads;threadId++)
		{
			var multiThreadedTrustworthinessComputer= new MultiThreadedTrustworthinessComputer(threadId,numberOfThreads, sums, dataInstances,projectedPoints, 
					k, N, forRedLayer, visualRanks);
			
			var thread=new Thread(multiThreadedTrustworthinessComputer);
			threads.add(thread);
			thread.start();
		}
		
		for(int threadId=0;threadId<numberOfThreads;threadId++)
		{
			threads.get(threadId).join();
		}
		
		for(int threadId=0;threadId<numberOfThreads;threadId++)
		{
			sum+=sums[threadId];
		}
		
		return 1- (2*sum)/(N*((double)k)*(2*N-3*k-1));
	}
	
	private class MultiThreadedTrustworthinessComputer implements Runnable
	{
		int threadId_;
		int numberOfThreads_;
		double[] sums_;
		ArrayList<CompactDataInstance> dataInstances_;
		ArrayList<CompactProjectedPoint> projectedPoints_;
		int k_;
		int N_;
		boolean forRedLayer_;
		int[][] visualRanks_;
		
		public MultiThreadedTrustworthinessComputer(int threadId, int numberOfThreads, double[] sums,
				ArrayList<CompactDataInstance> dataInstances, ArrayList<CompactProjectedPoint> projectedPoints, int k, 
				int N, boolean forRedLayer, int[][] visualRanks )
		{
			threadId_=threadId;
			numberOfThreads_=numberOfThreads;
			sums_=sums;
			dataInstances_=dataInstances;
			projectedPoints_=projectedPoints;
			k_=k;
			N_=N;
			forRedLayer_=forRedLayer;
			visualRanks_=visualRanks;
		}
		
		public void run()
		{
			for(int i=threadId_;i<dataInstances_.size();i+=numberOfThreads_)
				for(int j=0;j<dataInstances_.size();j++)
					visualRanks_[i][j]=-1;
			
			for(int i=threadId_;i<dataInstances_.size();i+=numberOfThreads_)
			{
				var dataInstance1=dataInstances_.get(i);
				
				for(int t=0;t<dataInstance1.GetEvaluationProjectedPoints().size();t++)
				{
					var projectedPoint1=dataInstance1.GetEvaluationProjectedPoints().get(t);
					
					Comparator<CompactProjectedPoint> projectedPointComparator=new Comparator<CompactProjectedPoint>()
					{
						
						@Override
						public int compare(CompactProjectedPoint arg0, CompactProjectedPoint arg1)
						{
							if(projectedPoint1.GetDistance(arg0)<projectedPoint1.GetDistance(arg1))
								return -1;
							else if(projectedPoint1.GetDistance(arg0)>projectedPoint1.GetDistance(arg1))
								return 1;
							else
								return 0;
						}
					};
					
					var otherProjectedPoints=new PriorityQueue<CompactProjectedPoint>(projectedPointComparator);
					
					for(int j=0;j<projectedPoints_.size();j++)
					{
						var projectedPoint2=projectedPoints_.get(j);
						
						if(projectedPoint2==projectedPoint1)
							continue;
						
						if(forRedLayer_ && projectedPoint2.IsGray())
							continue;
						
						otherProjectedPoints.add(projectedPoint2);
					}
					
					int rank=1;
					while(!otherProjectedPoints.isEmpty())
					{
						var projectedPoint2=otherProjectedPoints.poll();
						var index=projectedPoint2.GetDataInstance().GetIndexInDataInstanceSet();
						if(visualRanks_[i][index]==-1)
							visualRanks_[i][index]=rank;
						else
							visualRanks_[i][index]=Math.min(rank,visualRanks_[i][index]);
						
						rank++;
					}
				}
			}
			
			for(int i=threadId_;i<dataInstances_.size();i+=numberOfThreads_)
			{
				var dataInstance1=dataInstances_.get(i);
				for(int j=0;j<dataInstances_.size();j++)
				{
					if(visualRanks_[i][j]>k_)
						continue;
					
					var dataInstance2=dataInstances_.get(j);
					
					int minRank=N_;
					
					boolean hasProjection=false;
					boolean isVisualNeighbour=false;
					
					for(int t=0;t<dataInstance1.GetEvaluationProjectedPoints().size();t++)
					{
						var projectedPoint1=dataInstance1.GetEvaluationProjectedPoints().get(t);
							
						for(int s=0;s<dataInstance2.GetEvaluationProjectedPoints().size();s++)
						{
							var projectedPoint2=dataInstance2.GetEvaluationProjectedPoints().get(s);
							
							if(!forRedLayer_)
								hasProjection=true;
							else if(!(projectedPoint1.IsGray() || projectedPoint2.IsGray()))
								hasProjection=true;
							
							int visualRank=1;
							
							for(int u=0;u<projectedPoints_.size();u++)
							{
								var projectedPoint3=projectedPoints_.get(u);
								
								if(projectedPoint3==projectedPoint1 || projectedPoint3==projectedPoint2)
									continue;
								
								if(projectedPoint3.GetDistance(projectedPoint1)<projectedPoint2.GetDistance(projectedPoint1))
									if(!forRedLayer_ || !(projectedPoint3.IsGray()))
										visualRank++;
								
								if(visualRank>k_)
									break;
							}
							
							if(visualRank>k_)
								continue;
													
							isVisualNeighbour=true;
							
							int rank=1;
							for(int u=0;u<dataInstances_.size();u++)
							{
								var dataInstance3=dataInstances_.get(u);
								
								if(dataInstance3==dataInstance1 || dataInstance3==dataInstance2)
									continue;
								
								if(dataInstance3.DistanceTo(dataInstance1)<dataInstance2.DistanceTo(dataInstance1))
									if(!forRedLayer_ || !(dataInstance3.GetProjectedPoints().get(0).IsGray()))
										rank++;
							}
							
							minRank=Math.min(minRank, rank);
						}
					}
					
					if(minRank>k_ && hasProjection && isVisualNeighbour)
						sums_[threadId_]+=minRank-k_;
				}
			}
		}
	}
}
