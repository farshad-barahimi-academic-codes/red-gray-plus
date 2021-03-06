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
import java.util.HashMap;
import java.util.Random;

public class RedGrayPlusProjectionMethodMultiThreaded extends CompactProjectionMethod
{
	/**
	 * The algorithm consists of three components:
	 * + Computing a k nearest neighborhood graph.
	 * + Applying a force directed graph layout algorithm which is based on but significantly modified from the force directed graph layout method of FRUCHTERMAN and REINGOLD (1991).
	 * + Choosing some points to be replicated gradually during iterations and rebuilding the neighborhood graph based on replicated points
	 * Look at the following preprint on arXiv for more information:
	 * Farshad Barahimi and Fernando Paulovich, “Multi-point dimensionality reduction to improve projection layout reliability.” , arXiv preprint (2021).
	 */
	@Override
	public CompactProjectionOutput Project(CompactDataInstanceSet dataInstanceSet, HashMap<String, String> parameters) throws Exception
	{
		var projectionSteps=new ArrayList<CompactProjectionStep>();
		double width=1000;
		double height=1000;
		
		var dataInstances=dataInstanceSet.GetDataInstances();
		int numberOfPoints=dataInstances.size();
		int defaultNumberOfNeighbors=numberOfPoints/3;
		
		int numberOfNeighbors=defaultNumberOfNeighbors;
		if(parameters.getOrDefault("NumberOfNeighboursForBuildingGraph","").compareTo("one-third")==0)
			numberOfNeighbors=numberOfPoints/3;
		else if(parameters.getOrDefault("NumberOfNeighboursForBuildingGraph","").compareTo("one-forth")==0)
			numberOfNeighbors=numberOfPoints/4;
		else if(parameters.getOrDefault("NumberOfNeighboursForBuildingGraph","").compareTo("one-fifth")==0)
			numberOfNeighbors=numberOfPoints/5;
		else
			numberOfNeighbors=Integer.parseInt(parameters.getOrDefault("NumberOfNeighboursForBuildingGraph", defaultNumberOfNeighbors+""));
		
		int numberOfThreads = Runtime.getRuntime().availableProcessors()-1;
		if(parameters.containsKey("NumberOfThreads"))
			numberOfThreads = Integer.parseInt(parameters.get("NumberOfThreads"));
		
		System.out.println("Using "+numberOfThreads+" threads.");
		
		boolean displayNeighborhoodGraph=(parameters.getOrDefault("DisplayNeighborhoodGraph", "false").compareTo("true")==0);
		
		dataInstanceSet.ComputeNeighbors(numberOfNeighbors);
		
		var random = new Random();
		random.setSeed(76213290821348841l);
		
		for(int i=0;i<1;i++)
		{	
			for(int j=0;j<dataInstances.size();j++)
			{
				var dataInstance=dataInstances.get(j);
				var projectedPoints=dataInstance.GetProjectedPoints();
				var projectedPoint=new CompactProjectedPoint(random.nextDouble()*width,random.nextDouble()*height, dataInstance);
				if(i==0)
					projectedPoints.clear();
				projectedPoints.add(projectedPoint);
				projectedPoint.SetProjectionIndex(i);
			}
		}
		
		for(int i=0;i<dataInstances.size();i++)
		{
			var dataInstance1=dataInstances.get(i);
			for(int j=0;j<dataInstance1.GetNeighbors().size();j++)
			{
				var dataInstance2=dataInstance1.GetNeighbors().get(j);
				dataInstance1.GetProjectedPoints().get(0).GetNeighbors().add(dataInstance2.GetProjectedPoints().get(0));
			}
		}
		
		var initialProjectedPointSet=new CompactProjectedPointSet(dataInstanceSet);
		
		projectionSteps.add(new CompactProjectionStep("Initial random", initialProjectedPointSet));
		
		int numberOfSteps=1000;
		
		double initialTemperature=100;
		double temperature=initialTemperature;
		double idealDistance=Math.sqrt((width*height)/numberOfPoints);
		double idealDistanceSquared=Math.pow(idealDistance, 2);
		double epsilon=1e-9;
		
		double maximumOriginalDistance=dataInstanceSet.GetMaximumDistance();
		
		double visualDensityVariationParameter=Double.parseDouble(parameters.getOrDefault("VisualDensityAdjustmentParameter", "0.9"));
		double originalDataImpactFactor=0.5;
		
		double maximumVisualDistance=initialProjectedPointSet.GetMaximumDistance();
		
		int numberOfReplications=1;
		int replicationInterval=1;
		boolean replicationStarted=false;
		
		double[] angleCosines=new double[36];
		double[] angleSines=new double[36];
		
		for(int angleIndex=0;angleIndex<36;angleIndex++)
		{
			double angle=(Math.PI/18.0)*angleIndex;
			angleCosines[angleIndex]=Math.cos(angle);
			angleSines[angleIndex]=Math.sin(angle);
		}
		
		double frozenFrameX=0;
		double frozenFrameY=0;
		double frozenFrameX1=0;
		double frozenFrameY1=0;
		
		boolean isThirdPhaseStarted=false;
		boolean isForthPhaseStarted=false;
		
		int totalStep=-1;
		
		for(int step=0;step<numberOfSteps;step++)
		{
			totalStep++;
			if((totalStep+1)%100==1)
				System.out.println(String.format("Starting iteration %04d of 1830 | timestamp:", totalStep+1) + System.currentTimeMillis());
			
			
			// Repulsive forces
			var threads=new ArrayList<Thread>();
			for(int threadId=0;threadId<numberOfThreads;threadId++)
			{
				var repulsiveForcesCalculator= new RepulsiveForcesCalculator(threadId, 
						epsilon, idealDistanceSquared,
						numberOfReplications, replicationStarted, step, replicationInterval,
						angleCosines, angleSines, dataInstanceSet.GetDataInstances(),numberOfThreads);
				
				var thread=new Thread(repulsiveForcesCalculator);
				threads.add(thread);
				thread.start();
			}
			
			for(int threadId=0;threadId<numberOfThreads;threadId++)
			{
				threads.get(threadId).join();
			}
			
			// Attractive forces
			threads=new ArrayList<Thread>();
			for(int threadId=0;threadId<numberOfThreads;threadId++)
			{
				var attractiveForcesCalculator= new AttractiveForcesCalculator(threadId, 
						epsilon, idealDistance,
						numberOfReplications, replicationStarted, step, replicationInterval,
						angleCosines, angleSines, maximumOriginalDistance,
						maximumVisualDistance, visualDensityVariationParameter,
						originalDataImpactFactor,dataInstanceSet.GetDataInstances(),numberOfThreads, true);
				
				var thread=new Thread(attractiveForcesCalculator);
				threads.add(thread);
				thread.start();
			}
			
			for(int threadId=0;threadId<numberOfThreads;threadId++)
			{
				threads.get(threadId).join();
			}
			
			threads=new ArrayList<Thread>();
			for(int threadId=0;threadId<numberOfThreads;threadId++)
			{
				var attractiveForcesCalculator= new AttractiveForcesCalculator(threadId, 
						epsilon, idealDistance,
						numberOfReplications, replicationStarted, step, replicationInterval,
						angleCosines, angleSines, maximumOriginalDistance,
						maximumVisualDistance, visualDensityVariationParameter,
						originalDataImpactFactor,dataInstanceSet.GetDataInstances(),numberOfThreads, false);
				
				var thread=new Thread(attractiveForcesCalculator);
				threads.add(thread);
				thread.start();
			}
			
			for(int threadId=0;threadId<numberOfThreads;threadId++)
			{
				threads.get(threadId).join();
			}
			
			CompactProjectedPoint selectedProjectedPoint=null;
			int selectedAngleIndex=0;
			
			
			for(int i=0;i<dataInstances.size();i++)
			{
				var dataInstance=dataInstances.get(i);
				for(int k=0;k<dataInstance.GetProjectedPoints().size();k++)
				{
					var projectedPoint=dataInstance.GetProjectedPoints().get(k);
					if(numberOfReplications>0 && replicationStarted && step%replicationInterval==0)
					{
						for(int angleIndex=0;angleIndex<36;angleIndex++)
						{
							if(projectedPoint.HasReplicationFailed())
								continue;
							
							if(projectedPoint.IsGray())
								continue;
							
							if(selectedProjectedPoint==null)
							{
								selectedProjectedPoint=projectedPoint;
								selectedAngleIndex=angleIndex;
							}
							else if(projectedPoint.ReplicationPressure(angleIndex)>selectedProjectedPoint.ReplicationPressure(selectedAngleIndex))
							{
								selectedProjectedPoint=projectedPoint;
								selectedAngleIndex=angleIndex;
							}		
						}
					}
					
					double additionalSize=Math.sqrt(Math.pow(projectedPoint.GetAdditionalX(),2)+Math.pow(projectedPoint.GetAdditionalY(), 2));
					
					if(additionalSize>epsilon && !projectedPoint.IsFrozen())
					{
						projectedPoint.SetX(projectedPoint.GetX()+ (projectedPoint.GetAdditionalX()/additionalSize)*Math.min(additionalSize,temperature));
						projectedPoint.SetY(projectedPoint.GetY()+ (projectedPoint.GetAdditionalY()/additionalSize)*Math.min(additionalSize,temperature));
						
						if(replicationStarted)
						{
							projectedPoint.SetX(Math.max(frozenFrameX,Math.min(projectedPoint.GetX(), frozenFrameX1)));
							projectedPoint.SetY(Math.max(frozenFrameY,Math.min(projectedPoint.GetY(), frozenFrameY1)));
						}
					}
				}
			}
						
			if(step==501)
			{
				var projectedPointSet=new CompactProjectedPointSet(dataInstanceSet,false,false);
				int outlierCount=projectedPointSet.GetPressureOutlierCount();
				if(parameters.containsKey("OverrideMaxNumberOfReplicates"))
					outlierCount= Integer.parseInt(parameters.get("OverrideMaxNumberOfReplicates"));
				numberOfReplications=outlierCount;
			}
			else if(numberOfReplications>0 && replicationStarted && step%replicationInterval==0)
			{
				double selectedAngle=(Math.PI/18.0)*selectedAngleIndex;
				//selectedProjectedPoint.ReplicateBasedOnAngle(selectedAngle,dataInstanceSet);
				if(selectedProjectedPoint!=null)
				{
					selectedProjectedPoint.SetIsInEffective(true);
					numberOfReplications--;
				}
			}
			
			if(step==500)
			{
				replicationStarted=true;
								
				var containingBox=new CompactProjectedPointSet(dataInstanceSet,false,false).GetContainingBox();
				frozenFrameX=containingBox.GetCorner1().get(0);
				frozenFrameY=containingBox.GetCorner1().get(1);
				frozenFrameX1=containingBox.GetCorner2().get(0);
				frozenFrameY1=containingBox.GetCorner2().get(1);
				
				double increaseX=(frozenFrameX1-frozenFrameX)*0.05;
				double increaseY=(frozenFrameY1-frozenFrameY)*0.05;
				
				frozenFrameX=frozenFrameX-increaseX;
				frozenFrameX1=frozenFrameX1+increaseX;
				frozenFrameY=frozenFrameY-increaseX;
				frozenFrameY1=frozenFrameY1+increaseY;
			}
			
			if(step==950 && !isThirdPhaseStarted)
			{
				isThirdPhaseStarted=true;
				step=510;
				for(int i=0;i<dataInstances.size();i++)
				{
					var dataInstance=dataInstances.get(i);
					int numberOfProjections=dataInstance.GetProjectedPoints().size();
					for(int k=0;k<numberOfProjections;k++)
					{
						var projectedPoint=dataInstance.GetProjectedPoints().get(k);
						if(projectedPoint.IsInEffective())
						{
							projectedPoint.SetIsInEffective(false);
						}
						
						else
							projectedPoint.SetIsFrozen(true);
					}
				}
			}
			
			if(step==900  && isThirdPhaseStarted && !isForthPhaseStarted)
			{
				isForthPhaseStarted=true;
				step=510;
				for(int i=0;i<dataInstances.size();i++)
				{
					var dataInstance=dataInstances.get(i);
					int numberOfProjections=dataInstance.GetProjectedPoints().size();
					for(int k=0;k<numberOfProjections;k++)
					{
						var projectedPoint=dataInstance.GetProjectedPoints().get(k);
						if(projectedPoint.IsGray())
						{
							projectedPoint.ReplicateBasedOnAngles();;
						}
					}
				}
			}
			
			temperature=initialTemperature-((step+1.0)/numberOfSteps)*initialTemperature;
			if(displayNeighborhoodGraph)
				projectionSteps.add(new CompactProjectionStep(""+(totalStep+1), new CompactProjectedPointSet(dataInstanceSet,true,true),numberOfThreads));
			else
				projectionSteps.add(new CompactProjectionStep(""+(totalStep+1), new CompactProjectedPointSet(dataInstanceSet,true,false),numberOfThreads));
		}
			
		
		return new CompactProjectionOutput("Red Gray Plus projection", projectionSteps);
	}
	
	private class RepulsiveForcesCalculator implements Runnable
	{
		private int threadId_;
		private double epsilon_;
		private double idealDistanceSquared_;
		private int numberOfReplications_;
		private boolean replicationStarted_;
		private int step_;
		private double replicationInterval_;
		private double[] angleCosines_;
		private double[] angleSines_;
		private ArrayList<CompactDataInstance> dataInstances_;
		private int numberOfThreads_;
		
		public RepulsiveForcesCalculator(int threadId,
				double epsilon,
				double idealDistanceSquared,
				int numberOfReplications,
				boolean replicationStarted,
				int step,
				double replicationInterval,
				double[] angleCosines,
				double[] angleSines,
				ArrayList<CompactDataInstance> dataInstances,
				int numberOfThreads)
		{
			threadId_=threadId;
			epsilon_=epsilon;
			idealDistanceSquared_=idealDistanceSquared;
			numberOfReplications_=numberOfReplications;
			replicationStarted_=replicationStarted;
			step_=step;
			replicationInterval_=replicationInterval;
			angleCosines_=angleCosines;
			angleSines_=angleSines;
			dataInstances_=dataInstances;
			numberOfThreads_=numberOfThreads;
		}
		
		public void run()
		{
			for(int i=threadId_;i<dataInstances_.size();i+=numberOfThreads_)
			{
				var dataInstance1=dataInstances_.get(i);
				for(int k=0;k<dataInstance1.GetProjectedPoints().size();k++)
				{
					var projectedPoint1=dataInstance1.GetProjectedPoints().get(k);
					projectedPoint1.SetAdditionalX(0);
					projectedPoint1.SetAdditionalY(0);
					projectedPoint1.ResetPressures();
					for(int j=0;j<dataInstances_.size();j++)
					{
						var dataInstance2=dataInstances_.get(j);
						
						for(int t=0;t<dataInstance2.GetProjectedPoints().size();t++)
						{
							if(i==j && t==k)
								continue;
							
							var projectedPoint2=dataInstance2.GetProjectedPoints().get(t);
							
							if(projectedPoint1.IsInEffective() || projectedPoint2.IsInEffective())
								continue;
							
							double deltaX=projectedPoint1.GetX()-projectedPoint2.GetX();
							double deltaY=projectedPoint1.GetY()-projectedPoint2.GetY();
							double deltaSize=Math.sqrt(Math.pow(deltaX, 2)+Math.pow(deltaY, 2));
							
							if(deltaSize<epsilon_)
								deltaSize=epsilon_;
							
							double repulsiveForce=idealDistanceSquared_/deltaSize;
							
							double vec2X=(deltaX/deltaSize)*repulsiveForce;
							double vec2Y=(deltaY/deltaSize)*repulsiveForce;
							
							if(numberOfReplications_>0 && replicationStarted_ && step_%replicationInterval_==0)
							{
								for(int angleIndex=0;angleIndex<36;angleIndex++)
								{
									double vec1X=angleCosines_[angleIndex];
									double vec1Y=angleSines_[angleIndex];
									
									double pressure1=(vec1X*vec2X+vec1Y*vec2Y);
									projectedPoint1.AddToPressures(pressure1, angleIndex);
								}
							}
							
							projectedPoint1.SetAdditionalX(projectedPoint1.GetAdditionalX()+(deltaX/deltaSize)*repulsiveForce);
							projectedPoint1.SetAdditionalY(projectedPoint1.GetAdditionalY()+(deltaY/deltaSize)*repulsiveForce);
						}
					}
				}
			}
		}
	}
	
	private class AttractiveForcesCalculator implements Runnable
	{
		private int threadId_;
		private double epsilon_;
		private double idealDistance_;
		private int numberOfReplications_;
		private boolean replicationStarted_;
		private int step_;
		private double replicationInterval_;
		private double[] angleCosines_;
		private double[] angleSines_;
		double maximumOriginalDistance_;
		double maximumVisualDistance_;
		double visualDensityVariationParameter_;
		double originalDataImpactFactor_;
		ArrayList<CompactDataInstance> dataInstances_;
		int numberOfThreads_;
		boolean computeFirst_;
		
		public AttractiveForcesCalculator(int threadId,
				double epsilon,
				double idealDistance,
				int numberOfReplications,
				boolean replicationStarted,
				int step,
				double replicationInterval,
				double[] angleCosines,
				double[] angleSines,
				double maximumOriginalDistance,
				double maximumVisualDistance,
				double visualDensityVariationParameter,
				double originalDataImpactFactor,
				ArrayList<CompactDataInstance> dataInstances,
				int numberOfThreads,
				boolean computeFirst)
		{
			threadId_=threadId;
			epsilon_=epsilon;
			idealDistance_=idealDistance;
			numberOfReplications_=numberOfReplications;
			replicationStarted_=replicationStarted;
			step_=step;
			replicationInterval_=replicationInterval;
			angleCosines_=angleCosines;
			angleSines_=angleSines;
			maximumOriginalDistance_=maximumOriginalDistance;
			maximumVisualDistance_=maximumVisualDistance;
			visualDensityVariationParameter_=visualDensityVariationParameter;
			originalDataImpactFactor_=originalDataImpactFactor;
			dataInstances_=dataInstances;
			numberOfThreads_=numberOfThreads;
			computeFirst_=computeFirst;
		}
		
		public void run()
		{
			int integer1=threadId_;
			int integer2=numberOfThreads_;
			if(!computeFirst_)
			{
				integer1=0;
				integer2=1;
			}
			
			for(int i=integer1;i<dataInstances_.size();i+=integer2)
			{
				var dataInstance1=dataInstances_.get(i);
				for(int k=0;k<dataInstance1.GetProjectedPoints().size();k++)
				{
					var projectedPoint1=dataInstance1.GetProjectedPoints().get(k);
					var neighbors=projectedPoint1.GetNeighbors();
					for(int j=0;j<neighbors.size();j++)
					{
						var projectedPoint2=neighbors.get(j);
						
						if(projectedPoint1.IsInEffective() || projectedPoint2.IsInEffective())
							continue;
						
						if(computeFirst_ || projectedPoint2.GetDataInstance().GetIndexInDataInstanceSet()%numberOfThreads_==threadId_)
						{
							double deltaX=projectedPoint1.GetX()-projectedPoint2.GetX();
							double deltaY=projectedPoint1.GetY()-projectedPoint2.GetY();
							double deltaSize=Math.sqrt(Math.pow(deltaX, 2)+Math.pow(deltaY, 2));
							
							if(deltaSize<epsilon_)
								deltaSize=epsilon_;
							
							double attractiveForce=Math.pow(deltaSize/idealDistance_, 1-visualDensityVariationParameter_);
							
							double originalDistance=projectedPoint1.GetDataInstance().DistanceTo(projectedPoint2.GetDataInstance());
							originalDistance/=maximumOriginalDistance_;
							
							double attractiveForce2=originalDistance-deltaSize/maximumVisualDistance_;
							if(attractiveForce2>0)
								attractiveForce2=Math.min(attractiveForce2, Math.abs(attractiveForce)*originalDataImpactFactor_);
							else
								attractiveForce2=Math.max(attractiveForce2, Math.abs(attractiveForce)*(-originalDataImpactFactor_));
							
							attractiveForce+=attractiveForce2;
							
							double vec2X=-(deltaX/deltaSize)*attractiveForce;
							double vec2Y=-(deltaY/deltaSize)*attractiveForce;
							
							double vec3X=(deltaX/deltaSize)*attractiveForce;
							double vec3Y=(deltaY/deltaSize)*attractiveForce;
							
							if(numberOfReplications_>0 && replicationStarted_ && step_%replicationInterval_==0)
							{
								for(int angleIndex=0;angleIndex<36;angleIndex++)
								{
									double vec1X=angleCosines_[angleIndex];
									double vec1Y=angleSines_[angleIndex];
									
									if(computeFirst_)
									{
										double pressure1=(vec1X*vec2X+vec1Y*vec2Y);
										projectedPoint1.AddToPressures(pressure1, angleIndex);
									}
									else
									{
										double pressure2=(vec1X*vec3X+vec1Y*vec3Y);
										projectedPoint2.AddToPressures(pressure2, angleIndex);
									}
									
								}
							}
							
							if(computeFirst_)
							{
								projectedPoint1.SetAdditionalX(projectedPoint1.GetAdditionalX()-(deltaX/deltaSize)*attractiveForce*projectedPoint1.GetEffectiveWeight());
								projectedPoint1.SetAdditionalY(projectedPoint1.GetAdditionalY()-(deltaY/deltaSize)*attractiveForce*projectedPoint1.GetEffectiveWeight());
							}
							else
							{
								projectedPoint2.SetAdditionalX(projectedPoint2.GetAdditionalX()+(deltaX/deltaSize)*attractiveForce*projectedPoint2.GetEffectiveWeight());
								projectedPoint2.SetAdditionalY(projectedPoint2.GetAdditionalY()+(deltaY/deltaSize)*attractiveForce*projectedPoint2.GetEffectiveWeight());
							}
						}
					}
				}
			}
		}
	}
}
		
	