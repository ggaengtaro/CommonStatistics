package com.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statistics.vo.LimitVo;

public abstract class CommonLimit {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private double minScaleVal = 0.01;
	private double maxScaleVal = 0.99;
	private double kVal = 3.0;
	private double qVal = 0.0027;
	private Boolean bOutlier = false;
	private String limitCalcLogicTyp;
	private String targetTyp;
	private Double sigmaVal = 3.0;
	private Double engTargetVal = null;
	private Double engLclVal = null;
	private Double engUclVal = null;
	
	public abstract LimitVo getLimit(double[] listDataVal);
	
	public void setMinScaleVal(double minScaleVal) {
		this.minScaleVal = minScaleVal;
	}
	
	public void setMaxScaleVal(double maxScaleVal) {
		this.maxScaleVal = maxScaleVal;
	}
	
	public void setKVal(double kVal) {
		this.kVal = kVal;
	}
	
	public double getQVal() {
		return qVal;
	}
	
	public void setQVal(double qVal) {
		this.qVal = qVal;
	}
	
	public Boolean getBOutlier() {
		return bOutlier;
	}
	
	public void setBOutlier(Boolean bOutlier) {
		this.bOutlier = bOutlier;
	}
	
	public String getLimitCalcLogicTyp() {
		return limitCalcLogicTyp;
	}
	
	public void setLimitCalcLogicTyp(String limitCalcLogicTyp) {
		this.limitCalcLogicTyp = limitCalcLogicTyp;
	}

	public String getTargetTyp() {
		return targetTyp;
	}

	public void setTargetTyp(String targetTyp) {
		this.targetTyp = targetTyp;
	}

	public Double getSigmaVal() {
		return sigmaVal;
	}

	public void setSigmaVal(Double sigmaVal) {
		this.sigmaVal = sigmaVal;
	}

	public Double getEngTargetVal() {
		return engTargetVal;
	}

	public void setEngTargetVal(Double engTargetVal) {
		this.engTargetVal = engTargetVal;
	}

	public Double getEngLclVal() {
		return engLclVal;
	}

	public void setEngLclVal(Double engLclVal) {
		this.engLclVal = engLclVal;
	}

	public Double getEngUclVal() {
		return engUclVal;
	}

	public void setEngUclVal(Double engUclVal) {
		this.engUclVal = engUclVal;
	}
	
	public double[] setDataScaling(double[] listDataVal) throws Exception {
		double maxVal = StatUtils.max(listDataVal);
		double minVal = StatUtils.min(listDataVal);
		
		double[] listResultVal = new double[listDataVal.length];
		
		if(minVal == maxVal) {
			logger.info("====================================================================================================");
			logger.info("Data Scaling fail. (min = max = {})", minVal);
			logger.info("====================================================================================================");
			
			return null;
		}
		else {
			for(int i=0; i<listDataVal.length; ++i) {
				double data = this.minScaleVal + (this.maxScaleVal - this.minScaleVal) * (listDataVal[i] - minVal) / (maxVal - minVal);
				listResultVal[i] = data;
			}
		}
		
		return listResultVal;
	}
	
	public double setDataDescaling(double[] listDataVal, double dataVal) throws Exception {
		double maxVal = StatUtils.max(listDataVal);
		double minVal = StatUtils.min(listDataVal);
		double resultVal = 0.0;
		
		if(minVal == maxVal) {
			logger.info("====================================================================================================");
			logger.info("Data Scaling fail. (min = max = {})", minVal);
			logger.info("====================================================================================================");
		}
		else {
			resultVal = (dataVal = this.minScaleVal) * (maxVal - minVal) / (this.maxScaleVal - this.minScaleVal) + minVal;
		}
		
		return resultVal;
	}
	
	public double[] removeOutlier(double[] listDataVal) throws Exception {
		int sizeVal = 0;
		double q1Val = 0.0;
		double q3Val = 0.0;
		double iqrVal, uolVal, lolVal;
		
		if(listDataVal != null) {
			sizeVal = listDataVal.length;
		}
		
		Arrays.sort(listDataVal);
		
		logger.info("====================================================================================================");
		logger.info("Sorting data: {}", Arrays.toString(listDataVal));
		logger.info("Count: {}", listDataVal.length);
		logger.info("====================================================================================================");
		
		q1Val = getQuartile(listDataVal, 0.25);
		q3Val = getQuartile(listDataVal, 0.75);
		
		iqrVal = q3Val - q1Val;
		uolVal = q3Val + (this.kVal * iqrVal);
		lolVal = q1Val - (this.kVal * iqrVal);
		
		logger.info("====================================================================================================");
		logger.info("K = {}", this.kVal);
		logger.info("Q1 = {} , Q3 = {}", q1Val, q3Val);
		logger.info("IQR = {}", iqrVal);
		logger.info("LOL = {} , UOL = {}", lolVal, uolVal);
		logger.info("====================================================================================================");
		
		List<Double> list = new ArrayList<Double>();
		for(int i=sizeVal-1; 0<=i; --i) {
			if(lolVal <= listDataVal[i] && listDataVal[i] <= uolVal) {
				list.add(listDataVal[i]);
			}
		}
		
		double[] listResultVal = new double[list.size()];
		for(int i=0; i<list.size(); ++i) {
			listResultVal[i] = list.get(i);
		}
		
		logger.info("====================================================================================================");
		logger.info("After removeOutlier Cnt : {}", list.size());
		logger.info("====================================================================================================");
		
		return listResultVal;
	}
	
	public double getQuartile(double[] listDataVal, double qVal) throws Exception {
		// (도수-1) * q = j + g;
		// y = (1-g) * x *(j+1) + g * x * (j + 2)
		
		int j = (int) ((double) (listDataVal.length - 1) * qVal);
		double p = ((double) (listDataVal.length - 1) * qVal) - j;
		
		return ((1 - p) * listDataVal[j]) + (p * (listDataVal[j+1]));
	}
}
