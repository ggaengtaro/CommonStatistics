package com.statistics;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statistics.vo.LimitVo;

public class NonParametricLimit extends CommonLimit {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public NonParametricLimit() {
		setLimitCalcLogicTyp("NonParametric");
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
			
			// Data Scaling
			double[] listScaledDataVal = setDataScaling(listData);
			if(listScaledDataVal == null) {
				return null;
			}
			
			Median median = new Median();
			avgVal = StatUtils.mean(listData);
			medianVal = median.evaluate(listData);
			
			scaleAvgVal = StatUtils.mean(listScaledDataVal);
			scaleStdDevVal = Math.sqrt(StatUtils.variance(listScaledDataVal));
			scaleAvgVal = Double.parseDouble(String.format("%.5f", scaleAvgVal));
			scaleStdDevVal = Double.parseDouble(String.format("%.5f", scaleStdDevVal));
			minVal = StatUtils.min(listData);
			maxVal = StatUtils.max(listData);
			
			// Get Scaled Limit
			Double engLclVal = getEngLclVal();
			Double engUclVal = getEngUclVal();
			Double sigmaVal = getSigmaVal();
			
			if(sigmaVal == null) {
				sigmaVal = 3.0;
			}
			
			double qVal = getQVal();
			double scaledLclVal = getQuartile(listScaledDataVal, qVal);
			double scaledUclVal = getQuartile(listScaledDataVal, 1 - qVal);
			
			if(engLclVal != null) {
				lclVal = engLclVal;
			}
			else {
				lclVal = setDataDescaling(listData, scaledLclVal);
			}
			
			if(engUclVal != null) {
				uclVal = engUclVal;
			}
			else {
				uclVal = setDataDescaling(listData, scaledUclVal);
			}
			
			Double engTargetVal = getEngTargetVal();
			if(engTargetVal != null) {
				targetVal = engTargetVal;
			}
			else {
				targetVal = 0.0;
			}
			
			stdDevVal = uclVal / sigmaVal;
			
			logger.info("====================================================================================================");
			logger.info("scaled lcl = {} , scaled ucl = {}", scaledLclVal, scaledUclVal);
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
			logger.error("NonParametricLimit::getLimit Exception");
			logger.error(e.getMessage());
		}
		
		return limitVo;
	}

}
