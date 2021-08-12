package com.statistics;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statistics.vo.LimitVo;

public class MovingRangeLimit extends CommonLimit {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public MovingRangeLimit() {
		setLimitCalcLogicTyp("MovingRange");
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
			
			double[] listMovingRangeDataVal = new double[listData.length - 1];
			for(int i=1; i<listData.length; ++i) {
				listMovingRangeDataVal[i - 1] = Math.abs(listData[i] - listData[i - 1]);
			}
			
			Median median = new Median();
			double movingRangeAvgVal = StatUtils.mean(listMovingRangeDataVal);
			avgVal = StatUtils.mean(listData);
			medianVal = median.evaluate(listData);
			
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
			
			stdDevVal = null;
			scaleAvgVal = null;
			scaleStdDevVal = null;
			minVal = StatUtils.min(listData);
			maxVal = StatUtils.max(listData);
			
			// Get Limit
			Double engLclVal = getEngLclVal();
			Double engUclVal = getEngUclVal();
			Double sigmaVal = getSigmaVal();
			
			if(sigmaVal == null) {
				sigmaVal = 2.66;
			}
			
			if(engLclVal != null) {
				lclVal = engLclVal;
			}
			else {
				lclVal = targetVal - (sigmaVal * movingRangeAvgVal);
			}
			
			if(engUclVal != null) {
				uclVal = engUclVal;
			}
			else {
				uclVal = targetVal + (sigmaVal * movingRangeAvgVal);
			}
			
			logger.info("====================================================================================================");
			logger.info("lcl = {} , ucl = {}", lclVal, uclVal);
			logger.info("====================================================================================================");
			
			limitVo.setTargetVal(targetVal);
			limitVo.setStdDevVal(stdDevVal);
			limitVo.setAvgVal(movingRangeAvgVal);
			limitVo.setMedianVal(medianVal);
			limitVo.setLclVal(lclVal);
			limitVo.setUclVal(uclVal);
			limitVo.setScaleAvgVal(scaleAvgVal);
			limitVo.setScaleStdDevVal(scaleStdDevVal);
			limitVo.setBefMinVal(minVal);
			limitVo.setBefMaxVal(maxVal);
		}
		catch (Exception e) {
			logger.error("MovingRangeLimit::getLimit Exception");
			logger.error(e.getMessage());
		}
		
		return limitVo;
	}

}
