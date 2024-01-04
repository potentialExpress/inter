package com.verizon.connect.infra.interfaceservice.external.interfaces.webservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.ExternalInterfaceRequest;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.ExternalInterfaceResponse;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.Header;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.LocationDetailsByTNwithCC;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.Specification;
import com.verizon.connect.infra.interfaceservice.external.interfaces.taskexecution.RestDbOperationServiceConnector;
import com.verizon.connect.infra.interfaceservice.external.interfaces.taskexecution.Task;
import com.verizon.connect.infra.interfaceservice.model.om.Status;
import com.verizon.infrastructure.connect.genericdatabase.dto.GenericDataBaseResponseDTO;
import com.verizon.infrastructure.connect.genericdatabase.dto.GenericTableData;
import com.verizon.infrastructure.connect.genericdatabase.dto.PreparedQueryV3;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebServicesImplIpro {
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	RestDbOperationServiceConnector restDbOperationServiceConnector;
	
	@Autowired
	WebServicesImplUtil webServicesImplUtil;
	
	@Autowired
	WebServicesImplUtil2 webServicesImplUtil2;

	public void getSTNDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		log.info("Inside  getSTNDetails, request::" + objectMapper.writeValueAsString(request));

		Status status=new Status();
		List<String> inputRequestValues = webServicesImplUtil.extractLocationIdValuesOfGetSTNDetails(request);
		List<String> transactionIdValues = webServicesImplUtil.extractTransactionIdOfGetSTNDetails(request);

		if (inputRequestValues.isEmpty() || transactionIdValues.isEmpty()) {
			
			webServicesImplUtil2.setCommonErrorStatus(WebServicesErrorCodes.MISSING_LOCATION_ID.errorCode, WebServicesErrorCodes.MISSING_LOCATION_ID.errorDesc, status, externalInterfaceResponse);
		} else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
			List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(
					externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {});
			
			List<GenericTableData> tableData = dbResponse.get(0).getDataList().get(0);
			webServicesImplUtil2.setSTNValues(task, request, taskList, dbResponse, tableData);

			GenericTableData transactionData = new GenericTableData();
			transactionData.setColumnName(request.getHeader().getSpecifications().get(0).getSpecCode());
			transactionData.setColumnValue(request.getHeader().getSpecifications().get(0).getSpecValue().toString());
			tableData.add(transactionData); 
			
			dbResponse.get(0).getDataList().clear();
			dbResponse.get(0).getDataList().add(tableData);
			externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
			externalInterfaceResponse.setRawBusinessResponse(dbResponse);

			log.info("Exited  getSTNDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
		}
	}

	
	public void getIproLocationDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		log.info("Inside  getIproLocationDetails, request::" + objectMapper.writeValueAsString(request));
		boolean islocid = false ;
		Map<String, String> headerList = webServicesImplUtil.createValidationMap(request);
		Status status = new Status();
		
		Specification specLoc = new Specification();
		
		List<Specification> specList =webServicesImplUtil2.setCommonSpecs(request);
		specLoc.setSpecCode("locId");
		if((request.getPayload().get(0).getSpecValue()) != null)
		{
		specLoc.setSpecValue(request.getPayload().get(0).getSpecValue());
		specList.add(specLoc);
		}		
		if((request.getPayload().get(0).getSpecValue()) == null || StringUtils.isEmpty(request.getPayload().get(0).getSpecValue().toString()))
        {
			specLoc.setSpecValue("0000");
			specList.add(specLoc);
		}
		Header header = new Header("getIproLocationDetails", "IPRO", specList);
		request.setHeader(header);

		if (StringUtils.isNoneEmpty(headerList.get("LocationId"))){
		  Task tasks = new Task(); 
		  tasks.setTaskName("getLocationId");
		  tasks.setUrl(task.getUrl());
		  ExternalInterfaceResponse externalInterfaceResponseForLocId = new ExternalInterfaceResponse();
		  restDbOperationServiceConnector.executeRestDbOperationApi(tasks, request,
		  taskList, externalInterfaceResponseForLocId);
		  islocid = webServicesImplUtil.checkDataListIsEmpty(externalInterfaceResponseForLocId);
			  
			  if(islocid == false) {
				  webServicesImplUtil2.setCommonErrorStatus(WebServicesErrorCodes.SUCCESS.errorCode, WebServicesErrorCodes.SUCCESS.errorDesc, status, externalInterfaceResponse);
			  }
		}
		
		if (StringUtils.isEmpty(headerList.get("LocationId"))) {
			webServicesImplUtil2.setCommonErrorStatus(WebServicesErrorCodes.SUCCESS.errorCode, WebServicesErrorCodes.SUCCESS.errorDesc, status, externalInterfaceResponse);
		}

		if (StringUtils.isEmpty(headerList.get("LocationId")) && StringUtils.isEmpty(headerList.get("TN"))) {
			webServicesImplUtil2.setCommonErrorStatus(WebServicesErrorCodes.MISSING_LOC_ID.errorCode, WebServicesErrorCodes.MISSING_LOC_ID.errorDesc, status, externalInterfaceResponse);
		}

		else if(islocid == true){
			externalInterfaceResponse.setHeader(header);
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil2
					.extractLocationIdsOfIproLocationDetailsRequest(headerList);
			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponse, preparedQueryList);
		}
		log.info("Exited  getIproLocationDetails, response::"
				+ objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getLocAddress(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		log.info("Inside  getLocAddress, request::" + objectMapper.writeValueAsString(request));
		Map<String,String> validationList = webServicesImplUtil.createValidationMap(request);
		Status status = new Status();		
		 
		 if (StringUtils.isEmpty(validationList.get("LocationId")) && StringUtils.isEmpty(validationList.get("TN"))) {
			 
			 webServicesImplUtil2.setCommonErrorStatus(WebServicesErrorCodes.FAILURE.errorCode, WebServicesErrorCodes.FAILURE.errorDesc, status, externalInterfaceResponse);
			 
			}
		
		else {
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil.extractLocationIdsOfGetLocAddressRequest(validationList);		
			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponse, preparedQueryList);
			}
		log.info("Exited  getLocAddress, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
		
		}
	
	public void getLocationDetailsBySegmentwithCC(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {

		
		Map<String, String> validationList = webServicesImplUtil.createValidationMap(request);

		LocationDetailsByTNwithCC details = new LocationDetailsByTNwithCC();
		if (validationList.get("ListOfTNs") == null) {
			webServicesImplUtil2.setCommonErrorDetails(WebServicesErrorCodes.MISSING_TN.errorCode, WebServicesErrorCodes.MISSING_TN.errorDesc, details, externalInterfaceResponse);
		} else if (validationList.get("LastSegment") == null) {
			webServicesImplUtil2.setCommonErrorDetails(WebServicesErrorCodes.MISSING_ENTERPRISE_TRUNK_ID.errorCode,
					WebServicesErrorCodes.MISSING_ENTERPRISE_TRUNK_ID.errorDesc, details, externalInterfaceResponse);
		} else {
			log.info("json request:==="+request.getPayload());
			task.setOriginatingSystem("IPRO");
			String[] inputValues = webServicesImplUtil.getInputValuesLocationDetailsSegment(request);
			List<String> locAndTnNotInEsap = new ArrayList<>();
			String[] tnstr = inputValues[0].replace("\r", "").replace("\n", "").replace("\t", "").split(",");
			Map<String, String> locTypeDtl = new HashMap<>();
			Map<String, ArrayList<String>> locAndTninEsap = new HashMap<>();
			details.setTransactionId(inputValues[4]);
			
			boolean isSingleSTN = false;
			int tnLength = tnstr.length;
			Map<String, ArrayList<String>> otherLocTns = new HashMap<>();
			String locationId = inputValues[2];
			String segment = inputValues[3];
			details.setDiallingCountryCode(inputValues[1]);
			String cc = inputValues[1];
			isSingleSTN = webServicesImplUtil2.isSingleSTN(task, request, taskList, isSingleSTN, tnLength);
			webServicesImplUtil2.commonGetLocationDetailsbySegmentESAP(task, request, taskList, inputValues, locAndTnNotInEsap, 
					locTypeDtl, locAndTninEsap);
			
			webServicesImplUtil2.commonSetOtherLocations(task, request, taskList, inputValues, locAndTnNotInEsap, otherLocTns, cc);

			if (locAndTnNotInEsap != null && !locAndTnNotInEsap.isEmpty()) {
				for (int k = 0; k< locAndTnNotInEsap.size(); k++) {
					ExternalInterfaceRequest req =  new ExternalInterfaceRequest();
					String tn = locAndTnNotInEsap.get(k);
					Specification spec = new Specification();
					spec.setSpecCode("ListOfTNs");
					spec.setSpecValue(cc.concat(tn));
					List<Specification> specs = new ArrayList<>();
					specs.add(spec);
					req.setPayload(specs);
					ExternalInterfaceResponse externalInterfaceResponseSingle= new ExternalInterfaceResponse();
					webServicesImplUtil2.queryByTaskName("checkSingleStnESAP",task, request, taskList,
							externalInterfaceResponseSingle);
					locAndTnNotInEsap.remove(tn); 
				}
			}

			String str = locAndTnNotInEsap.stream().collect(Collectors.joining(","));
			
			details.setNoRecordFoundTNs(str);
			if (segment != null && "Y".equalsIgnoreCase(segment)) {

				webServicesImplUtil2.commonSetLocationSegmentDetails( details, locAndTnNotInEsap,locAndTninEsap);
				webServicesImplUtil2.getLocationSegmentDetails(locAndTninEsap, isSingleSTN, externalInterfaceResponse, task, taskList,
						locTypeDtl, locationId);
				
				webServicesImplUtil2.setOtherFormattedTns(details, otherLocTns);
				
				webServicesImplUtil2.setCommonErrorDetails(WebServicesErrorCodes.SUCCESS.errorCode, WebServicesErrorCodes.SUCCESS.errorDesc, details, externalInterfaceResponse);				 
				
			} 
		}
		log.info("Exited  getLocationDetailsSegmentWithCC, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));

	}


	public void getLocationDetailsByTN(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		log.info("Inside  getLocationDetailsByTN, request::" + objectMapper.writeValueAsString(request));
		Status status =  new Status();
		
		Map<String,String> validationList = webServicesImplUtil.createValidationMap(request); 
		if (validationList.get("ListofTNs") == null) {
			webServicesImplUtil2.setCommonErrorStatus(WebServicesErrorCodes.MISSING_TN.errorCode,
					WebServicesErrorCodes.MISSING_TN.errorDesc, status, externalInterfaceResponse);

		} else {
		String[] inputValues = webServicesImplUtil.getInputValuesLocationDetails(request);
		List<String> locAndTnNotInEsap =  new ArrayList<>();
		String[] tnstr = inputValues[0].replace("\r","").replace("\n","").replace("\t","").split(",");
		
		for (String tnId : tnstr) {
			request.getPayload().stream().forEach(spec -> {
				if ("ListofTNs".equalsIgnoreCase(spec.getSpecCode())) {
					spec.setSpecValue(tnId.replace("\r","").replace("\n","").replace("\t",""));
				}
			});
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
			log.info("ExternalInterfaceResponse: === " + externalInterfaceResponse);
			webServicesImplUtil.checkNoRecordsFoundTn(locAndTnNotInEsap ,externalInterfaceResponse, tnId);
		}
		String str = locAndTnNotInEsap.stream().collect(
	            Collectors.joining(","));
		LocationDetailsByTNwithCC details = new LocationDetailsByTNwithCC();
		details.setDiallingCountryCode(inputValues[1]);
		details.setNoRecordFoundTNs(str);
		details.setTransactionId(inputValues[2]);
		webServicesImplUtil2.setCommonErrorDetails("0", WebServicesErrorCodes.SUCCESS.errorDesc, details, externalInterfaceResponse);
		}
		log.info("Exited  getLocationDetailsByTN, response::"
				+ objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getLocationDetailsByTNwithCC(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		log.info("Inside  getLocationDetailsByTNwithCC, request::" + objectMapper.writeValueAsString(request));

		LocationDetailsByTNwithCC details = new LocationDetailsByTNwithCC();
		Map<String,String> validationList = webServicesImplUtil.createValidationMap(request); 
		if (validationList.get("ListofTNs") == null) {
			webServicesImplUtil2.setCommonErrorDetails(WebServicesErrorCodes.MISSING_TN.errorCode, WebServicesErrorCodes.MISSING_TN.errorDesc, details, externalInterfaceResponse);
		} else if (validationList.get("DialingCountryCode") == null) {
			webServicesImplUtil2.setCommonErrorDetails(WebServicesErrorCodes.MISSING_DIALING_COUNTRY_CODE.errorCode, WebServicesErrorCodes.MISSING_DIALING_COUNTRY_CODE.errorDesc, details, externalInterfaceResponse);
		}
		else {
		String[] inputValues = webServicesImplUtil.getInputValuesLocationDetails(request);
		List<String> locAndTnNotInEsap =  new ArrayList<>();
		String[] tnstr = inputValues[0].replace("\r","").replace("\n","").replace("\t","").split(",");
		List<List<GenericTableData>> v= new ArrayList<>();
		String cc = inputValues[1];
		
		Map<String, String> locIdAndType = new HashMap<>();
		Map<String, ArrayList<String>> locIdAndTn = new HashMap<>();
		
		for (String tnId : tnstr) {
			request.getPayload().stream().forEach(spec -> {
				if ("ListofTNs".equalsIgnoreCase(spec.getSpecCode())) {
					spec.setSpecValue(cc.concat(tnId));
				}
			});
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
			webServicesImplUtil2.setMapLocIdAndTn(externalInterfaceResponse,locIdAndType,locIdAndTn,tnId);
			log.info("ExternalInterfaceResponse: === " + externalInterfaceResponse);
			webServicesImplUtil.checkNoRecordsFoundTn(locAndTnNotInEsap ,externalInterfaceResponse, tnId);
		}
		String location_id = "", location_type = "";
		ArrayList<String> tnList = null;
		
		for(String locId: locIdAndType.keySet()) {
			List<GenericTableData> tableData = new ArrayList<>();
			location_id = locId;
			location_type = locIdAndType.get(location_id);
			location_type = ("standard".equalsIgnoreCase(location_type)) ? "SINGLE" :location_type;
			tnList = locIdAndTn.get(location_id);
			GenericTableData data = new GenericTableData();
			data.setColumnName("location_id");
			data.setColumnValue(location_id);
			GenericTableData data1 = new GenericTableData();
			data1.setColumnName("location_type");
			data1.setColumnValue(location_type);
			GenericTableData data2 = new GenericTableData();
			data2.setColumnName("tn");
			data2.setColumnValue(tnList.stream().collect(
		            Collectors.joining(",")));
			tableData.add(data);
			tableData.add(data1);
			tableData.add(data2);
			v.add(tableData);
		}
		
		List<GenericDataBaseResponseDTO> dbResponse =null;
		if(externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null) {
			dbResponse = objectMapper.readValue(
				externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>(){});
		
		dbResponse.clear(); 
		GenericDataBaseResponseDTO d = new GenericDataBaseResponseDTO(); 
		
		d.setDataList(v);
		dbResponse.add(d);
		externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
		
		String str = locAndTnNotInEsap.stream().collect(
	            Collectors.joining(","));
		
		details.setDiallingCountryCode(inputValues[1]);
		details.setNoRecordFoundTNs(str);
		details.setTransactionId(inputValues[2]);
		webServicesImplUtil2.setCommonErrorDetails("0", WebServicesErrorCodes.SUCCESS.errorDesc, details, externalInterfaceResponse);
		}
		}
		log.info("Exited  getLocationDetailsByTNwithCC, response::"
				+ objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	
	public void getTNsStatus(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonMappingException, JsonProcessingException {
		
		log.info("Inside  getTNsStatus, request::" + objectMapper.writeValueAsString(request));
		
		Status status = new Status();
		List<String> workOrderIds = webServicesImplUtil.extractWorkOrderIdValuesOfGetTNsDetails(request);
		
		if (workOrderIds.isEmpty()) {
			webServicesImplUtil2.setCommonErrorStatus(WebServicesErrorCodes.MISSING_WORK_ORDER_ID.errorCode, WebServicesErrorCodes.MISSING_WORK_ORDER_ID.errorDesc, status, externalInterfaceResponse);
		} else {
			
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
			List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(
					externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {
					});
			List<GenericTableData> tableData = dbResponse.get(0).getDataList().get(0);
			if(tableData.isEmpty()) {
				webServicesImplUtil2.setCommonErrorStatus(WebServicesErrorCodes.MISSING_WORK_ORDER_ID.errorCode, WebServicesErrorCodes.MISSING_WORK_ORDER_ID.errorDesc, status, externalInterfaceResponse);
				
				return;
			}
				String orderStatus = "";
				String entityTypeName ="";
				for(GenericTableData data: tableData) {
					if("order_status".equalsIgnoreCase(data.getColumnName())) {
						orderStatus = data.getColumnValue();
					}
					if("entity_type_name".equalsIgnoreCase(data.getColumnName())) {
						entityTypeName = data.getColumnValue();
					}
				}
				if("WO_FAILED".equalsIgnoreCase(orderStatus)) {
					GenericTableData statusCodeData = new GenericTableData();
					statusCodeData.setColumnName("StatusCode");
					statusCodeData.setColumnValue(WebServicesErrorCodes.FAILED_WORK_ORDER_STATUS.errorCode);
					tableData.add(statusCodeData);
					GenericTableData statusTextData = new GenericTableData();
					statusTextData.setColumnName("StatusText");
					statusTextData.setColumnValue(WebServicesErrorCodes.FAILED_WORK_ORDER_STATUS.errorDesc);
					tableData.add(statusTextData);
					dbResponse.get(0).getDataList().clear();
					dbResponse.get(0).getDataList().add(tableData);
					externalInterfaceResponse.setRawBusinessResponse(dbResponse);
					externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
					return;
				}
				
				Set<String> entityTypes = new HashSet<>();
				entityTypes.add("PBX_TN");
				entityTypes.add("KEY_TN");
				entityTypes.add("ET_TN");
				entityTypes.add("RES_TN");
				entityTypes.add("SUBSCRIBER");

				if(!entityTypes.contains(entityTypeName)) {
					GenericTableData statusCodeData = new GenericTableData();
					statusCodeData.setColumnName("StatusCode");
					statusCodeData.setColumnValue(WebServicesErrorCodes.FAILED_WORK_ENTITY_TYPE.errorCode);
					tableData.add(statusCodeData);
					GenericTableData statusTextData = new GenericTableData();
					statusTextData.setColumnName("StatusText");
					statusTextData.setColumnValue(WebServicesErrorCodes.FAILED_WORK_ENTITY_TYPE.errorDesc);
					tableData.add(statusTextData);
					dbResponse.get(0).getDataList().clear();
					dbResponse.get(0).getDataList().add(tableData);
					externalInterfaceResponse.setRawBusinessResponse(dbResponse);
					externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
					return;
				}
				Set<String> accountNumbers = getTnForTNsStatus(task, request, taskList, externalInterfaceResponse);
				for(String account: accountNumbers) {
					GenericTableData tnData = new GenericTableData();
					tnData.setColumnName("tn");
					tnData.setColumnValue(account);
					tableData.add(tnData);
					GenericTableData statusData = new GenericTableData();
					statusData.setColumnName("status");
					statusData.setColumnValue(getStatusTypeForTns(account, task, request, taskList, externalInterfaceResponse));
					tableData.add(statusData);
				}
				GenericTableData transactionData = new GenericTableData();
				transactionData.setColumnName(request.getHeader().getSpecifications().get(0).getSpecCode());
				transactionData.setColumnValue(request.getHeader().getSpecifications().get(0).getSpecValue().toString());
				tableData.add(transactionData);
				webServicesImplUtil2.setStatusCodeAndTextForSuccess(tableData);
				dbResponse.get(0).getDataList().clear();
				dbResponse.get(0).getDataList().add(tableData);
				externalInterfaceResponse.setRawBusinessResponse(dbResponse);
				externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
		}
	}

	
	
	public Set<String> getTnForTNsStatus(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonMappingException, JsonProcessingException {
		
		log.info("Inside  getAccountNumberForTNS, request::" + objectMapper.writeValueAsString(request));
		task.setTaskName("getTNsStatusForTn");
		
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
				externalInterfaceResponse);
		List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(
				externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {
				});
		List<GenericDataBaseResponseDTO> dbResponseForTN = dbResponse.stream()
				.filter(e -> "getTNsStatusForTn".equalsIgnoreCase(e.getSqlKey())).collect(Collectors.toList());
		List<GenericTableData> tableData = dbResponseForTN.get(0).getDataList().get(0);
		Set<String> accountNumbers = new HashSet<>();
		tableData.forEach(data -> {
			if(("tn").equalsIgnoreCase(data.getColumnName())) accountNumbers.add((String) data.getColumnValue());
		});
		return accountNumbers;
	}
	
	private String getStatusTypeForTns(String account, Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonMappingException, JsonProcessingException {
		
		log.info("Inside  getAccountNumberForTNS, request::" + objectMapper.writeValueAsString(request));
		
		task.setTaskName("getTNsStatusForTnType");
		Specification accountNumberSpec = new Specification();
		accountNumberSpec.setSpecCode("TN");
		accountNumberSpec.setSpecValue(account);
		request.getPayload().add(accountNumberSpec);
		
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
				externalInterfaceResponse);
		List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(
				externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {
				});
		List<GenericDataBaseResponseDTO> dbResponseForTNType = dbResponse.stream()
				.filter(e -> "getTNsStatusForTnType".equalsIgnoreCase(e.getSqlKey())).collect(Collectors.toList());
		if(dbResponseForTNType.get(0).getDataList().isEmpty()) {
			return "";
		}
		List<GenericTableData> tableData = dbResponseForTNType.get(0).getDataList().get(0);
		List<String> status = new ArrayList<>();
		tableData.forEach(data -> {
			if(("status").equalsIgnoreCase(data.getColumnName())) status.add((String) data.getColumnValue());
		});
		
		return status.isEmpty()? "" : status.get(0);
	}
	
	public void getSBCSignallingNonTSO(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {

		Map<String, String> validationList = webServicesImplUtil.createValidationMap(request);
		log.info("Inside  getSBCSignallingNonTSO, request::" + objectMapper.writeValueAsString(request));
		LocationDetailsByTNwithCC details = new LocationDetailsByTNwithCC();
		request.getHeader().getSpecifications().stream().forEach(spec -> {
			if ("TransactionID".equalsIgnoreCase(spec.getSpecCode())) {
				details.setTransactionId(spec.getSpecValue().toString());
			}
		});
		if (validationList.get("LocationId") == null) {
			webServicesImplUtil2.setCommonErrorDetails(WebServicesErrorCodes.MISSING_LOC_ID.errorCode, WebServicesErrorCodes.MISSING_LOC_ID.errorDesc, details, externalInterfaceResponse);
		} else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
			if(externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null)
				setDnsValues(externalInterfaceResponse, request, task,taskList);	
		}
		externalInterfaceResponse.setRawBusinessResponse(details);
		log.info("Exited  getSBCSignallingNonTSO, response::"
				+ objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getSBCSignallingTSO(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {

		Map<String, String> validationList = webServicesImplUtil.createValidationMap(request);
		log.info("Inside  getSBCSignallingTSO, request::" + objectMapper.writeValueAsString(request));
		LocationDetailsByTNwithCC details = new LocationDetailsByTNwithCC();
		request.getHeader().getSpecifications().stream().forEach(spec -> {
			if ("TransactionID".equalsIgnoreCase(spec.getSpecCode())) {
				details.setTransactionId(spec.getSpecValue().toString());
			}
		});
		if (validationList.get("EntepriseId") == null) { 
			webServicesImplUtil2.setCommonErrorDetails(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorCode, WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorDesc, details, externalInterfaceResponse);
		} else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
			
			if(externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null)
				setDnsValues(externalInterfaceResponse, request, task,taskList);	
		}
		externalInterfaceResponse.setRawBusinessResponse(details);
		log.info("Exited  getSBCSignallingTSO, response::"
				+ objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void domainQryRequest(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		log.info("Inside getDomainQuery, request::" + objectMapper.writeValueAsString(request));
		Status status = new Status();
				
		List<Specification> specList =webServicesImplUtil2.setCommonSpecs(request);
		Header header = new Header("DomainQryRequest", "IPRO", specList);
		request.setHeader(header);
		Map<String, String> headerList = webServicesImplUtil.createValidationMap(request);
		if (StringUtils.isEmpty(headerList.get("SipDomainName"))) {
			webServicesImplUtil2.setCommonErrorStatus(WebServicesErrorCodes.NO_DATA_FOUND_TN.errorCode, "SipDomainName Not Found in ESAP DB", status, externalInterfaceResponse);
		} else {
			externalInterfaceResponse.setHeader(header);
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
			log.info("Exited eTEBIDetailsSummaryRequest, response::"
					+ objectMapper.writeValueAsString(externalInterfaceResponse));
		}
	}
	
	public void setDnsValues(ExternalInterfaceResponse externalInterfaceResponse,
			ExternalInterfaceRequest request, Task task, List<Task> taskList)
			throws JsonMappingException, JsonProcessingException {

			List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(
					externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {
					});
			Map<String, String> ipAddress = new HashMap<>();
			List<List<GenericTableData>> dataList = new ArrayList<>();
			for (List<GenericTableData> data : dbResponse.get(0).getDataList()) {
				for(GenericTableData d : data) {
					if (("state").equalsIgnoreCase(d.getColumnName())) {
						callIpServiceMethod(request, task, taskList, ipAddress, d.getColumnValue());
						break;
					}
				} 
				GenericTableData primaryAddress = new GenericTableData();
				primaryAddress.setColumnName("primary_address");
				primaryAddress.setColumnValue(ipAddress.get("Primary"));
				GenericTableData secondaryAddress = new GenericTableData();
				secondaryAddress.setColumnName("secondary_address");
				secondaryAddress.setColumnValue(ipAddress.get("Secondary"));
				data.add(primaryAddress);
				data.add(secondaryAddress);
				dataList.add(data);
			}
			dbResponse.get(0).getDataList().clear();
			dbResponse.get(0).getDataList().addAll(dataList);
			externalInterfaceResponse.setRawResponse(objectMapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(dbResponse));
	}

	public void callIpServiceMethod(ExternalInterfaceRequest request, Task task, List<Task> taskList, 
			Map<String, String> ipAddress, String value) throws JsonMappingException, JsonProcessingException {
		
		Specification spec = new Specification();
		spec.setSpecCode("state"); 
		spec.setSpecValue(value);
		request.getPayload().add(spec);
		task.setTaskName("getDnsInfo");
		ExternalInterfaceResponse resp = new ExternalInterfaceResponse();
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, resp);
		if(resp != null && resp.getRawResponse() != null)
			getIpAddress(resp,task,request,taskList,ipAddress);
		
	}

	public void getIpAddress(ExternalInterfaceResponse resp, Task task, ExternalInterfaceRequest request,
			List<Task> taskList, Map<String, String> ipAddress) throws JsonMappingException, JsonProcessingException {

		List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(resp.getRawResponse(),
				new TypeReference<List<GenericDataBaseResponseDTO>>() {
				});
		if (dbResponse != null && dbResponse.get(0) != null && dbResponse.get(0).getDataList() != null
				&& dbResponse.get(0).getDataList().size() >0) {
			List<GenericTableData> data = dbResponse.get(0).getDataList().get(0);
			String primaryDns = "", secondaryDns = "";
			for (GenericTableData tabledata : data) {
				if (("primary_dns").equalsIgnoreCase(tabledata.getColumnName()))
					primaryDns = tabledata.getColumnValue();
				else if (("secondary_dns").equalsIgnoreCase(tabledata.getColumnName()))
					secondaryDns = tabledata.getColumnValue();
			}
			Specification primary = new Specification();
			primary.setSpecCode("primary");
			primary.setSpecValue(primaryDns);
			Specification secondary = new Specification();
			secondary.setSpecCode("secondary");
			secondary.setSpecValue(secondaryDns);
			request.getPayload().add(primary);
			request.getPayload().add(secondary);
			task.setTaskName("getIpAddress");
			ExternalInterfaceResponse response = new ExternalInterfaceResponse();
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, response);
			webServicesImplUtil2.getAddressInfo(response, ipAddress);
		}
		else {
			ipAddress.put("Primary", "");
			ipAddress.put("Secondary", "");
		}
	}
	
	public void getLDOnlyTSOMigration(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		
		log.info("Inside  getSBCSignallingTSO, request::" + objectMapper.writeValueAsString(request));
		Map<String, String> validationList = webServicesImplUtil.createValidationMap(request);
		LocationDetailsByTNwithCC details = new LocationDetailsByTNwithCC();
		request.getHeader().getSpecifications().stream().forEach(spec -> {
			if ("TransactionID".equalsIgnoreCase(spec.getSpecCode())) {
				details.setTransactionId(spec.getSpecValue().toString());
			}
		});
		if (validationList.get("vamp_ref_id") == null) {
			
			webServicesImplUtil2.setCommonErrorDetails(WebServicesErrorCodes.NO_DATA_FOUND_TN.errorCode,WebServicesErrorCodes.NO_DATA_FOUND_TN.errorDesc,details, externalInterfaceResponse);
			
		} else {
			externalInterfaceResponse.setRawBusinessResponse(details);
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
			List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(
					externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {});
			
			String locationId = webServicesImplUtil2.getLocationIdRequest(dbResponse);
			Specification locId =  new Specification();
			locId.setSpecCode("location_id");
			locId.setSpecValue(locationId);
			request.getPayload().add(locId);
			
			List<GenericTableData> tableData = dbResponse.get(0).getDataList().get(0);
			task.setTaskName("getLDOnlyDeviceDetails");
			ExternalInterfaceResponse externalInterfaceResponseHub = new ExternalInterfaceResponse();
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponseHub);
			String stnDevice = webServicesImplUtil2.getStnDevice(externalInterfaceResponseHub);
			
			dbResponse.get(0).getDataList().clear();
			dbResponse.get(0).getDataList().add(tableData);
			externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
			details.setOtherLocationTns(stnDevice);
			webServicesImplUtil2.setCommonErrorDetails(WebServicesErrorCodes.SUCCESS.errorCode, WebServicesErrorCodes.SUCCESS.errorDesc, details, externalInterfaceResponse);
		}
		
		log.info("Inside  getSBCSignallingTSO, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}

	public void getVAMPDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		log.info("Inside  getVAMPDetails, request::" + objectMapper.writeValueAsString(request));
		
		List<String> inputRequestValues = webServicesImplUtil.extractTsoLocationIdValues(request);

		if (inputRequestValues.isEmpty()) {
			List<GenericTableData> tableData = new ArrayList<>();
			GenericTableData statusCode=new GenericTableData();
			statusCode.setColumnValue(WebServicesErrorCodes.MISSING_TSO_LOCATION_ID.errorCode);
			statusCode.setColumnName("StatusCode");
			tableData.add(statusCode);
			GenericTableData statusText=new GenericTableData();
			statusText.setColumnValue(WebServicesErrorCodes.MISSING_TSO_LOCATION_ID.errorDesc);
			statusText.setColumnName("StatusText");
			tableData.add(statusText);
			GenericTableData transactionData = new GenericTableData();
			transactionData.setColumnName(request.getHeader().getSpecifications().get(0).getSpecCode());
			transactionData.setColumnValue(request.getHeader().getSpecifications().get(0).getSpecValue().toString());
			tableData.add(transactionData);
			
			List<GenericDataBaseResponseDTO> dbResponse = new ArrayList<>();
			GenericDataBaseResponseDTO db = new GenericDataBaseResponseDTO();
			List<List<GenericTableData>> tableDataList = new ArrayList<>();
			tableDataList.add(tableData);
			db.setDataList(tableDataList);
			dbResponse.add(db);
			db.setNumberOfRecords(0);
			externalInterfaceResponse.setRawBusinessResponse(dbResponse);
			externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
			return;
		} else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
			List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(
					externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {});
			
			if(dbResponse.get(0).getDataList().isEmpty() || dbResponse.get(0).getDataList().get(0).isEmpty()) {
				List<GenericTableData> tableData = new ArrayList<>();
				GenericTableData statusCode=new GenericTableData();
				statusCode.setColumnValue(WebServicesErrorCodes.INVALID_TSO_LOCATION_ID.errorCode);
				statusCode.setColumnName("StatusCode");
				tableData.add(statusCode);
				GenericTableData statusText=new GenericTableData();
				statusText.setColumnValue(WebServicesErrorCodes.INVALID_TSO_LOCATION_ID.errorDesc);
				statusText.setColumnName("StatusText");
				tableData.add(statusText);
				GenericTableData transactionData = new GenericTableData();
				transactionData.setColumnName(request.getHeader().getSpecifications().get(0).getSpecCode());
				transactionData.setColumnValue(request.getHeader().getSpecifications().get(0).getSpecValue().toString());
				tableData.add(transactionData);
				
				dbResponse.get(0).getDataList().clear();
				dbResponse.get(0).getDataList().add(tableData);
				externalInterfaceResponse.setRawBusinessResponse(dbResponse);
				externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
				return;
			}
			
			List<GenericTableData> tableData = dbResponse.get(0).getDataList().get(0);
			
			String newLocationId = null;
			String oldLocationId = null;
			
			tableData.forEach(e -> {
				if("".equals(e.getColumnValue())){
					e.setColumnValue(null);
				}
			});
			
			for(GenericTableData data: tableData) {
				if("newLocationId".equalsIgnoreCase(data.getColumnName())) {
					newLocationId =data.getColumnValue();
				} else if("oldLocationId".equalsIgnoreCase(data.getColumnName())) {
					oldLocationId =data.getColumnValue();
				}
			}
			
			List<GenericTableData> locTableData = getLocationDetails(task, request, taskList, externalInterfaceResponse, newLocationId);
			tableData.addAll(locTableData);
			Map<String, String> aniEnabledMap = getAniEnabledForLocation(task, request, taskList, externalInterfaceResponse, oldLocationId);
			
			GenericTableData transactionData = new GenericTableData();
			transactionData.setColumnName(request.getHeader().getSpecifications().get(0).getSpecCode());
			transactionData.setColumnValue(request.getHeader().getSpecifications().get(0).getSpecValue().toString());
			tableData.add(transactionData);
			
			GenericTableData aniData = new GenericTableData();
			aniData.setColumnName("ANIEnabled");
			aniData.setColumnValue(aniEnabledMap.get("anienabled"));
			tableData.add(aniData);
			
			GenericTableData serviceLevelData = new GenericTableData();
			serviceLevelData.setColumnName("ServiceLevel");
			serviceLevelData.setColumnValue(aniEnabledMap.get("serviceLevel"));
			tableData.add(serviceLevelData);
			
			webServicesImplUtil2.setStatusCodeAndTextForSuccess(tableData);
			
			dbResponse.get(0).getDataList().clear();
			dbResponse.get(0).getDataList().add(tableData);
			externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
			externalInterfaceResponse.setRawBusinessResponse(dbResponse);

			log.info("Exited  getVampDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
			
		}
	}
	
	public List<GenericTableData> getLocationDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse, String locId) throws JsonProcessingException {
		log.info("Inside  getLocationDetails, request::" + objectMapper.writeValueAsString(request));

		task.setTaskName("getSTNDetails");
		Specification locSpec = new Specification();
		locSpec.setSpecCode("LocationId");
		locSpec.setSpecValue(locId);
		request.getPayload().add(locSpec);
		
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
				externalInterfaceResponse);
		List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(
				externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {});
		
		List<GenericTableData> tableData = dbResponse.stream()
				.filter(e -> "getSTNDetails".equalsIgnoreCase(e.getSqlKey()))
				.collect(Collectors.toList()).get(0)
				.getDataList().get(0);
		webServicesImplUtil2.
		setSTNValues(task, request, taskList, dbResponse, tableData);

		return tableData;
	}
	
	public Map<String, String> getAniEnabledForLocation(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse, String locId) throws JsonProcessingException {
		log.info("Inside  getAniEnabledForLocation, request::" + objectMapper.writeValueAsString(request));

		task.setTaskName("getVAMPDetailsForAniEnabledAndServiceLevel");
		Specification locSpec = new Specification();
		locSpec.setSpecCode("OldLocationId");
		locSpec.setSpecValue(locId);
		request.getPayload().add(locSpec);
		
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
				externalInterfaceResponse);
		List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(
				externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {});
		
		List<GenericTableData> tableData = dbResponse.stream()
				.filter(e -> "getVAMPDetailsForAniEnabledAndServiceLevel".equalsIgnoreCase(e.getSqlKey()))
				.collect(Collectors.toList()).get(0)
				.getDataList().get(0);
		String aniEnabled = webServicesImplUtil.extractAniEnabledOfLocationDetails(tableData);
		String serviceLevel = webServicesImplUtil.extractServiceLevelOfLocationDetails(tableData);

		Map<String, String> map = new HashMap<>();
		map.put("serviceLevel", serviceLevel);
		map.put("anienabled", "1".equals(aniEnabled) ? "Y" : "N");
		return map;
	}
	
	public void sTNVAMPRequest(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		log.info("Inside sTNVAMPRequest, request::" + objectMapper.writeValueAsString(request));
		Status status = new Status();
		List<Specification> specList = webServicesImplUtil2.setCommonSpecs(request);
		Header header = new Header("STNVAMPRequest", "IPRO", specList);
		request.setHeader(header);
		Map<String, String> headerList = webServicesImplUtil.createValidationMap(request);
		if (StringUtils.isEmpty(headerList.get("TSOLocationId"))) {
			webServicesImplUtil2.setCommonErrorStatus(WebServicesErrorCodes.MISSING_LOC_ID.errorCode, WebServicesErrorCodes.MISSING_LOC_ID.errorDesc, status, externalInterfaceResponse);
		} else {
			externalInterfaceResponse.setHeader(header);
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
			log.info("Exited STNVAMPRequest, response::"
					+ objectMapper.writeValueAsString(externalInterfaceResponse));
		}
	}
	public void getLocationDetailsBySegment(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {

		Map<String, String> validationList = webServicesImplUtil.createValidationMap(request);

		LocationDetailsByTNwithCC details = new LocationDetailsByTNwithCC();
		if (validationList.get("ListOfTNs") == null) {
			webServicesImplUtil2.setCommonErrorDetails(WebServicesErrorCodes.MISSING_TN.errorCode, WebServicesErrorCodes.MISSING_TN.errorDesc, details, externalInterfaceResponse);
		} else if (validationList.get("LastSegment") == null) {
			webServicesImplUtil2.setCommonErrorDetails(WebServicesErrorCodes.MISSING_ENTERPRISE_TRUNK_ID.errorCode, 
					WebServicesErrorCodes.MISSING_ENTERPRISE_TRUNK_ID.errorDesc, details, externalInterfaceResponse);
		} else {
			log.info("json request:==="+request.getPayload());
			task.setOriginatingSystem("IPRO");
			String[] inputValues = webServicesImplUtil.getInputValuesLocationDetailsSegment(request);
			List<String> locAndTnNotInEsap = new ArrayList<>();
			String[] tnstr = inputValues[0].replace("\r", "").replace("\n", "").replace("\t", "").split(",");
			Map<String, String> locTypeDtl = new HashMap<>();
			Map<String, ArrayList<String>> locAndTninEsap = new HashMap<>();
			details.setTransactionId(inputValues[4]);
			boolean isSingleSTN = false;
			int tnLength = tnstr.length;
			Map<String, ArrayList<String>> otherLocTns = new HashMap<>();
			String locationId = inputValues[2];
			String segment = inputValues[3];
			details.setDiallingCountryCode(inputValues[1]);
			String cc = inputValues[1];
			isSingleSTN = webServicesImplUtil2.isSingleSTN(task, request, taskList, isSingleSTN, tnLength);
			webServicesImplUtil2.commonGetLocationDetailsbySegmentESAP(task, request, taskList, inputValues, locAndTnNotInEsap, 
					locTypeDtl, locAndTninEsap);
			webServicesImplUtil2.commonSetOtherLocations(task, request, taskList, inputValues, locAndTnNotInEsap, otherLocTns, cc);

			webServicesImplUtil2.commonRemoveTn(task, request, taskList, locAndTnNotInEsap);

			String str = locAndTnNotInEsap.stream().collect(Collectors.joining(","));

			details.setNoRecordFoundTNs(str);
			if (segment != null && "Y".equalsIgnoreCase(segment)) {

				webServicesImplUtil2.commonSetLocationSegmentDetails( details, locAndTnNotInEsap,locAndTninEsap);
				webServicesImplUtil2.getLocationSegmentDetails(locAndTninEsap, isSingleSTN, externalInterfaceResponse, task, taskList,
						locTypeDtl, locationId);
				webServicesImplUtil2.setRemoteLocationIds(task, request, taskList, externalInterfaceResponse, locationId);
				webServicesImplUtil2.setVmaTns(task, request, taskList, externalInterfaceResponse, locationId);
				details.setStatusCode("0");
				details.setStatusDescription("SUCCESS");
				webServicesImplUtil2.setOtherFormattedTns(details, otherLocTns);
				
				}
		}
		log.info("Exited  getLocationDetailsSegment, response::"
				+ objectMapper.writeValueAsString(externalInterfaceResponse));

	}
	
	}
	
