package com.statistics;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statistics.vo.LimitVo;

public class NormalDistributionLimit extends CommonLimit {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public NormalDistributionLimit() {
		setLimitCalcLogicTyp("NormalDistribution");
	}
	
	@Override
	public LimitVo getLimit(double[] listDataVal) {
		LimitVo limitVo = new LimitVo();
		Double targetVal;
		Double stdDevVal;
		Double avgVal;
		Double medianVal;
		Double lclVal;
		Double uclVal;
		Double scaleAvgVal;
		Double scaleStdDevVal;
		Double minVal;
		Double maxVal;
		
		try {
			double[] listData = listDataVal;
			
			// Remove Outlier
			if(getBOutlier() == true) {
				listData = removeOutlier(listData);
			}
			
			Median median = new Median();
			avgVal = StatUtils.mean(listData);
			medianVal = median.evaluate(listData);
			stdDevVal = Math.sqrt(StatUtils.variance(listData));
			
			String targetTyp = getTargetTyp();
			Double engTargetVal = getEngTargetVal();
			if(engTargetVal != null) {
				targetVal = engTargetVal;
			}
			else if("MEDIAN".equals(targetTyp)) {
				targetVal = medianVal;
			}
			else {
				targetVal = avgVal;
			}
			
			scaleAvgVal = null;
			scaleStdDevVal = null;
			minVal = StatUtils.min(listData);
			maxVal = StatUtils.max(listData);
			
			// Get Limit
			Double engLclVal = getEngLclVal();
			Double engUclVal = getEngUclVal();
			Double sigmaVal = getSigmaVal();
			
			if(sigmaVal == null) {
				sigmaVal = 3.0;
			}
			
			if(engLclVal != null) {
				lclVal = engLclVal;
			}
			else {
				lclVal = targetVal - (sigmaVal * stdDevVal);
			}
			
			if(engUclVal != null) {
				uclVal = engUclVal;
			}
			else {
				uclVal = targetVal + (sigmaVal * stdDevVal);
			}
			
			logger.info("====================================================================================================");
			logger.info("lcl = {} , ucl = {}", lclVal, uclVal);
			logger.info("====================================================================================================");
			
			limitVo.setTargetVal(targetVal);
			limitVo.setStdDevVal(stdDevVal);
			limitVo.setAvgVal(avgVal);
			limitVo.setMedianVal(medianVal);
			limitVo.setLclVal(lclVal);
			limitVo.setUclVal(uclVal);
			limitVo.setScaleAvgVal(scaleAvgVal);
			limitVo.setScaleStdDevVal(scaleStdDevVal);
			limitVo.setBefMinVal(minVal);
			limitVo.setBefMaxVal(maxVal);
		}
		catch (Exception e) {
			logger.error("NormalDistributionLimit::getLimit Exception");
			logger.error(e.getMessage());
		}
		
		return limitVo;
	}
	
	public Boolean ValidationNormalDistribution(double[] listDataVal) {
		KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();
		NormalDistribution normalDistribution = new NormalDistribution();
		
		return ksTest.kolmogorovSmirnovTest(normalDistribution, listDataVal, 0.05);
	}
}
