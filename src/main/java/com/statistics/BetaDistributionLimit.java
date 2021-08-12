package com.statistics;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statistics.vo.LimitVo;

public class BetaDistributionLimit extends CommonLimit {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public BetaDistributionLimit() {
		setLimitCalcLogicTyp("BetaDistribution");
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
			stdDevVal = Math.sqrt(StatUtils.variance(listData));
			
			if(stdDevVal == 0) {
				logger.error("Recal Fail (std = 0, beta Distribution alpha, beta = infiniy. Recal Beta Distribution Limit Impossible");
				
				return null;
			}
			
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
			
			scaleAvgVal = StatUtils.mean(listScaledDataVal);
			scaleStdDevVal = Math.sqrt(StatUtils.variance(listScaledDataVal));
			scaleAvgVal = Double.parseDouble(String.format("%.5f", scaleAvgVal));
			scaleStdDevVal = Double.parseDouble(String.format("%.5f", scaleStdDevVal));
			minVal = StatUtils.min(listData);
			maxVal = StatUtils.max(listData);
			
			// Get Scaled Limit
			double uVal = (scaleAvgVal * (1 - scaleAvgVal) / (scaleStdDevVal * scaleStdDevVal)) - 1;
			double alphaVal = scaleAvgVal * uVal;
			double betaVal = (1 - scaleAvgVal) * uVal;
			
			logger.info("====================================================================================================");
			logger.info("scaled avg = {} , scaled stddev = {}", scaleAvgVal, scaleStdDevVal);
			logger.info("alpha = {}, beta = {} , u = {}", alphaVal, betaVal, uVal);
			logger.info("====================================================================================================");
			
			BetaDistribution betaDis = new BetaDistribution(alphaVal, betaVal);
			NormalDistribution normalDis = new NormalDistribution();
			
			double sigmaVal = getSigmaVal();
			double pVal = normalDis.cumulativeProbability(sigmaVal);
			if(pVal < 0 || 1 < pVal) {
				logger.error("Recal Fail (P value at endpoints do not in [0 , 1]");
				
				return null;
			}
			
			double betaLclVal = betaDis.inverseCumulativeProbability(1 - pVal);
			double betaUclVal = betaDis.inverseCumulativeProbability(pVal);
			
			Double engLclVal = getEngLclVal();
			Double engUclVal = getEngUclVal();
			
			if(engLclVal != null) {
				lclVal = engLclVal;
			}
			else {
				lclVal = setDataDescaling(listData, betaLclVal);
			}
			
			if(engUclVal != null) {
				uclVal = engUclVal;
			}
			else {
				uclVal = setDataDescaling(listData, betaUclVal);
			}
			
			logger.info("====================================================================================================");
			logger.info("p = {}, 1-p = {}", pVal, 1 - pVal);
			logger.info("beta lcl = {} , beta ucl = {}", betaLclVal, betaUclVal);
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
			logger.error("BetaDistributionLimit::getLimit Exception");
			logger.error(e.getMessage());
		}
		
		return limitVo;
	}
}
