package com.aimir.service.system.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aimir.dao.system.TariffEMDao;
import com.aimir.dao.system.TariffTypeDao;
import com.aimir.model.system.TariffEM;
import com.aimir.model.system.TariffType;
import com.aimir.service.system.TariffTypeManager;
import com.aimir.util.StringUtil;

@WebService(endpointInterface = "com.aimir.service.system.TariffTypeManager")
@Service(value = "tariffTypeManager")
public class TariffTypeManagerImpl implements TariffTypeManager{
	
	private static Log log = LogFactory.getLog(TariffTypeManagerImpl.class);

	@Autowired
	TariffTypeDao dao;

    @Autowired
    TariffEMDao tariffEMDao;

	public List<TariffType> getAll() {
		return dao.getAll();
	}

	public List<TariffType> getTariffTypeBySupplier(String serviceType, Integer supplierId) {
		return dao.getTariffTypeBySupplier(serviceType, supplierId);
	}

	public List<TariffType> getTariffTypeList(Integer supplier , Integer serviceType) {
		return dao.getTariffTypeList(supplier, serviceType);
	}
	
	public TariffType getTariffType(Integer id) {
		return dao.get(id);
	}
	
	@Transactional(readOnly=false)
	public void delete(TariffType tariffType) {
		dao.delete(tariffType);
	}

	/**
	 * method name : getTariffSupplySizeComboData<b/>
	 * method Desc : TariffEM 의 SupplySize ComboData 를 조회한다.
	 *
	 * @param conditionMap
	 * @return
	 */
	public Map<String, Object> getTariffSupplySizeComboData(Map<String, Object> conditionMap) {
	    Map<String, Object> result = new HashMap<String, Object>();
	    List<Map<String, Object>> list = tariffEMDao.getTariffSupplySizeComboData(conditionMap);
	    List<Map<String, Object>> combo = new ArrayList<Map<String, Object>>();
	    Map<String, Object> data = null;
	    StringBuilder sbText = new StringBuilder();
	    StringBuilder sbCode = new StringBuilder();
	    boolean isEmpty = false;
	    
	    if (list != null) {
	        for (Map<String, Object> map : list) {
                if ((map.get("supplySizeMin") != null && !StringUtil.nullToBlank(map.get("condition1")).isEmpty())
                        || (map.get("supplySizeMax") != null && !StringUtil.nullToBlank(map.get("condition2")).isEmpty())) {
                    sbCode.delete(0, sbCode.length());
                    sbText.delete(0, sbText.length());

                    if (map.get("supplySizeMin") != null && !StringUtil.nullToBlank(map.get("condition1")).isEmpty()) {
                        sbText.append(map.get("supplySizeMin")).append(" ");
                        if (map.get("condition1").toString().equals(">")) {
                            sbText.append("＜");
                        } else if (map.get("condition1").toString().equals(">=")) {
                            sbText.append("≤");
                        }
                        sbText.append(" ");
                    }
                    sbText.append("x ");

                    if (map.get("supplySizeMax") != null && !StringUtil.nullToBlank(map.get("condition2")).isEmpty()) {

                        if (map.get("condition2").toString().equals("<")) {
                            sbText.append("＜");
                        } else if (map.get("condition2").toString().equals("<=")) {
                            sbText.append("≤");
                        }
                        sbText.append(" ").append(map.get("supplySizeMax"));
                    }
                    sbCode.append(StringUtil.nullToBlank(map.get("supplySizeMin"))).append(",").append(StringUtil.nullToBlank(map.get("condition1"))).append(",");
                    sbCode.append(StringUtil.nullToBlank(map.get("supplySizeMax"))).append(",").append(StringUtil.nullToBlank(map.get("condition2")));
                    data = new HashMap<String, Object>();
                    data.put("id", sbCode.toString());
                    data.put("name", sbText.toString());

                    combo.add(data);
                }
	        }
	    }

	    if (list == null || list.size() <= 0) {
	        isEmpty = true;
	    }

	    result.put("empty", isEmpty);
	    result.put("result", combo);
	    return result;
	}
	
	/**
	 * Suni쪽에 내리는 Tariff를 계산.
	 */
	public List<Map<String,Object>> getSTSTariff(Integer tariffIndexId, String yyyymmdd) {
		List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		Map<String,Object> tariffMap = getTariff(tariffIndexId, yyyymmdd);
		List<TariffEM> tariffList = (List<TariffEM>) tariffMap.get("tariffList");
		String tariffName = (String) tariffMap.get("tariffName");
		
		if("Residential".equals(tariffName)) {
			for (int i = 0; i < tariffList.size(); i++) {
				TariffEM tariffEM = tariffList.get(i);
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("cons", tariffEM.getSupplySizeMin());
				map.put("fixedRate", tariffEM.getServiceCharge());
				Double publicLevy = (tariffEM.getActiveEnergyCharge() * tariffEM.getTransmissionNetworkCharge());
				Double govLevy = (tariffEM.getActiveEnergyCharge() * tariffEM.getDistributionNetworkCharge());
				Double varRate = (tariffEM.getActiveEnergyCharge() + publicLevy + govLevy);
				map.put("varRate", Double.parseDouble( String.format( "%.4f" , varRate ) ));
				map.put("condRate1", tariffEM.getAdminCharge() == null ? 0d : -1*Double.parseDouble( String.format( "%.4f" , tariffEM.getAdminCharge() ) ));
				map.put("condRate2", tariffEM.getMaxDemand() == null ? 0d : -1*Double.parseDouble( String.format( "%.4f" , tariffEM.getMaxDemand() ) ));
				returnList.add(map);
			}
			
		} else if("Non Residential".equals(tariffName)) {
			for (int i = 0; i < tariffList.size(); i++) {
				TariffEM tariffEM = tariffList.get(i);
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("cons", tariffEM.getSupplySizeMin());
				map.put("fixedRate", tariffEM.getServiceCharge());
				Double publicLevy = (tariffEM.getActiveEnergyCharge() * tariffEM.getTransmissionNetworkCharge());
				Double govLevy = (tariffEM.getActiveEnergyCharge() * tariffEM.getDistributionNetworkCharge());
				Double varRate = (tariffEM.getActiveEnergyCharge() + publicLevy + govLevy) * (tariffEM.getEnergyDemandCharge() + 1);
				map.put("varRate", Double.parseDouble( String.format( "%.4f" , varRate ) ));
				map.put("condRate1", tariffEM.getAdminCharge() == null ? 0d : -1*Double.parseDouble( String.format( "%.4f" , tariffEM.getAdminCharge())));
				map.put("condRate2", tariffEM.getRateRebalancingLevy() == null ? 0d : -1*Double.parseDouble( String.format( "%.4f" , tariffEM.getRateRebalancingLevy())));
				returnList.add(map);
				System.out.println("map info : " + map);
			}
		}
		
		return returnList;
	}
	
	/**
	 * WSION용 getTariff
	 */
	public List<Map<String,Object>> getSTSDlmsTariff(Integer tariffIndexId, String yyyymmdd) {
		List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		Map<String,Object> tariffMap = getTariff(tariffIndexId, yyyymmdd);
		List<TariffEM> tariffList = (List<TariffEM>) tariffMap.get("tariffList");
		String tariffName = (String) tariffMap.get("tariffName");
		
		if("Residential".equals(tariffName)) {
			for (int i = 0; i < tariffList.size(); i++) {
				TariffEM tariffEM = tariffList.get(i);
				Map<String,Object> map = new HashMap<String,Object>();
				
				map.put("cons", tariffEM.getSupplySizeMin()); // Supply Size
				map.put("fixedRate", tariffEM.getServiceCharge());
				
				double condRate2 = tariffEM.getRateRebalancingLevy() == null ? 0d : Double.parseDouble(String.format("%.4f", tariffEM.getRateRebalancingLevy()));
				double condRate1 = tariffEM.getAdminCharge() == null ? 0d : Double.parseDouble(String.format("%.4f", tariffEM.getAdminCharge()));
				double govLey = tariffEM.getDistributionNetworkCharge() == null ? 0d : Double.parseDouble(String.format("%.4f", tariffEM.getDistributionNetworkCharge()));
				double streetLightLevy = tariffEM.getTransmissionNetworkCharge() == null ? 0d : Double.parseDouble(String.format("%.4f", tariffEM.getTransmissionNetworkCharge()));
				double vat = tariffEM.getEnergyDemandCharge() == null ? 0d : Double.parseDouble(String.format("%.4f", tariffEM.getEnergyDemandCharge()));
				double lifeLineSubsidy = tariffEM.getReactiveEnergyCharge() == null ? 0d : Double.parseDouble(String.format("%.4f", tariffEM.getReactiveEnergyCharge()));
				
				map.put("condRate2", condRate2); // Utility Relief
				map.put("condRate1", condRate1); // Normal Subsidy
				map.put("activeEnergyCharge", tariffEM.getActiveEnergyCharge());
				map.put("govLey", govLey);
				map.put("streetLightLevy", streetLightLevy);
				map.put("vat", vat);
				map.put("lifeLineSubsidy", lifeLineSubsidy);
				
				log.debug("map: " + map);
				returnList.add(map);
			}
		} else if("Non Residential".equals(tariffName)) {
			for (int i = 0; i < tariffList.size(); i++) {
				TariffEM tariffEM = tariffList.get(i);
				Map<String,Object> map = new HashMap<String,Object>();
				
				map.put("cons", tariffEM.getSupplySizeMin()); // Supply Size
				map.put("fixedRate", tariffEM.getServiceCharge());
				
				double condRate2 = tariffEM.getRateRebalancingLevy() == null ? 0d : Double.parseDouble(String.format("%.4f", tariffEM.getRateRebalancingLevy()));
				double condRate1 = tariffEM.getAdminCharge() == null ? 0d : Double.parseDouble(String.format("%.4f", tariffEM.getAdminCharge()));
				double govLey = tariffEM.getDistributionNetworkCharge() == null ? 0d : Double.parseDouble(String.format("%.4f", tariffEM.getDistributionNetworkCharge()));
				double streetLightLevy = tariffEM.getTransmissionNetworkCharge() == null ? 0d : Double.parseDouble(String.format("%.4f", tariffEM.getTransmissionNetworkCharge()));
				double vat = tariffEM.getEnergyDemandCharge() == null ? 0d : Double.parseDouble(String.format("%.4f", tariffEM.getEnergyDemandCharge()));
				double lifeLineSubsidy = tariffEM.getReactiveEnergyCharge() == null ? 0d : Double.parseDouble(String.format("%.4f", tariffEM.getReactiveEnergyCharge()));
				
				map.put("condRate2", condRate2); // Utility Relief
				map.put("condRate1", condRate1); // Normal Subsidy
				map.put("activeEnergyCharge", tariffEM.getActiveEnergyCharge());
				map.put("govLey", govLey);
				map.put("streetLightLevy", streetLightLevy);
				map.put("vat", vat);
				map.put("lifeLineSubsidy", lifeLineSubsidy);
				
				log.debug("map: " + map);
				returnList.add(map);
			}
		}
		
		return returnList;
	}
	
	public Map<String,Object> getTariff(Integer tariffIndexId, String yyyymmdd) {
		Map<String,Object> map = new HashMap<String,Object>();
        TariffType tariffType = dao.get(tariffIndexId);
        
        Map<String, Object> tariffParam = new HashMap<String, Object>();

        tariffParam.put("tariffIndex", tariffType);
        tariffParam.put("searchDate", yyyymmdd);
        
        List<TariffEM> tariffList = tariffEMDao.getApplyedTariff(tariffParam);
        
        Collections.sort(tariffList, new Comparator<TariffEM>() {

            @Override
            public int compare(TariffEM t1, TariffEM t2) {
                return t1.getSupplySizeMin() < t2.getSupplySizeMin()? -1:1;
            }
        });
        map.put("tariffName", tariffType.getName());
        map.put("tariffList", tariffList);
        return map;
    }
	
}