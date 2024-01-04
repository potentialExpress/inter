package com.verizon.connect.infra.interfaceservice.external.interfaces.webservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.ExternalInterfaceRequest;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.ExternalInterfaceResponse;
import com.verizon.connect.infra.interfaceservice.model.LocationEnum;
import com.verizon.connect.infra.interfaceservice.model.ServiceLevelEnum;
import com.verizon.connect.infra.interfaceservice.model.GetLocationListRequest;
import com.verizon.connect.infra.interfaceservice.model.TNPoolSummeryRequest;
import com.verizon.infrastructure.connect.genericdatabase.dto.GenericDataBaseResponseDTO;
import com.verizon.infrastructure.connect.genericdatabase.dto.GenericTableData;
import com.verizon.infrastructure.connect.genericdatabase.dto.PreparedQueryV3;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebServicesImplUtil {
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public String[] extractInputValuesOfGetETEBIDetailsRequest(ExternalInterfaceRequest request) {
		String[] inputRequestValues=new String[10];
		request.getPayload().stream().forEach(spec -> {
			if ("EnterpriseId".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[0] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("EBIName".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[1] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("EBIIPAddress".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[2] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("EBIEnabled".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[3] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("EBISearchCriteria".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[4] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("SipOption".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[5] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("EBIFields".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[6] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("SortCondition".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[7] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("Offset".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[8] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("Size".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[9] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			}
		});
		return inputRequestValues;
	}
	
	public List<String> extractEnterpriseTrunkIdsOfGetETEBIDetailsRequest(ExternalInterfaceRequest request) {
		List<String> enterpriseTrunkIdValues=new ArrayList<>();
		request.getPayload().stream().forEach(spec -> {
            if("EnterpriseTrunkId".equalsIgnoreCase(spec.getSpecCode()) && (spec.getSpecValue() != null
					&& spec.getSpecValue().toString().trim().length() > 0)) {
			enterpriseTrunkIdValues.add(String.valueOf(spec.getSpecValue()));
            }
		});
		return enterpriseTrunkIdValues;
	}
	
	public List<PreparedQueryV3> extractEbiDetailsPreparedQueryList(String[] inputRequestValues,
			List<String> enterpriseTrunkIdValues) {
		List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
		PreparedQueryV3 preparedQuery = new PreparedQueryV3();
		PreparedQueryV3 preparedQuery2 = new PreparedQueryV3();
		preparedQuery.setKey("searchSortAndPagination");
		preparedQuery2.setKey("finalSearchSortAndPagination");
		StringBuffer ebiQuery = new StringBuffer();
		StringBuffer ebiQuery2 = new StringBuffer();
		if(!CollectionUtils.isEmpty(enterpriseTrunkIdValues)) {
			ebiQuery.append("and et_ebi_data.enterprise_trunk_id in ('");
			for (int n = 0; n < enterpriseTrunkIdValues.size(); n++) {
				ebiQuery.append(enterpriseTrunkIdValues.get(n)+"'");
				ebiQuery.append(",");
		    }
		    if (ebiQuery.length() > 0) {
		    	ebiQuery.deleteCharAt(ebiQuery.lastIndexOf(","));
		    }
		    ebiQuery.append(") ");
		}
		boolean isEbiNameOrIpAddressNotNull=(inputRequestValues[1] != null || inputRequestValues[2] != null) ;
		if(isEbiNameOrIpAddressNotNull || inputRequestValues[3] != null || inputRequestValues[4] != null
				|| inputRequestValues[5] != null) {
			ebiSearchCriteriaPreparedQuery(inputRequestValues, ebiQuery);
		}
		
		if(inputRequestValues[6] != null || inputRequestValues[7] != null) {
			if("EBIName".equals(inputRequestValues[6]) && "ASC".equals(inputRequestValues[7])) {
				ebiQuery.append(" order by tbl_tso_ebi.entity_name asc ");
			} else if("EBIName".equals(inputRequestValues[6]) && "DESC".equals(inputRequestValues[7])) {
				ebiQuery.append(" order by tbl_tso_ebi.entity_name desc ");
			}
			
			if("EnterpriseTrunkName".equals(inputRequestValues[6]) && "ASC".equals(inputRequestValues[7])) {
				ebiQuery.append("order by et_ebi_data.enterprise_trunk_name asc ");
			} else if("EnterpriseTrunkName".equals(inputRequestValues[6]) && "DESC".equals(inputRequestValues[7])) {
				ebiQuery.append(" order by et_ebi_data.enterprise_trunk_name desc ");
			}
			//ESVRRS-18365
			if("SipOption".equals(inputRequestValues[6]) && "ASC".equals(inputRequestValues[7])) {
				ebiQuery.append(" order by entity_attribute_array_ping_status ->> 'specValue' asc ");
			} else if("SipOption".equals(inputRequestValues[6]) && "DESC".equals(inputRequestValues[7])) {
				ebiQuery.append(" order by entity_attribute_array_ping_status ->> 'specValue' desc ");
			}
			//ESVRRS-18365
		}
		
		
		if(inputRequestValues[6] != null || inputRequestValues[7] != null) {
			if("EBIName".equals(inputRequestValues[6]) && "ASC".equals(inputRequestValues[7])) {
				ebiQuery2.append(" order by ebi_name asc ");
			} else if("EBIName".equals(inputRequestValues[6]) && "DESC".equals(inputRequestValues[7])) {
				ebiQuery2.append(" order by ebi_name desc ");
			}
			
			if("EnterpriseTrunkName".equals(inputRequestValues[6]) && "ASC".equals(inputRequestValues[7])) {
				ebiQuery2.append("order by enterprise_trunk_name asc ");
			} else if("EnterpriseTrunkName".equals(inputRequestValues[6]) && "DESC".equals(inputRequestValues[7])) {
				ebiQuery2.append(" order by enterprise_trunk_name desc ");
			}
			//ESVRRS-18365
			if("SipOption".equals(inputRequestValues[6]) && "ASC".equals(inputRequestValues[7])) {
				ebiQuery2.append(" order by ping_status asc ");
			} else if("SipOption".equals(inputRequestValues[6]) && "DESC".equals(inputRequestValues[7])) {
				ebiQuery2.append(" order by ping_status desc ");
			}
			//ESVRRS-18365
		}	
		
		preparedQuery.setSqlText(ebiQuery.toString());

		preparedQuery2.setSqlText(ebiQuery2.toString());
		
		preparedQueryList.add(preparedQuery);
		preparedQueryList.add(preparedQuery2);
		return preparedQueryList;
	}

	private void ebiSearchCriteriaPreparedQuery(String[] inputRequestValues, StringBuffer ebiQuery) {
		if(inputRequestValues[4] != null){
			log.info("In side getEBISearchValue and criteria is :  "+inputRequestValues[4]);
			if("BEGINS_WITH".equals(inputRequestValues[4]) || "CONTAINS".equals(inputRequestValues[4])
					|| "ENDS_WITH".equals(inputRequestValues[4])){
				if(inputRequestValues[1]!= null) {
					ebiQuery.append(" and tbl_tso_ebi.entity_name like '%");
					ebiQuery.append(inputRequestValues[1]+"%'");
				} else if(inputRequestValues[2] != null) {
					ebiQuery.append(" and entity_attribute_array_ebi_ip_address ->> 'specValue' like '%");
					ebiQuery.append(inputRequestValues[2]+"%'");
				}
			} else if("EQUALS".equals(inputRequestValues[4])){
				if(inputRequestValues[1] != null) {
					ebiQuery.append(" and tbl_tso_ebi.entity_name = '");
					ebiQuery.append(inputRequestValues[1]+"'");
				} else if(inputRequestValues[2] != null) {
					ebiQuery.append(" and entity_attribute_array_ebi_ip_address ->> 'specValue' = '");
					ebiQuery.append(inputRequestValues[2]+"'");
				}
			}
		} else {
			if(inputRequestValues[1] != null) {
				ebiQuery.append(" and tbl_tso_ebi.entity_name = '");
				ebiQuery.append(inputRequestValues[1]+"'");
			} else if(inputRequestValues[2] != null) {
				ebiQuery.append(" and entity_attribute_array_ebi_ip_address ->> 'specValue' = '");
				ebiQuery.append(inputRequestValues[2]+"'");
			}
		}
		
		if(inputRequestValues[3] != null) {
			if("Yes".equals(inputRequestValues[3])) {
				ebiQuery.append(" and entity_attribute_array_cust_bound_ebi_status ->> 'specValue'='1' ");
			} else {
				ebiQuery.append(" and entity_attribute_array_cust_bound_ebi_status ->> 'specValue'='0' ");
			}
		}
		//ESVRRS-18365
		if(inputRequestValues[5] != null) {						
			if("Yes".equals(inputRequestValues[5])) {
				ebiQuery.append(" and entity_attribute_array_ping_status ->> 'specValue'='1' ");
			} else { 
				ebiQuery.append(" and entity_attribute_array_ping_status ->> 'specValue'='0' ");
			}
		}
		//ESVRRS-18365
	}
	
	public Map<String, String> createHeaderMap(ExternalInterfaceRequest request) {
		Map<String,String> headerList = new HashMap<>();
		request.getHeader().getSpecifications().forEach(spec ->
			headerList.put(spec.getSpecCode().toLowerCase(), spec.getSpecValue()!= null ? spec.getSpecValue().toString(): null)
		);		
		return  headerList;
	}

	public void setSummaryContextId(ExternalInterfaceResponse externalInterfaceResponse, Map<String, String> headerList) {
		
		if(headerList.get("summarycontextid")!=null) {
			String response = externalInterfaceResponse.getRawResponse();
			JSONArray jArray = new JSONArray(response);
			JSONObject jObj = jArray.getJSONObject(0);
			JSONArray dList = jObj.getJSONArray("dataList");
			JSONObject newColumn = new JSONObject();
			newColumn.put("columnName", "SummaryContextID");
			newColumn.put("columnValue", headerList.get("summarycontextid"));
			dList.getJSONArray(0).put(newColumn);
			jObj.put("dataList", dList);
			response = jArray.toString();
			externalInterfaceResponse.setRawResponse(response);
		}
	}

	public Map<String, String> createValidationMap(ExternalInterfaceRequest request) {
		Map<String,String> validationList = new HashMap<>();
		request.getPayload().stream().forEach(spec -> 
			validationList.put(spec.getSpecCode(), (spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
					: null)
		);
		return validationList;
	}
	
	public String[] extractInputValuesOfGetECNDetails(ExternalInterfaceRequest request) {
		String[] inputRequestValues=new String[10];
		request.getPayload().stream().forEach(spec -> {
			if ("EnterpriseId".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[0] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("ECNId".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[1] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("ECNName".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[2] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("EBIName".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[3] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("EBIIPAddress".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[4] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("ECNSearchCriteria".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[5] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("ECNFields".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[6] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("SortCondition".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[7] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("Offset".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[8] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("Size".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[9] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			}
		});
		return inputRequestValues;
	}
	
	public List<PreparedQueryV3> extractGetECNDetailsPreparedQueryList(String[] inputRequestValues) throws JsonProcessingException {
		List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
		PreparedQueryV3 preparedQuery = new PreparedQueryV3();
		preparedQuery.setKey("searchSortAndPagination");
		StringBuffer ecnQuery = new StringBuffer();

		boolean isEbiNameOrIpAddressNotNull=(inputRequestValues[1] != null || inputRequestValues[2] != null) ;
		if(isEbiNameOrIpAddressNotNull || inputRequestValues[3] != null || inputRequestValues[4] != null
				|| inputRequestValues[5] != null) {
			ecnSearchCriteriaPreparedQuery(inputRequestValues, ecnQuery);
		}
		
		if(inputRequestValues[6] != null || inputRequestValues[7] != null) {
			if("ECNId".equals(inputRequestValues[6]) && "ASC".equals(inputRequestValues[7])) {
				ecnQuery.append(" ORDER BY ECNE.ECNE_ID ASC ");
			} else if("ECNId".equals(inputRequestValues[6]) && "DESC".equals(inputRequestValues[7])) {
				ecnQuery.append(" ORDER BY ECNE.ECNE_ID DESC ");
			}
			
			if("ECNName".equals(inputRequestValues[6]) && "ASC".equals(inputRequestValues[7])) {
				ecnQuery.append(" ORDER BY ECNE.ECNE_NAME ASC ");
			} else if("ECNName".equals(inputRequestValues[6]) && "DESC".equals(inputRequestValues[7])) {
				ecnQuery.append(" ORDER BY ECNE.ECNE_NAME DESC ");
			}
			if("ECNCity".equals(inputRequestValues[6]) && "ASC".equals(inputRequestValues[7])) {
				ecnQuery.append(" ORDER BY ECNE.ECNE_CITY ASC ");
			} else if("ECNCity".equals(inputRequestValues[6]) && "DESC".equals(inputRequestValues[7])) {
				ecnQuery.append("ORDER BY ECNE.ECNE_CITY DESC ");
			}
			
			if("ECNState".equals(inputRequestValues[6]) && "ASC".equals(inputRequestValues[7])) {
				ecnQuery.append(" ORDER BY ECNE.ECNE_STATE ASC ");
			} else if("ECNState".equals(inputRequestValues[6]) && "DESC".equals(inputRequestValues[7])) {
				ecnQuery.append("ORDER BY ECNE.ECNE_STATE DESC ");
			}
			
			if("EBIName".equals(inputRequestValues[6]) && "ASC".equals(inputRequestValues[7])) {
				ecnQuery.append(" ORDER BY ECNE.EBI_NAME ASC ");
			} else if("EBIName".equals(inputRequestValues[6]) && "DESC".equals(inputRequestValues[7])) {
				ecnQuery.append("ORDER BY ECNE.EBI_NAME DESC ");
			}
			
			if("EBIIPAddress".equals(inputRequestValues[6]) && "ASC".equals(inputRequestValues[7])) {
				ecnQuery.append(" ORDER BY ECNE.EBI_IP ASC ");
			} else if("EBIIPAddress".equals(inputRequestValues[6]) && "DESC".equals(inputRequestValues[7])) {
				ecnQuery.append("ORDER BY ECNE.EBI_IP DESC ");
			}
			
		}
		
		ecnQuery.append(") ecne WHERE r > ");
		ecnQuery.append(inputRequestValues[8]);
		ecnQuery.append(" AND r <= ");
		ecnQuery.append(Integer.valueOf(inputRequestValues[8]) +Integer.valueOf(inputRequestValues[9]));
		
		log.info("getECNDetails preparedQuery search and sort: "+objectMapper.writeValueAsString(ecnQuery));
		preparedQuery.setSqlText(ecnQuery.toString());
		
		preparedQueryList.add(preparedQuery);
		return preparedQueryList;
	}
	
	private void ecnSearchCriteriaPreparedQuery(String[] inputRequestValues, StringBuffer ecnQuery) {
		String searchByEbiNameQuery="select\r\n" + 
				"	trunk_group_ecn_group_id as ecne_id\r\n" + 
				"from\r\n" + 
				"	(\r\n" + 
				"	select\r\n" + 
				"		distinct trunk_group_child_entity_attributes_array_attributes1 ->> 'specValue' as trunk_group_ecn_group_id,\r\n" + 
				"		trunk_group_child_entity_attributes_array_attributes2 ->> 'specValue' as trunk_group_ebi_id\r\n" + 
				"	from\r\n" + 
				"		 (\r\n" + 
				"		select\r\n" + 
				"			jsonb_array_elements(cast(enterprise_trunk_group_child_data.ecn_group_array_child_entity_data as jsonb))->>'childEntityAttributes' as enterprise_trunk_group_child_entity_attributes_array\r\n" + 
				"		from\r\n" + 
				"			(\r\n" + 
				"			select\r\n" + 
				"				ecn_group_array->>'childEntityData' as ecn_group_array_child_entity_data\r\n" + 
				"			from\r\n" + 
				"				svcinv.t_service_inventory,\r\n" + 
				"				jsonb_array_elements(entity_child_data -> 'entityChildReferenceData') ecn_group_array\r\n" + 
				"			where\r\n" + 
				"				entity_type = 'ENTERPRISE_TRUNK'\r\n" + 
				"				and ecn_group_array ->>'childEntityType' = 'TRUNK_GROUP'\r\n" + 
				"	        ) enterprise_trunk_group_child_data\r\n" + 
				"          ) trunk_group_key_and_child_attributes,\r\n" + 
				"		 jsonb_array_elements(cast(trunk_group_key_and_child_attributes.enterprise_trunk_group_child_entity_attributes_array as jsonb) ->'attributes') trunk_group_child_entity_attributes_array_attributes1,\r\n" + 
				"		 jsonb_array_elements(cast(trunk_group_key_and_child_attributes.enterprise_trunk_group_child_entity_attributes_array as jsonb) ->'attributes') trunk_group_child_entity_attributes_array_attributes2\r\n" + 
				"	where\r\n" + 
				"		trunk_group_child_entity_attributes_array_attributes1 ->> 'specName' = 'ECNE_ID'\r\n" + 
				"		and trunk_group_child_entity_attributes_array_attributes2 ->> 'specName' = 'EBI_ID')tbl_tso_trunk_group\r\n" + 
				"where\r\n" + 
				"	trunk_group_ebi_id \r\n" + 
				"          in\r\n" + 
				"          (\r\n" + 
				"	select\r\n" + 
				"		entity_id as ebi_id\r\n" + 
				"	from\r\n" + 
				"		svcinv.t_service_inventory tsi\r\n" + 
				"	where\r\n" + 
				"		entity_type = 'EBI'\r\n" + 
				"		and entity_name ";
		
		String searchByEbiIpQuery="select\r\n" + 
				"	trunk_group_ecn_group_id as ecne_id\r\n" + 
				"from\r\n" + 
				"	(\r\n" + 
				"	select\r\n" + 
				"		distinct trunk_group_child_entity_attributes_array_attributes1 ->> 'specValue' as trunk_group_ecn_group_id,\r\n" + 
				"		trunk_group_child_entity_attributes_array_attributes2 ->> 'specValue' as trunk_group_ebi_id\r\n" + 
				"	from\r\n" + 
				"		 (\r\n" + 
				"		select\r\n" + 
				"			jsonb_array_elements(cast(enterprise_trunk_group_child_data.ecn_group_array_child_entity_data as jsonb))->>'childEntityAttributes' as enterprise_trunk_group_child_entity_attributes_array\r\n" + 
				"		from\r\n" + 
				"			(\r\n" + 
				"			select\r\n" + 
				"				ecn_group_array->>'childEntityData' as ecn_group_array_child_entity_data\r\n" + 
				"			from\r\n" + 
				"				svcinv.t_service_inventory,\r\n" + 
				"				jsonb_array_elements(entity_child_data -> 'entityChildReferenceData') ecn_group_array\r\n" + 
				"			where\r\n" + 
				"				entity_type = 'ENTERPRISE_TRUNK'\r\n" + 
				"				and ecn_group_array ->>'childEntityType' = 'TRUNK_GROUP'\r\n" + 
				"	        ) enterprise_trunk_group_child_data\r\n" + 
				"          ) trunk_group_key_and_child_attributes,\r\n" + 
				"		 jsonb_array_elements(cast(trunk_group_key_and_child_attributes.enterprise_trunk_group_child_entity_attributes_array as jsonb) ->'attributes') trunk_group_child_entity_attributes_array_attributes1,\r\n" + 
				"		 jsonb_array_elements(cast(trunk_group_key_and_child_attributes.enterprise_trunk_group_child_entity_attributes_array as jsonb) ->'attributes') trunk_group_child_entity_attributes_array_attributes2\r\n" + 
				"	where\r\n" + 
				"		trunk_group_child_entity_attributes_array_attributes1 ->> 'specName' = 'ECNE_ID'\r\n" + 
				"		and trunk_group_child_entity_attributes_array_attributes2 ->> 'specName' = 'EBI_ID')tbl_tso_trunk_group\r\n" + 
				"where\r\n" + 
				"	trunk_group_ebi_id \r\n" + 
				"          in\r\n" + 
				"          (\r\n" + 
				"	select\r\n" + 
				"		entity_id as ebi_id\r\n" + 
				"	from\r\n" + 
				"		svcinv.t_service_inventory,\r\n" + 
				"		jsonb_array_elements(entity_attributes -> 'entityAttributes') entity_attribute_array_ebi_ip\r\n" + 
				"	where\r\n" + 
				"		entity_type = 'EBI'\r\n" + 
				"		and entity_attribute_array_ebi_ip ->> 'specName' ='EBI_IP'\r\n" + 
				"	    and entity_attribute_array_ebi_ip ->> 'specValue'";
		
		if(inputRequestValues[5] != null){
			log.info("In side ecnSearchCriteriaPreparedQuery and criteria is :  "+inputRequestValues[5]);
			if("BEGINS_WITH".equals(inputRequestValues[5]) || "CONTAINS".equals(inputRequestValues[5])
					|| "ENDS_WITH".equals(inputRequestValues[5])){
				if(inputRequestValues[1]!= null) {
					ecnQuery.append(" WHERE ECNE.ECNE_ID like '%");
					ecnQuery.append(inputRequestValues[1]+"%'");
				} else if(inputRequestValues[2] != null) {
					ecnQuery.append(" WHERE ECNE.ECNE_NAME like '%");
					ecnQuery.append(inputRequestValues[2]+"%'");
				}		
				else if(inputRequestValues[3] != null) {
					ecnQuery.append(" WHERE ecne.ecne_id in (");
					ecnQuery.append(searchByEbiNameQuery +"like '"+inputRequestValues[3]+"'"+"))");
				}
				else if(inputRequestValues[4] != null) {
					ecnQuery.append(" WHERE ecne.ecne_id in (");
					ecnQuery.append(searchByEbiIpQuery +"like '"+inputRequestValues[4]+"')"+"))");
				}
			} else if("EQUALS".equals(inputRequestValues[5])){
				if(inputRequestValues[1]!= null) {
					ecnQuery.append(" WHERE ECNE.ECNE_ID ='");
					ecnQuery.append(inputRequestValues[1]+"'");
				} else if(inputRequestValues[2] != null) {
					ecnQuery.append(" WHERE ECNE.ECNE_NAME = '");
					ecnQuery.append(inputRequestValues[2]+"'");
				}		
				else if(inputRequestValues[3] != null) {
					ecnQuery.append(" WHERE ecne.ecne_id in (");
					ecnQuery.append(searchByEbiNameQuery +"='"+inputRequestValues[3]+"'"+"))");
				}
				else if(inputRequestValues[4] != null) {
					ecnQuery.append(" WHERE ecne.ecne_id in (");
					ecnQuery.append(searchByEbiIpQuery +"= '"+inputRequestValues[4]+"')"+"))");
				}
			}
		} 

	}

	public String[] extractInputValuesOfEnterpriseTrunkLinesRequest(ExternalInterfaceRequest request) {
		String[] inputRequestValues=new String[16];
		request.getPayload().stream().forEach(spec -> {
  			if ("EnterpriseId".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[0] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("EnterpriseTrunkId".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[1] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("LocationId".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[2] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("StartTN".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[3] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("EndTN".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[4] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("ExcludeStartTN".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[5] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("ExcludeEndTN".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[6] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("CLIDFirstName".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[7] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("CLIDLastName".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[8] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("PrivateNumber".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[9] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("Extension".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[10] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("SearchCondition".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[11] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("ETLineFields".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[12] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("SortCondition".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[13] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("Offset".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[14] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("Size".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[15] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			}
		});
		return inputRequestValues;
	}

	
	
	public String[] extractInputValuesOfGetLinesForPBX(ExternalInterfaceRequest request) {
		String[] inputRequestValues=new String[9];
		request.getPayload().stream().forEach(spec -> {
  			if ("PBXID".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[0] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("StartTN".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[1] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("EndTN".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[2] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("Extension".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[3] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("SearchCondition".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[4] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			}  else if ("Offset".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[5] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("Size".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[6] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			}
			else if ("PBXLineFields".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[7] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			} else if ("SortCondition".equalsIgnoreCase(spec.getSpecCode())) {
				inputRequestValues[8] = (spec.getSpecValue() != null
						&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
								: null;
			}
		});
		return inputRequestValues;
	}
		public List<PreparedQueryV3> extractenterpriseTrunkLinesPreparedQueryList(String[] inputRequestValues) {
			List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
			PreparedQueryV3 preparedQuery = new PreparedQueryV3();

			preparedQuery.setKey("trunkLinesSearchSortAndPagination");
		
			StringBuffer trunklinesQuery = new StringBuffer();
			
			boolean iserpriseTrunkLinesNotNul=(inputRequestValues[3] != null || inputRequestValues[4] != null || inputRequestValues[5] != null);
			boolean iserpriseTrunkLinesNotNull=(inputRequestValues[6] != null || inputRequestValues[7] != null || inputRequestValues[8] != null);
			boolean iserpriseTrunkLinesNotNulll =(inputRequestValues[9] != null || inputRequestValues[10] != null || inputRequestValues[11] != null);
			if(iserpriseTrunkLinesNotNul || iserpriseTrunkLinesNotNull || iserpriseTrunkLinesNotNulll) {
				trunklinesSearchCriteriaPreparedQuery(inputRequestValues, trunklinesQuery);
			}
			
			if(inputRequestValues[12] != null || inputRequestValues[13] != null) {
				if("TN".equals(inputRequestValues[12]) && "ASC".equals(inputRequestValues[13])) {
					trunklinesQuery.append(" order by tbl_trunk_lines.tn asc ");
				} else if("TN".equals(inputRequestValues[12]) && "DESC".equals(inputRequestValues[13])) {
					trunklinesQuery.append(" order by tbl_trunk_lines.tn desc ");
				}
				
				if("CLIDFirstName".equals(inputRequestValues[12]) && "ASC".equals(inputRequestValues[13])) {
					trunklinesQuery.append(" order by tbl_trunk_lines.calling_line_id_first_name asc ");
				} else if("CLIDFirstName".equals(inputRequestValues[12]) && "DESC".equals(inputRequestValues[13])) {
					trunklinesQuery.append(" order by tbl_trunk_lines.calling_line_id_first_name desc ");
				}
				
 				if("CLIDLastName".equals(inputRequestValues[12]) && "ASC".equals(inputRequestValues[13])) {
					trunklinesQuery.append(" order by tbl_trunk_lines.calling_line_id_last_name asc ");
				} else if("CLIDLastName".equals(inputRequestValues[12]) && "DESC".equals(inputRequestValues[13])) {
					trunklinesQuery.append(" order by tbl_trunk_lines.calling_line_id_last_name desc ");
				}
				
				if("PrivateNumber".equals(inputRequestValues[12]) && "ASC".equals(inputRequestValues[13])) {
					trunklinesQuery.append(" order by tbl_trunk_lines.private_number asc ");
				} else if("PrivateNumber".equals(inputRequestValues[12]) && "DESC".equals(inputRequestValues[13])) {
					trunklinesQuery.append(" order by tbl_trunk_lines.private_number desc ");
				}
				
 				if("Extension".equals(inputRequestValues[12]) && "ASC".equals(inputRequestValues[13])) {
					trunklinesQuery.append(" order by tbl_trunk_lines.extention_length asc ");
				} else if("Extension".equals(inputRequestValues[12]) && "DESC".equals(inputRequestValues[13])) {
					trunklinesQuery.append(" order by tbl_trunk_lines.extention_length desc ");
				}
			}
			preparedQuery.setSqlText(trunklinesQuery.toString());
			preparedQueryList.add(preparedQuery);
			
			return preparedQueryList;
		}
		private void trunklinesSearchCriteriaPreparedQuery(String[] inputRequestValues, StringBuffer trunklinesQuery) {
			if(inputRequestValues[3] != null && inputRequestValues[3].length()> 0 && (inputRequestValues[4] == null || inputRequestValues[4].length() <= 0)) {
				trunklinesQuery.append(" where tbl_trunk_lines.tn in (select tsi.tn from svcinv.v_tn_inventory tsi,"
						+ "jsonb_array_elements(tsi.tn_attributes -> 'attributeValue') tnAttributes1,"
						+ "jsonb_array_elements(tsi.tn_attributes -> 'attributeValue') tnAttributes2"
						+ " where "
						+ "tnAttributes1 ->> 'specName'='ENTERPRISE_TRUNK_ID' and "
						+ "tnAttributes2 ->> 'specValue' = '"+inputRequestValues[1]+"' and "
						+ "tsi.tn like '"+inputRequestValues[3]+"')");
			}
			if((inputRequestValues[3] == null || inputRequestValues[3].length()<= 0) && inputRequestValues[4] != null && inputRequestValues[4].length() > 0) {
				trunklinesQuery.append(" where tbl_trunk_lines.tn in (select tsi.tn from svcinv.v_tn_inventory tsi,"
						+ "jsonb_array_elements(tsi.tn_attributes -> 'attributeValue') tnAttributes1,"
						+ "jsonb_array_elements(tsi.tn_attributes -> 'attributeValue') tnAttributes2"
						+ " where "
						+ "tnAttributes1 ->> 'specName'='ENTERPRISE_TRUNK_ID' and "
						+ "tnAttributes2 ->> 'specValue' = '"+inputRequestValues[1]+"' and "
						+ "tsi.tn like '"+inputRequestValues[4]+"')");
			}
			if((null != inputRequestValues[3] && inputRequestValues[3].length()> 0) && inputRequestValues[4] != null && inputRequestValues[4].length() > 0) {
				trunklinesQuery.append(" where tbl_trunk_lines.tn between '"+inputRequestValues[3]+"' and '"+inputRequestValues[4]+"'");
			}
			if(inputRequestValues[5] != null && inputRequestValues[5].length() > 0 && inputRequestValues[6] != null && inputRequestValues[6].length() > 0) {
				trunklinesQuery.append(" where tbl_trunk_lines.tn not between '"+inputRequestValues[3]+"' and '"+inputRequestValues[4]+"'");
			}
		}
		

		
		public List<PreparedQueryV3> extractGetLinesForPBXPreparedQueryList(String[] inputRequestValues) {
			List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
			PreparedQueryV3 preparedQuery = new PreparedQueryV3();

			preparedQuery.setKey("getLinesForPBXSearchSortAndPagination");
		
			StringBuffer pbxlinesQuery = new StringBuffer();
			
			boolean iserpriseTrunkLinesNotNul=(inputRequestValues[1] != null || inputRequestValues[2] != null );
		
			boolean iserpriseTrunkLinesNotNulll =(inputRequestValues[3] != null || inputRequestValues[4] != null);
			
			if(iserpriseTrunkLinesNotNul || iserpriseTrunkLinesNotNulll ) {
				getLinesForPBXPSearchCriteriaPreparedQuery(inputRequestValues, pbxlinesQuery);
			}
		
			if(inputRequestValues[7] != null || inputRequestValues[8] != null) {
				if("TN".equals(inputRequestValues[7]) && "ASC".equals(inputRequestValues[8])) {
					pbxlinesQuery.append(" order by tbl_pbx_lines.tn asc ");
				} else if("TN".equals(inputRequestValues[7]) && "DESC".equals(inputRequestValues[8])) {
					pbxlinesQuery.append(" order by tbl_pbx_lines.tn desc ");
				}
				
				if("CLIDFirstName".equals(inputRequestValues[7]) && "ASC".equals(inputRequestValues[8])) {
					pbxlinesQuery.append(" order by tbl_pbx_lines.calling_line_id_first_name asc ");
				} else if("CLIDFirstName".equals(inputRequestValues[7]) && "DESC".equals(inputRequestValues[8])) {
					pbxlinesQuery.append(" order by tbl_pbx_lines.calling_line_id_first_name desc ");
				}
				
 				if("CLIDLastName".equals(inputRequestValues[7]) && "ASC".equals(inputRequestValues[8])) {
 					pbxlinesQuery.append(" order by tbl_pbx_lines.calling_line_id_last_name asc ");
				} else if("CLIDLastName".equals(inputRequestValues[7]) && "DESC".equals(inputRequestValues[8])) {
					pbxlinesQuery.append(" order by tbl_pbx_lines.calling_line_id_last_name desc ");
				}
				
				if("PrivateNumber".equals(inputRequestValues[7]) && "ASC".equals(inputRequestValues[8])) {
					pbxlinesQuery.append(" order by tbl_pbx_lines.private_number asc ");
				} else if("PrivateNumber".equals(inputRequestValues[7]) && "DESC".equals(inputRequestValues[8])) {
					pbxlinesQuery.append(" order by tbl_pbx_lines.private_number desc ");
				}
				
 				if("Extension".equals(inputRequestValues[7]) && "ASC".equals(inputRequestValues[8])) {
 					pbxlinesQuery.append(" order by tbl_pbx_lines.extention_length asc ");
				} else if("Extension".equals(inputRequestValues[7]) && "DESC".equals(inputRequestValues[8])) {
					pbxlinesQuery.append(" order by tbl_pbx_lines.extention_length desc ");
				}
			}
			preparedQuery.setSqlText(pbxlinesQuery.toString());
			preparedQueryList.add(preparedQuery);
			
			return preparedQueryList;
		}
		
		private void getLinesForPBXPSearchCriteriaPreparedQuery(String[] inputRequestValues, StringBuffer pbxlinesQuery) {
			if(inputRequestValues[1] != null && inputRequestValues[1].length()> 0 && (inputRequestValues[2] == null || inputRequestValues[2].length() <= 0)) {
				pbxlinesQuery.append(" where tbl_pbx_lines.tn in (select tsi.tn from tninv.t_tn_inventory tsi,"
						+ "jsonb_array_elements(tsi.tn_attributes -> 'attributeValue') tnAttributes1,"
						+ "jsonb_array_elements(tsi.tn_attributes -> 'attributeValue') tnAttributes2"
						+ " where "
						+ "tnAttributes1 ->> 'specName'='GROUP_ID' and "
						+ "tnAttributes2 ->> 'specValue' = '"+inputRequestValues[0]+"' and "
						+ "tsi.tn like '"+inputRequestValues[1]+"')");
			}
			if((inputRequestValues[1] == null || inputRequestValues[1].length()<= 0) && inputRequestValues[2] != null && inputRequestValues[2].length() > 0) {
				pbxlinesQuery.append(" where tbl_pbx_lines.tn in (select tsi.tn from tninv.t_tn_inventory tsi,"
						+ "jsonb_array_elements(tsi.tn_attributes -> 'attributeValue') tnAttributes1,"
						+ "jsonb_array_elements(tsi.tn_attributes -> 'attributeValue') tnAttributes2"
						+ " where "
						+ "tnAttributes1 ->> 'specName'='GROUP_ID' and "
						+ "tnAttributes2 ->> 'specValue' = '"+inputRequestValues[0]+"' and "
						+ "tsi.tn like '"+inputRequestValues[2]+"')");
			}
			if((null != inputRequestValues[1] && inputRequestValues[1].length()> 0) && inputRequestValues[2] != null && inputRequestValues[2].length() > 0) {
				pbxlinesQuery.append(" where tbl_pbx_lines.tn between '"+inputRequestValues[1]+"' and '"+inputRequestValues[2]+"'");
			}
		}
		
public String[] extractInputValuesOfgetRingingNumberSummary(ExternalInterfaceRequest request) {
			
			String[] inputRequestValues=new String[9];
			request.getPayload().stream().forEach(spec -> {
	  			if ("DepartmentID".equalsIgnoreCase(spec.getSpecCode())) {
					inputRequestValues[0] = (spec.getSpecValue() != null
							&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
									: null;
				} else if ("LocationID".equalsIgnoreCase(spec.getSpecCode())) {
					inputRequestValues[1] = (spec.getSpecValue() != null
							&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
									: null;
				} else if ("Number".equalsIgnoreCase(spec.getSpecCode())) {
					inputRequestValues[2] = (spec.getSpecValue() != null
							&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
									: null;
				} else if ("Type".equalsIgnoreCase(spec.getSpecCode())) {
					inputRequestValues[3] = (spec.getSpecValue() != null
							&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
									: null;
				} else if ("SearchCondition".equalsIgnoreCase(spec.getSpecCode())) {
					inputRequestValues[4] = (spec.getSpecValue() != null
							&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
									: null;
				} else if ("SortFields".equalsIgnoreCase(spec.getSpecCode())) {
					inputRequestValues[5] = (spec.getSpecValue() != null
							&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
									: null;
				} else if ("SortCondition".equalsIgnoreCase(spec.getSpecCode())) {
					inputRequestValues[6] = (spec.getSpecValue() != null
							&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
									: null;
				} else if ("Offset".equalsIgnoreCase(spec.getSpecCode())) {
					inputRequestValues[7] = (spec.getSpecValue() != null
							&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
									: null;
				} else if ("Size".equalsIgnoreCase(spec.getSpecCode())) {
					inputRequestValues[8] = (spec.getSpecValue() != null
							&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
									: null;
				}
			});
			return inputRequestValues;
		}

	public List<PreparedQueryV3> extractRingingNumberSummaryPreparedQueryList(String[] inputRequestValues) {
				List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
				PreparedQueryV3 preparedQuery = new PreparedQueryV3();

				preparedQuery.setKey("ringingNumberSummarySearchSort");
			
				StringBuffer ringingSummaryQuery = new StringBuffer();

				if(inputRequestValues[5] != null || inputRequestValues[6] != null) {
					
					if("CLIDFirstName".equals(inputRequestValues[5]) && "ASC".equals(inputRequestValues[6])) {
						ringingSummaryQuery.append(" order by tbl_ringing_summary.cid_first_name asc ");
					} else if("CLIDFirstName".equals(inputRequestValues[5]) && "DESC".equals(inputRequestValues[6])) {
						ringingSummaryQuery.append(" order by tbl_ringing_summary.cid_first_name desc ");
					}
					
	 				if("CLIDLastName".equals(inputRequestValues[5]) && "ASC".equals(inputRequestValues[6])) {
	 					ringingSummaryQuery.append(" order by tbl_ringing_summary.cid_last_name asc ");
					} else if("CLIDLastName".equals(inputRequestValues[5]) && "DESC".equals(inputRequestValues[6])) {
						ringingSummaryQuery.append(" order by tbl_ringing_summary.cid_last_name desc ");
					}
				}
				preparedQuery.setSqlText(ringingSummaryQuery.toString());
				preparedQueryList.add(preparedQuery);
				
				return preparedQueryList;
			}
			public void setResponseStatus(ExternalInterfaceResponse externalInterfaceResponse, Map<String, String> statusList) {
					
					if(statusList.get("TransactionId")!=null) {
						String response = externalInterfaceResponse.getRawResponse();
						JSONArray jArray = new JSONArray(response);
						JSONObject jObj = jArray.getJSONObject(0);
						JSONArray dList = jObj.getJSONArray("dataList");
						JSONObject newColumn = new JSONObject();
						newColumn.put("columnName", "TransactionId");
						newColumn.put("columnValue", statusList.get("TransactionId"));
						newColumn.put("columnName", "StatusCode");
						newColumn.put("columnValue", statusList.get("0"));
						newColumn.put("columnName", "StatusText");
						newColumn.put("columnValue", statusList.get("Success"));
						dList.getJSONArray(0).put(newColumn);
						jObj.put("dataList", dList);
						response = jArray.toString();
						externalInterfaceResponse.setRawResponse(response);
					}
			}
			
			public String[] extractInputValuesOfgetSubscriberSummary(ExternalInterfaceRequest request) {
				
				String[] inputRequestValues=new String[15];
				request.getPayload().stream().forEach(spec -> {
		  			if ("EnterpriseID".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[0] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("DepartmentID".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[1] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("LocationID".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[2] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("FirstName".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[3] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("LastName".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[4] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("TN".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[5] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("Extension".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[6] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("PrivateNumber".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[7] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("VECUserName".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[8] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}
					else if ("VMBoxNumber".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[9] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}
					else if ("SearchCondition".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[10] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}
					else if ("SubscriberFields".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[11] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}
					else if ("SortCondition".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[12] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}
					else if ("Offset".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[13] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}
					else if ("Size".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[14] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}
		  			
				});
				return inputRequestValues;
			}
			
			public List<PreparedQueryV3> extractSubscriberSummaryPreparedQueryList(String[] inputRequestValues,List<String> sortList) {
				List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
				PreparedQueryV3 preparedQuery = new PreparedQueryV3();
				preparedQuery.setKey("SubscriberSummarySearchSort");
				StringBuffer sqlQuery = new StringBuffer();
				if(inputRequestValues[2] != null) {
					sqlQuery.append(" tbl_Subscriber_summary.location_id ='"+inputRequestValues[2]+"'");
					if(inputRequestValues[1] != null) {
						sqlQuery.append(" and tbl_Subscriber_summary.depart_id ='"+inputRequestValues[1]+"'");
					}
				}
				else if(inputRequestValues[2] != null && inputRequestValues[1] != null){
					
					sqlQuery.append(" tbl_Subscriber_summary.depart_id ='"+inputRequestValues[1]+"'");
				}
				else if(inputRequestValues[0] != null && inputRequestValues[1] != null && inputRequestValues[2] != null){
					sqlQuery.append(" tbl_Subscriber_summary.customer_id ='"+inputRequestValues[0]+"'");
				}
				else
					sqlQuery.append(" true ");
				
				if(inputRequestValues[3] != null)
					sqlQuery.append(" "+inputRequestValues[10] + " tbl_Subscriber_summary.first_name =  '"+inputRequestValues[3]+"'");
				if(inputRequestValues[4] != null)
					sqlQuery.append(" "+inputRequestValues[10]+ " tbl_Subscriber_summary.last_name = '"+inputRequestValues[4]+"'");
				if(inputRequestValues[5] != null)
					sqlQuery.append(" "+inputRequestValues[10]+ " tbl_Subscriber_summary.tn = '"+inputRequestValues[5]+"'");
				if(inputRequestValues[6] != null)
						sqlQuery.append(" "+inputRequestValues[10]+ " tbl_Subscriber_summary.extension = '"+inputRequestValues[6]+"'");
				if(inputRequestValues[7]!= null)
					sqlQuery.append(" "+inputRequestValues[10]+ " tbl_Subscriber_summary.private_num = '"+inputRequestValues[7]+"'");
				if(inputRequestValues[8]!= null)
					sqlQuery.append(" "+inputRequestValues[10]+ " tbl_Subscriber_summary.VECUserName = '"+inputRequestValues[8]+"'");
				if(inputRequestValues[9]!= null)
					sqlQuery.append(" "+inputRequestValues[10]+ " tbl_Subscriber_summary.VM_BOX_NUM = '"+inputRequestValues[9]+"'");
				if(!sortList.isEmpty()) {
					sqlQuery.append(" order by");
				
				for(int i = 0; i< sortList.size();i++) {
					sqlQuery.append(" tbl_Subscriber_summary."+sortList.get(i));
					if(i!=sortList.size()-1)
						sqlQuery.append(",");
				}
				
				sqlQuery.append(" "+inputRequestValues[12]);
				}
				preparedQuery.setSqlText(sqlQuery.toString());
				preparedQueryList.add(preparedQuery);
				
				return preparedQueryList;
			}

			public String[] getInputValuesLocationDetails(ExternalInterfaceRequest request) {
				
				String[] inputValues = new String[4];
				request.getPayload().stream().forEach(spec -> {
					if ("ListofTNs".equalsIgnoreCase(spec.getSpecCode())) {
						inputValues[0] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
								? String.valueOf(spec.getSpecValue())
								: null;
					}
					if ("DialingCountryCode".equalsIgnoreCase(spec.getSpecCode())) {
						inputValues[1] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
								? String.valueOf(spec.getSpecValue())
								: null;
					}
				});
				
				request.getHeader().getSpecifications().stream().forEach(spec-> {
					
					if ("TransactionID".equalsIgnoreCase(spec.getSpecCode())) {
						inputValues[2] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
								? String.valueOf(spec.getSpecValue())
								: null;
					}
				});
				return inputValues;
			}
	/**
	  * This is to check if the TN has any records
	  */
			public List<String> checkNoRecordsFoundTn(List<String> locAndTnNotInEsap,ExternalInterfaceResponse externalInterfaceResponse, String tnId) {
				
				if(externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null) {
					
					String response = externalInterfaceResponse.getRawResponse();
					JSONArray jArray = new JSONArray(response);
					JSONObject jObj = jArray.getJSONObject(jArray.length()-1);
					JSONArray dList = jObj.getJSONArray("dataList");
					if(dList == null || dList.length()==0) {
						locAndTnNotInEsap.add(tnId);
						
					}
				}
				else {
					locAndTnNotInEsap.add(tnId);
				}
				return locAndTnNotInEsap;
			}
			
			public List<String> extractLocationIdValuesOfGetSTNDetails(ExternalInterfaceRequest request) {
				
				List<String>extractLocationIdValues=new ArrayList<>();
				request.getPayload().stream().forEach(spec -> {
		            if("LocationId".equalsIgnoreCase(spec.getSpecCode()) && (spec.getSpecValue() != null
							&& spec.getSpecValue().toString().trim().length() > 0)) {
		            	extractLocationIdValues.add(String.valueOf(spec.getSpecValue()));
		            }
				});
				return extractLocationIdValues;
			
			}
			
			public List<String> extractTsoLocationIdValues(ExternalInterfaceRequest request) {
				
				List<String>extractLocationIdValues=new ArrayList<>();
				request.getPayload().stream().forEach(spec -> {
		            if("TSOLocationId".equalsIgnoreCase(spec.getSpecCode()) && (spec.getSpecValue() != null
							&& spec.getSpecValue().toString().trim().length() > 0)) {
		            	extractLocationIdValues.add(String.valueOf(spec.getSpecValue()));
		            }
				});
				return extractLocationIdValues;
			
			}

			public List<String> extractTransactionIdOfGetSTNDetails(ExternalInterfaceRequest request) {
				
				List<String>extractLocationIdValues=new ArrayList<>();
				request.getHeader().getSpecifications().stream().forEach(spec -> {
		            if("TransactionId".equalsIgnoreCase(spec.getSpecCode())) {
		            	extractLocationIdValues.add(String.valueOf(spec.getSpecValue()));
		            }
				});
				return extractLocationIdValues;
			
			}
			
			public String extractLocationTypeOfGetSTNDetails(List<GenericDataBaseResponseDTO> dbResponse) {
				
				final StringBuilder locType = new StringBuilder();
				List<GenericTableData> tableData = dbResponse.get(0).getDataList().get(0);
				tableData.forEach(data -> {
					if("location_type".equalsIgnoreCase(data.getColumnName())) {
						locType.append(data.getColumnValue());
					}
				});
				return locType.toString();
			}
			
			public String extractAniEnabledOfLocationDetails(List<GenericTableData> tableData) {
				
				final StringBuilder locType = new StringBuilder();
				tableData.forEach(data -> {
					if("ENHANCED_ANI_IND".equalsIgnoreCase(data.getColumnName())) {
						locType.append(data.getColumnValue());
					}
				});
				return locType.toString();
			}
			
			public String extractServiceLevelOfLocationDetails(List<GenericTableData> tableData) {
				
				final StringBuilder serviceLevel = new StringBuilder();
				tableData.forEach(data -> {
					if("service_level".equalsIgnoreCase(data.getColumnName())) {
						serviceLevel.append(data.getColumnValue());
					}
				});
				
				return ServiceLevelEnum.getEnumValue(serviceLevel.toString()); 
			}
			
			public List<String> extractWorkOrderIdValuesOfGetTNsDetails(ExternalInterfaceRequest request) {
				
				List<String> extractWorkOrderIdValues=new ArrayList<>();
				request.getPayload().stream().forEach(spec -> {
		            if("WOID".equalsIgnoreCase(spec.getSpecCode()) && (spec.getSpecValue() != null
							&& spec.getSpecValue().toString().trim().length() > 0)) {
		            	extractWorkOrderIdValues.add(String.valueOf(spec.getSpecValue()));
		            }
				});
				return extractWorkOrderIdValues;
			}

			public List<PreparedQueryV3> extractLocationIdsOfGetLocAddressRequest(Map<String, String> validationList) {
				List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
				PreparedQueryV3 preparedQuery = new PreparedQueryV3();
				preparedQuery.setKey("LocationOrTnConditionCheck");
				StringBuffer buffer = new StringBuffer();

				if (!StringUtils.isEmpty((validationList.get("LocationId")))) {
					buffer.append(" svcinv.entity_id = '");
					buffer.append(validationList.get("LocationId") + "'");
				}

				if (!StringUtils.isEmpty((validationList.get("TN")))
						&& !StringUtils.isEmpty((validationList.get("LocationId")))) {
					buffer.append(" and tninv.tn = '");
					buffer.append(validationList.get("TN") + "'");
				} else if (!StringUtils.isEmpty((validationList.get("TN")))
						&& StringUtils.isEmpty((validationList.get("LocationId")))) {
					buffer.append(" tninv.tn = '");
					buffer.append(validationList.get("TN") + "'");
				}

				preparedQuery.setSqlText(buffer.toString());
				preparedQueryList.add(preparedQuery);
				return preparedQueryList;
			}
			
			public boolean checkDataListIsEmpty(ExternalInterfaceResponse externalInterfaceResponse) {
				
				if(externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null) {
					JSONArray jArray = new JSONArray(externalInterfaceResponse.getRawResponse());
					JSONObject jObj = jArray.getJSONObject(0);
					JSONArray dList = jObj.getJSONArray("dataList");
					if(dList != null && dList.length() > 0) {
						return true;
					}
				}
				return false;
			}
			
			public String[] getInputValuesLocationDetailsSegment(ExternalInterfaceRequest request) {
				
				String[] inputValues = new String[6];
				request.getPayload().stream().forEach(spec -> {
					if ("ListOfTNs".equalsIgnoreCase(spec.getSpecCode())) {
						inputValues[0] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
								? String.valueOf(spec.getSpecValue())
								: null;
					}
					else if ("DialingCountryCode".equalsIgnoreCase(spec.getSpecCode())) {
						inputValues[1] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
								? String.valueOf(spec.getSpecValue())
								: null;
					}
					else if ("location_id".equalsIgnoreCase(spec.getSpecCode())) {
						inputValues[2] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
								? String.valueOf(spec.getSpecValue())
								: null;
					}
					else if ("LastSegment".equalsIgnoreCase(spec.getSpecCode())) {
						inputValues[3] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
								? String.valueOf(spec.getSpecValue())
								: null;
					}
				});
				
				request.getHeader().getSpecifications().stream().forEach(spec-> {
					
					if ("TransactionID".equalsIgnoreCase(spec.getSpecCode())) {
						inputValues[4] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
								? String.valueOf(spec.getSpecValue())
								: null;
					}
				});
				return inputValues;
			}
			
			public List<PreparedQueryV3> extractLocationID(Map<String, String> validationList) {
				List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
				PreparedQueryV3 preparedQuery = new PreparedQueryV3();
				preparedQuery.setKey("LocationIDConditionCheck");

				StringBuilder buffer = new StringBuilder();

				if (!StringUtils.isEmpty((validationList.get("LocationID")))) {
					
					buffer.append("'"+validationList.get("LocationID") + "'");
				}

				preparedQuery.setSqlText(buffer.toString());
				preparedQueryList.add(preparedQuery);
				return preparedQueryList;
			}
			
			public String[] extractInputValuesOfDLS(ExternalInterfaceRequest request) {
				String[] inputRequestValues=new String[15];
				request.getPayload().stream().forEach(spec -> {
		  			if ("DeviceID".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[0] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("EnterpriseID".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[1] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("DepartmentID".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[2] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("LocationID".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[3] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("BroadsoftUserID".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[4] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("ResourceType".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[5] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("TN".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[6] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("Extension".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[7] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					} else if ("PrivateNumber".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[8] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}else if ("CallerIDFirstName".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[9] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}else if ("CallerIDLastName".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[10] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}else if ("SearchCondition".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[11] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}
					else if ("SortCondition".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[12] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}
					else if ("Offset".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[13] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}
					else if ("Size".equalsIgnoreCase(spec.getSpecCode())) {
						inputRequestValues[14] = (spec.getSpecValue() != null
								&& spec.getSpecValue().toString().trim().length() > 0) ? String.valueOf(spec.getSpecValue())
										: null;
					}
				});
				return inputRequestValues;
			}

			public List<String> extractsortListOfDLS(ExternalInterfaceRequest request) {
				
				
				return request.getPayload().stream()
				.filter(spec -> "SortFields".equalsIgnoreCase(spec.getSpecCode()) && StringUtils.isNotBlank(String.valueOf(spec.getSpecValue())))
				.map(spec -> String.valueOf(spec.getSpecValue()))
				.collect(Collectors.toList());
			}
			
			public List<PreparedQueryV3> preparedQueryListforDLS(Map<String,String> validationList, List<String> sortList) {
				List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
				PreparedQueryV3 preparedQuery = new PreparedQueryV3();

				preparedQuery.setKey("dlSummarySearchSort");
				StringBuffer sqlQuery = new StringBuffer();
				String searchCon = validationList.get("SearchCondition");
				String sortCon = validationList.get("SortCondition");
				sqlQuery.append("t2.location_id = t1.loc_id");
				if(validationList.get("EnterpriseID") != null)
					sqlQuery.append(" "+searchCon + " t1.eid =  '"+validationList.get("EnterpriseID")+"'");
				if(validationList.get("DepartmentID") != null)
					sqlQuery.append(" "+searchCon+ " t1.department_id = '"+validationList.get("DepartmentID")+"'");
				if(validationList.get("LocationID") != null)
					sqlQuery.append(" "+searchCon+ " t1.location_id = '"+validationList.get("LocationID")+"'");
				if(validationList.get("Extension") != null)
						sqlQuery.append(" "+searchCon+ " t2.extension = '"+validationList.get("Extension")+"'");
				if(validationList.get("PrivateNumber") != null)
					sqlQuery.append(" "+searchCon+ " t2.private_num = '"+validationList.get("PrivateNumber")+"'");
				if(validationList.get("CallerIDFirstName") != null)
					sqlQuery.append(" "+searchCon+ " t1.CLID_FIRST_NAME = '"+validationList.get("CallerIDFirstName")+"'");
				if(validationList.get("CallerIDLastName") != null)
					sqlQuery.append(" "+searchCon+ " t1.CLID_LAST_NAME = '"+validationList.get("CallerIDLastName")+"'");
				if(validationList.get("TN") != null)
					sqlQuery.append(" "+searchCon+ " t2.tn = '"+validationList.get("TN"));
				if(validationList.get("BroadsoftUserID") != null )
					sqlQuery.append(" "+searchCon+ " t2.BroadsoftUserID = '"+validationList.get("BroadsoftUserID")+"'");
				if(!sortList.isEmpty()) {
						sqlQuery.append(" order by");
					
					for(int i = 0; i< sortList.size();i++) {
						sqlQuery.append(" t2."+sortList.get(i));
						if(i!=sortList.size()-1)
							sqlQuery.append(",");
					}
					sqlQuery.append(" "+sortCon);
				}
				preparedQuery.setSqlText(sqlQuery.toString());
				preparedQueryList.add(preparedQuery);
				
				return preparedQueryList;
			}
			
			public List<PreparedQueryV3> getTNPoolSummeryPreparedQuery(TNPoolSummeryRequest getTNPoolSummeryRequest) {
				List<PreparedQueryV3> preparedQueryList = new ArrayList<>();

				StringBuffer searchByTNPoolQuery = new StringBuffer();
				searchByTNPoolQuery.append(" AND svcinv.enterprise_id ='").append(getTNPoolSummeryRequest.getEnterpriseID());
				searchByTNPoolQuery.append("' AND svcinv.location_id = '"+getTNPoolSummeryRequest.getLocationID()).append("'");
				
				if(getTNPoolSummeryRequest.getStartTN()!=null && getTNPoolSummeryRequest.getEndTN()!=null) {
					searchByTNPoolQuery.append(" AND tninv.tn BETWEEN '").append(getTNPoolSummeryRequest.getStartTN());
					searchByTNPoolQuery.append("' AND  '").append(getTNPoolSummeryRequest.getEndTN()).append("'");
				}
				if("YES".equalsIgnoreCase(getTNPoolSummeryRequest.getActivated())) {
					searchByTNPoolQuery.append(" AND tnAttributes6 ->> 'specValue' = 'ACTIVATED'");
				}else if("NO".equalsIgnoreCase(getTNPoolSummeryRequest.getActivated())) {
					searchByTNPoolQuery.append(" AND tnAttributes6 ->> 'specValue' = 'DEACTIVATED'");
				}
				if("TN".equalsIgnoreCase(getTNPoolSummeryRequest.getTnFields())) {
					searchByTNPoolQuery.append(" order by tninv.tn ");
					if(StringUtils.isNotBlank(getTNPoolSummeryRequest.getSortCondition()))
						searchByTNPoolQuery.append(getTNPoolSummeryRequest.getSortCondition());
				}
				PreparedQueryV3 preparedQueryV3 = new PreparedQueryV3();
				preparedQueryV3.setKey("getTNPoolSummarySortSearch");
				preparedQueryV3.setSqlText(searchByTNPoolQuery.toString());
				preparedQueryList.add(preparedQueryV3);
				
				return preparedQueryList;
				
			}
			
			public List<PreparedQueryV3> getLocationListPreparedQuery(GetLocationListRequest getLocationListRequest) {
				List<PreparedQueryV3> preparedQueryList = new ArrayList<>();

				StringBuffer searchByTNPoolQuery = new StringBuffer();
				searchByTNPoolQuery.append(" AND enterprise ->> 'entityId' like '%").append(getLocationListRequest.getEnterpriseID()).append("%'");
				
				if(StringUtils.isNotBlank(getLocationListRequest.getLocationName())) {
					searchByTNPoolQuery.append(" AND  svcinv.entity_name like '%").append(getLocationListRequest.getLocationName()).append("%'");
				}
				if(StringUtils.isNotBlank(getLocationListRequest.getCircuitId())) {
					searchByTNPoolQuery.append(" AND  entityAttributes6 ->> 'specValue' like '%").append(getLocationListRequest.getCircuitId()).append("%'");
				}
				PreparedQueryV3 preparedQueryV3 = new PreparedQueryV3();
				preparedQueryV3.setKey("getLocationListSearch");
				preparedQueryV3.setSqlText(searchByTNPoolQuery.toString());
				preparedQueryList.add(preparedQueryV3);
				
				return preparedQueryList;
				
			}

			public List<String> extractsortListOfSubSummary(ExternalInterfaceRequest request) {
				
				
				return request.getPayload().stream()
						.filter(spec -> "SortFields".equalsIgnoreCase(spec.getSpecCode()) && StringUtils.isNotBlank(String.valueOf(spec.getSpecValue())))
						.map(spec -> String.valueOf(spec.getSpecValue()))
						.collect(Collectors.toList());
				
			}
			
}
