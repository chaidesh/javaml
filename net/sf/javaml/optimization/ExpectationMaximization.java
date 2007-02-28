/**
 * ExpectationMaximization.java, 22-feb-2007
 *
 * This file is part of the Java Machine Learning API
 * 
 * The Java Machine Learning API is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * The Java Machine Learning API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Java Machine Learning API; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Copyright (c) 2007, Andreas De Rijcke
 * 
 * Project: http://sourceforge.net/projects/java-ml/
 * 
 */

package net.sf.javaml.optimization;

import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.optimization.GammaFunction;
import java.lang.Math;
import java.util.Vector;

public class ExpectationMaximization {

	private int maxIter = 200;

	// convergence criterium
	private double cdif = 0.001;

	// dimension - 2
	private double dimD;

	// Pc
	private double pc;

	// optimized Pc
	private double pcOp;

	// pb
	private double pb;

	// optimized Pb
	private double pbOp;

	private double sD;

	private double sD1;

	private double sm;

	private double variance;
	// optimized variance
	private double varianceOp;

	// p(r|C)
	private Vector<Double> prc = new Vector<Double>() ;

	// p(r|B)
	private Vector<Double> prb = new Vector<Double>() ;

	// p(r|C)*Pc
	private Vector<Double> prcpc = new Vector<Double>() ;

	// p(r|B)*Pb
	private Vector<Double> prbpb = new Vector<Double>() ;

	// p(r)
	private Vector<Double> pr = new Vector<Double>() ;

	// p(C|r)
	private Vector<Double> pcr = new Vector<Double>() ;

	// all distances between cluster centroid and Instance < rk_prelim
	private Vector<Double> clusterDist = new Vector<Double>() ;

	private DistanceMeasure dm = new EuclideanDistance();

	private GammaFunction gammaF = new GammaFunction();

	// calculates first variance esteimate for a number of instances
	public double var(Vector<Instance> cluster, Vector<Double> clusterDist,
			double dimD) {
		double var;
		int instanceLenght = cluster.get(0).size();
		double sum = 0;
		for (int i = 0; i < cluster.size(); i++) {
			for (int j = 0; j < instanceLenght; j++) {
				sum += cluster.get(i).getValue(j) * cluster.get(i).getValue(j);
			}
		}
		return var = (1 / dimD) * sum / clusterDist.size();
	}

	// calculates optimized variance
	public double varOp(Vector<Instance> cluster, Vector<Double> pcr,
			double dimD, double sm) {
		double varOp;
		int instanceLenght = cluster.get(0).size();
		double sum = 0;
		System.out.println("EM : varOP : clustersize "+cluster.size());
		System.out.println("EM : varOP : pcr size "+pcr.size());
		for (int i = 0; i < cluster.size(); i++) {
			for (int j = 0; j < instanceLenght; j++) {
				sum += (cluster.get(i).getValue(j) * cluster.get(i).getValue(j))
						* pcr.get(i);
			}
		}
		return varOp = (1 / dimD) * sum / sm;
	}

	// calculates p(r|C)
	public Vector<Double> prc(double var, Vector<Double> clusterDist,
			double sD, double dimD) {
		Vector<Double> prc = new Vector<Double>();
		double sum[] = new double[clusterDist.size()];
		for (int i = 0; i < clusterDist.size(); i++) {
			sum[i] += (sD / Math.pow(2 * Math.PI * var * var, dimD / 2))
					* Math.pow(clusterDist.get(i), dimD - 1)
					* Math.exp(-(clusterDist.get(i) * clusterDist.get(i))
							/ (2 * var * var));
			prc.add(sum[i]);
		}
		return prc;
	}

	// calculates p(r|B)
	public Vector<Double> prb(double var, Vector<Double> clusterDist,
			double sD, double sD1, double dimD) {
		Vector<Double> prb = new Vector<Double>();
		double sum[] = new double[clusterDist.size()];
		for (int i = 0; i < clusterDist.size(); i++) {
			sum[i] += (sD / (sD1 * Math.pow(dimD + 1, dimD / 2)))
					* Math.pow(var, dimD - 1);
			prb.add(sum[i]);
		}
		return prb;
	}

	// calculates p(r|X) * Px
	public Vector<Double> prxpx(Vector<Double> prx, double px) {
		Vector<Double> prxpx = new Vector<Double>();
		
		for (int i = 0; i < prx.size(); i++) {
			double temp[] = new double[prx.size()];
			temp[i] = prx.get(i) * px;
			prxpx.add(temp[i]);
		}
		return prxpx;
	}

	// calculates p(r)
	public Vector<Double> pr(Vector<Double> prcpc, Vector<Double> prbpb) {
		if (prcpc.size() != prbpb.size()) {
			throw new RuntimeException(
					"Both vectors should contain the same number of values.");
		}
		Vector<Double> pr = new Vector<Double>();
		double sum[] = new double[prcpc.size()];
		for (int i = 0; i < prcpc.size(); i++) {
			sum[i] = prcpc.get(i) + prbpb.get(i);
			pr.add(sum[i]);
		}
		return pr;
	}

	// calculates P(C|r)
	public Vector<Double> pcr(Vector<Double> prcpc, Vector<Double> pr) {
		if (prcpc.size() != pr.size()) {
			throw new RuntimeException(
					"Both vectors should contain the same number of values.");
		}
		Vector<Double> pcr = new Vector<Double>();
		double temp[] = new double[prcpc.size()];
		for (int i = 0; i < prcpc.size(); i++) {
			temp[i] = prcpc.get(i) / pr.get(i);
			pcr.add(temp[i]);
		}
		return pcr;
	}

	// calculates sD
	public double sD(double dimD) {
		double sD = Math.pow(2 * Math.PI, dimD / 2) / gammaF.gamma(dimD / 2);
		return sD;
	}

	// calculates sm
	public double sm(Vector<Double> pcr) {
		double sm = 0;
		for (int i = 0; i < pcr.size(); i++) {
			sm += pcr.get(i);
		}
		return sm;
	}

	// main algorithm
	public double em(Vector<Instance> data, Vector<Instance> cluster, Instance ck, double rk_prelim,
			double dimension, Vector<Double> varianceEst) {
		dimD = dimension - 2;
		System.out.println("EM : dataSize " + data.size());
		System.out.println("EM : clusterSize " + cluster.size());
		System.out.println("EM : start main");
		// for each instances in cluster: calculate distance to ck
		for (int i = 0; i < cluster.size(); i++) {
			double distance = dm.calculateDistance(cluster.get(i), ck);
			System.out.println("EM : distance "+distance);
				clusterDist.add(distance);
		}
		
		System.out.println("EM : calculate first estimates");
		// calculate first estimate for pc, pb variance
		double clusterSize = cluster.size();
		pc = clusterSize / data.size();
		System.out.println("EM : estimate pc "+pc);
		pb = 1 - pc;
		System.out.println("EM : estimate pb "+pb);
		variance = var(cluster, clusterDist, dimD);
		System.out.println("EM : estimate variance "+variance);
		System.out.println("EM : start calculating optimized estimates");
		sD = sD(dimD);
		System.out.println("EM : sD "+sD);
		sD1 = sD(dimD + 1);
		System.out.println("EM : sD1 "+sD1);
		for (int i = 0; i < maxIter; i++) {
			prc = prc(variance, clusterDist, sD, dimD);
			System.out.println("EM : prc "+prc);
			prb = prb(variance, clusterDist, sD, sD1, dimD);
			System.out.println("EM : prb "+prb);
			prcpc = prxpx(prc, pc);
			System.out.println("EM : prcpc "+prcpc);
			prbpb = prxpx(prb, pb);
			System.out.println("EM : prbpb "+ prbpb);
			pr = pr(prcpc, prbpb);
			System.out.println("EM : pr "+pr);
			pcr = pcr(prcpc, pr);
			System.out.println("EM : pcr "+pcr);
			sm = sm(pcr);
			System.out.println("EM : sm " +sm);
			if ( sm == 0 || sm == Double.POSITIVE_INFINITY || sm == Double.NEGATIVE_INFINITY){
				System.out.println("SM value not valid.");
				varianceEst=null;
				return 0;
			}
			varianceOp = varOp(cluster, pcr, dimD, sm);
			System.out.println("EM : varianceOp " +varianceOp);
			pcOp = sm / cluster.size();
			System.out.println("EM : pcOp " +pcOp);
			pbOp = 1 - pcOp;
			System.out.println("EM : pbOp " +pbOp);
			if ( Math.abs(varianceOp - variance) < cdif & Math.abs(pcOp-pc)< cdif){
				System.out.println("No or incorrect convergence.");
				varianceEst=null;
				return 0;
			}
			pc = pcOp;
			pb = pbOp;
			variance = varianceOp;
			System.out.println("EM : next iteration");
		}
		System.out.println("EM : end calculation estimates");
		varianceEst.add(variance);
		System.out.println("EM : end EM");
		return pc;
	}
}
