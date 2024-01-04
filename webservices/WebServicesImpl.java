package com.verizon.connect.infra.interfaceservice.external.interfaces.webservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.JsonPath;
import com.verizon.connect.infra.interfaceservice.external.interfaces.constants.RulesConstants;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.ExternalInterfaceRequest;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.ExternalInterfaceResponse;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.Header;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.LocationDetailsByTNwithCC;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.Specification;
import com.verizon.connect.infra.interfaceservice.external.interfaces.taskexecution.RestDbOperationServiceConnector;
import com.verizon.connect.infra.interfaceservice.external.interfaces.taskexecution.Task;
import com.verizon.connect.infra.interfaceservice.model.GetLocationListRequest;
import com.verizon.connect.infra.interfaceservice.model.RegionMapping;
import com.verizon.connect.infra.interfaceservice.model.TNPoolSummeryRequest;
import com.verizon.connect.infra.interfaceservice.model.om.Status;
import com.verizon.infrastructure.connect.genericdatabase.dto.GenericDataBaseResponseDTO;
import com.verizon.infrastructure.connect.genericdatabase.dto.GenericTableData;
import com.verizon.infrastructure.connect.genericdatabase.dto.PreparedQueryV3;
import com.verizon.connect.infra.interfaceservice.model.GetEnterpriseSBCDeviceListRequest;
import com.verizon.connect.infra.interfaceservice.model.GetLocationSBCDeviceListRequest;


import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebServicesImpl {
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	RestDbOperationServiceConnector restDbOperationServiceConnector;
	
	@Autowired
	WebServicesImplUtil webServicesImplUtil;
	
	@Autowired
	WebServicesImplUtil2 webServicesImplUtil2;
	
	private String transactionID="TransactionID";

	public void getCustomerDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		log.info("Inside  getCustomerDetails, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  eTEBIDetailsSummaryRequest, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getLocationDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getLocationDetails, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  eTEBIDetailsSummaryRequest, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}

	public void eTEBIDetailsSummaryRequest(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  eTEBIDetailsSummaryRequest, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		String[] inputRequestValues=webServicesImplUtil.extractInputValuesOfGetETEBIDetailsRequest(request); //all input request values except enterpriseTrunkId
		List<String> enterpriseTrunkIdValues=webServicesImplUtil.extractEnterpriseTrunkIdsOfGetETEBIDetailsRequest(request);
		if (inputRequestValues[0] == null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}

		else {
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil.extractEbiDetailsPreparedQueryList(inputRequestValues,
					enterpriseTrunkIdValues);
			
			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponse, preparedQueryList);
		}
		log.info("Exited  eTEBIDetailsSummaryRequest, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getCustomerAuthorizationDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getCustomerAuthorizationDetails, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  getCustomerAuthorizationDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getLocationFeaturePackageDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getLocationFeaturePackageDetails, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  getLocationFeaturePackageDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getDeviceTypeDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getDeviceTypeDetails, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  getDeviceTypeDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	
	public void getEnterpriseTrunkDetailsSummary(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getEnterpriseTrunkDetailsSummary, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		Map<String,String> validationList =  webServicesImplUtil.createValidationMap(request);
		if(validationList.get("EnterpriseId")==null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else if(validationList.get("EnterpriseTrunkId")==null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_TRUNK_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_TRUNK_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}
		
		log.info("Exited  getEnterpriseTrunkDetailsSummary, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	
	
	public void getTNPoolSummaryCount(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getTNPoolSummaryCount, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		Map<String,String> headerList =  webServicesImplUtil.createHeaderMap(request);
		webServicesImplUtil.setSummaryContextId(externalInterfaceResponse,headerList);
		log.info("Exited  getTNPoolSummaryCount, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getKeyLineCount(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException{
		log.info("Inside  getKeyLineCount, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		Map<String,String> validationList =  webServicesImplUtil.createValidationMap(request);
		if(validationList.get("KeyID")==null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_KEY_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_KEY_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}
		
		log.info("Exited  getKeyLineCount, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void eBIListSummaryRequest(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  eBIListSummaryRequest, request::" + objectMapper.writeValueAsString(request));
		Map<String,String> validationList =  webServicesImplUtil.createValidationMap(request);
		Status status=new Status();
		if(validationList.get("Offset")==null || validationList.get("Size")==null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_OFFSET_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_OFFSET_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else if(validationList.get("EnterpriseId")==null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}
		log.info("Exited  eBIListSummaryRequest, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	public void eBCListSummaryRequest(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{

log.info("Inside  eBCListSummaryRequest, request::" + objectMapper.writeValueAsString(request));
		Map<String,String> validationList =  webServicesImplUtil.createValidationMap(request);
		Status status=new Status();
		if(validationList.get("Offset")==null || validationList.get("Size")==null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_OFFSET_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_OFFSET_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else if(validationList.get("EnterpriseId")==null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}
		log.info("Exited  eBCListSummaryRequest, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	public void getECNDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getECNDetails, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		String[] inputRequestValues=webServicesImplUtil.extractInputValuesOfGetECNDetails(request); //all input request values except enterpriseTrunkId
		if (inputRequestValues[0] == null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}

		else {
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil.extractGetECNDetailsPreparedQueryList(inputRequestValues);
			
			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponse, preparedQueryList);
		log.info("Exited  getECNDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	   }

    }
	
	public void getPrefixPlanDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getPrefixPlanDetails, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  getPrefixPlanDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getCallingPlanDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getCallingPlanDetails, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  getCallingPlanDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getEnterpriseDigitStrings(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getEnterpriseDigitStrings, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  getEnterpriseDigitStrings, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getEnterpriseTrunkLineCount(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getEnterpriseTrunkLineCount, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  getEnterpriseTrunkLineCount, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}

	public void enterpriseTrunkTNRequest(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  enterpriseTrunkTNRequest, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		task.setOriginatingSystem(request.getHeader().getOriginatingSystem());
		String[] inputRequestValues=webServicesImplUtil.extractInputValuesOfEnterpriseTrunkLinesRequest(request); 
		boolean iserpriseTrunkLinesNull=(inputRequestValues[11] == null || inputRequestValues[12] == null || inputRequestValues[13] == null);
		if (inputRequestValues[0] == null || inputRequestValues[1] == null || inputRequestValues[2] == null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else if (iserpriseTrunkLinesNull || inputRequestValues[14] == null || inputRequestValues[15] == null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_OFFSET_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_OFFSET_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}

		else {
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil.extractenterpriseTrunkLinesPreparedQueryList(inputRequestValues);
			
 			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponse, preparedQueryList);
		}
		log.info("Exited  enterpriseTrunkTNRequest, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
		
	}

	public void enterpriseTrunkLinesRequest(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  enterpriseTrunkLinesRequest, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		task.setOriginatingSystem(request.getHeader().getOriginatingSystem());
		String[] inputRequestValues=webServicesImplUtil.extractInputValuesOfEnterpriseTrunkLinesRequest(request);
		boolean iserpriseTrunkLinesNull=(inputRequestValues[11] == null || inputRequestValues[12] == null || inputRequestValues[13] == null);
		if (inputRequestValues[0] == null || inputRequestValues[1] == null || inputRequestValues[2] == null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else if (iserpriseTrunkLinesNull || inputRequestValues[14] == null || inputRequestValues[15] == null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_OFFSET_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_OFFSET_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}

		else {
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil.extractenterpriseTrunkLinesPreparedQueryList(inputRequestValues);
			
 			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponse, preparedQueryList);
		}
		log.info("Exited  enterpriseTrunkLinesRequest, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getLinesForPBX(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  GetLinesForPBX, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		task.setOriginatingSystem(request.getHeader().getOriginatingSystem());
		String[] inputRequestValues=webServicesImplUtil.extractInputValuesOfGetLinesForPBX(request);
		boolean isGetLinesForPBXNull=(inputRequestValues[4] == null || inputRequestValues[7] == null || inputRequestValues[8] == null);
		if (inputRequestValues[0] == null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_PBX_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_PBX_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else if (isGetLinesForPBXNull || inputRequestValues[5] == null || inputRequestValues[6] == null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_OFFSET_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_OFFSET_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}

		else {
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil.extractGetLinesForPBXPreparedQueryList(inputRequestValues);
			
 			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponse, preparedQueryList);
		}
		log.info("Exited  enterpriseTrunkLinesRequest, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}

	
	public void getPBXDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getPBXDetails, request::" + objectMapper.writeValueAsString(request));
		Map<String,String> validationList =  webServicesImplUtil.createValidationMap(request);
		Status status=new Status();
		if(null == validationList.get("PBXID")) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_PBX_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_PBX_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else if (null!= validationList.get("PBXID")){
			
			String tr = validationList.get("PBXID");
	        boolean result = tr.matches("[0-9]+");
			if(!result) {
				status.setStatusCode(WebServicesErrorCodes.PBX_ID_VALIDATION.errorCode);
				status.setStatusDescription(WebServicesErrorCodes.PBX_ID_VALIDATION.errorDesc);
				externalInterfaceResponse.setRawBusinessResponse(status);
			}
			else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}
		}
		log.info("Exited  getPBXDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getDepartmentDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getDepartmentDetails, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  getDepartmentDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}

	public void getDeviceDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getDeviceDetails, request::" + objectMapper.writeValueAsString(request));
		Map<String,String> validationList =  webServicesImplUtil.createValidationMap(request);
		Status status=new Status();
		if((null == validationList.get("DeviceID")) && (null == validationList.get("DeviceType"))) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_OFFSET_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_OFFSET_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
			
		}
			else if ((null == validationList.get("DeviceID")) && (null != validationList.get("DeviceType"))){					
					status.setStatusCode(WebServicesErrorCodes.MISSING_DEVICE_ID.errorCode);
					status.setStatusDescription(WebServicesErrorCodes.MISSING_DEVICE_ID.errorDesc);
					externalInterfaceResponse.setRawBusinessResponse(status);
			}
			else {
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
			}
		log.info("Exited  getDeviceDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
			
	}

	public void getSubscriberDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getSubscriberDetails, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  getSubscriberDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getPBXLineCount(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getPBXLineCount, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		Map<String,String> validationList =  webServicesImplUtil.createValidationMap(request);
		if(validationList.get("PBXID")==null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_PBXID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_PBXID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}
		
		log.info("Exited  getPBXLineCount, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getRingingNumberSummary(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getRingingNumberSummary, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		task.setOriginatingSystem(request.getHeader().getOriginatingSystem());
		String[] inputRequestValues=webServicesImplUtil.extractInputValuesOfgetRingingNumberSummary(request);
		
		boolean isringingNumberNul=(inputRequestValues[0] == null || inputRequestValues[1] == null || inputRequestValues[2] == null);
		boolean isringingNumberNull=(inputRequestValues[4] == null || inputRequestValues[5] == null || inputRequestValues[6] == null);
		boolean isringingNumberNulll=(inputRequestValues[7] == null || inputRequestValues[8] == null);
		boolean isringingNumberNull1 =(isringingNumberNull && isringingNumberNulll);

		if (isringingNumberNul && isringingNumberNull && isringingNumberNulll) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_OFFSET_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_OFFSET_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else if (isringingNumberNul && inputRequestValues[3] == null && !isringingNumberNull1) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_RECORDS.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_RECORDS.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else if (isringingNumberNulll && !(isringingNumberNull || isringingNumberNul)) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_OFFSET.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_OFFSET.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}

		else {
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil.extractRingingNumberSummaryPreparedQueryList(inputRequestValues);
			
 			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponse, preparedQueryList);
		}
		log.info("Exited  getRingingNumberSummary, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getLinesForKey(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getLinesForKey, request::" + objectMapper.writeValueAsString(request));
		Map<String,String> validationList =  webServicesImplUtil.createValidationMap(request);
		Status status=new Status();
		if(validationList.get("Offset")==null || validationList.get("Size")==null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_OFFSET_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_OFFSET_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else if(validationList.get("KeyID")==null) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_KEY_GRP_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_KEY_GRP_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}
		log.info("Exited  getLinesForKey, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getSubscriberVMCount(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getSubscriberVMCount, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		Map<String,String> validationList =  webServicesImplUtil.createValidationMap(request);
		if(validationList.get("LocationID")==null) {
			log.info("getSubscriberVMCount, Missing Location ID ");
			status.setStatusCode(WebServicesErrorCodes.MISSING_LOCATION_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_LOCATION_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}
		
		log.info("Exited  getSubscriberVMCount, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void getSubscriberDetailsList(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getSubscriberDetails, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  getSubscriberDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	

	public void getSubscriberSummary(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getSubscriberSummary, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		task.setOriginatingSystem(request.getHeader().getOriginatingSystem());
		String[] inputRequestValues=webServicesImplUtil.extractInputValuesOfgetSubscriberSummary(request);
		
		boolean isringingNumberNulll=(inputRequestValues[13] == null || inputRequestValues[14] == null);

		if (isringingNumberNulll) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_OFFSET_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_OFFSET_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else {
			List<String> sortList =  webServicesImplUtil.extractsortListOfSubSummary(request);
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil.extractSubscriberSummaryPreparedQueryList(inputRequestValues,sortList);
 			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponse, preparedQueryList);
		}
		log.info("Exited  getSubscriberSummary, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	
	public void getTNDetails(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		log.info("Inside  getTNDetails, request::" + objectMapper.writeValueAsString(request));
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  getTNDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void searchTrunkGroup(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  searchTrunkGroup, request::" + objectMapper.writeValueAsString(request));
		Map<String,String> validationList =  webServicesImplUtil.createValidationMap(request);
		Status status=new Status();

		if (StringUtils.isEmpty(validationList.get("LocationID")) && StringUtils.isEmpty(validationList.get("TransactionID"))) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_TRUNK_LOCATION_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_TRUNK_LOCATION_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else {
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil.extractLocationID(validationList);		
			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponse, preparedQueryList);
			
					
			}
		log.info("Exited searchTrunkGroup, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));		  			  
		}
	
		public void getDeviceLinesSummary(Task task, ExternalInterfaceRequest request,
				List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException{
			log.info("Inside  getDeviceLinesSummary, request::" + objectMapper.writeValueAsString(request));
			Map<String,String> inpList =  webServicesImplUtil.createValidationMap(request);
			List<String> sortList =  webServicesImplUtil.extractsortListOfDLS(request);
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil.preparedQueryListforDLS(inpList,sortList);
			if("SUBSCRIBER".equalsIgnoreCase(inpList.get("ResourceType")))
				task.setTaskName("GetDeviceLinesSummarySubscriber");
			if("KEY".equalsIgnoreCase(inpList.get("ResourceType")))
				task.setTaskName("GetDeviceLinesSummaryKey");
			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
				externalInterfaceResponse, preparedQueryList);
			log.info("Exited  getDeviceLinesSummary, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
		}
	
		public void setBSCountryCodeMapping(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {

		log.info("Inside  setBSCountryCodeMapping, request::" + objectMapper.writeValueAsString(request));

		LocationDetailsByTNwithCC details = new LocationDetailsByTNwithCC();
		
		String[] inputValues = webServicesImplUtil2.inputValuesSetBsCountryCodeMapping(request);

		final String defaultIndicator = inputValues[6];

		task.setTaskName("getBSMappingCode");
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);

		Map<String, RegionMapping> regionMap = new HashMap<>();
		List<GenericDataBaseResponseDTO> dbResponse = null;
		if (externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null) {

			dbResponse = objectMapper.readValue(externalInterfaceResponse.getRawResponse(),
					new TypeReference<List<GenericDataBaseResponseDTO>>() {
					});

			List<List<GenericTableData>> table = dbResponse.get(0).getDataList();
			for (List<GenericTableData> data : table) {
				String taskname = "";
				RegionMapping jsonObject = null;
				webServicesImplUtil2.setValues(data,taskname,jsonObject,regionMap);
				
			}
			task.setTaskName("setBSMappingCode");
			for (Entry<String, RegionMapping> entry : regionMap.entrySet()) {

				RegionMapping regionMapping = entry.getValue();
				regionMapping.setDefaultIndicator(("true".equalsIgnoreCase(defaultIndicator) ? "Y" : "N"));

				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String json = ow.writeValueAsString(regionMapping);
				Specification task1 = new Specification();
				task1.setSpecCode("taskname");
				task1.setSpecValue(entry.getKey());
				request.getPayload().add(task1);
				Specification regionSpec = new Specification();
				regionSpec.setSpecCode("regionMapping");
				regionSpec.setSpecValue(json);
				request.getPayload().add(regionSpec);

				restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
						externalInterfaceResponse);
				details.setTransactionId(inputValues[0]);
				externalInterfaceResponse.setRawBusinessResponse(details);
			}
		}
		log.info("Exited  setBsCountryCodeMapping, response::"
				+ objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	
	public void searchCustomer(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		
		log.info("Inside searchCustomer, request::" + objectMapper.writeValueAsString(request));
		Map<String, String> validationList = webServicesImplUtil.createValidationMap(request);
		Status status=new Status();
		
		if(StringUtils.isEmpty(validationList.get("version")) && StringUtils.isEmpty(validationList.get("startsWith"))) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_CUSTOMERNAME.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_CUSTOMERNAME.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else {
			
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}
		log.info("Exited searchCustomer, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
		
	}
	
	public void getTNPoolSummary(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getTNPoolSummary, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		TNPoolSummeryRequest getTNPoolSummeryRequest = webServicesImplUtil2.extractInputValuesOfGetTNPoolSummeryRequest(request);
		if (StringUtils.isNotBlank(getTNPoolSummeryRequest.getLocationID()) && StringUtils.isNotBlank(getTNPoolSummeryRequest.getEnterpriseID())) {
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil.getTNPoolSummeryPreparedQuery(getTNPoolSummeryRequest);
			task.setOriginatingSystem("CSSOP");
			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList, externalInterfaceResponse, preparedQueryList);
			log.info("Exited  getTNPoolSummary, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
		}else if(StringUtils.isBlank(getTNPoolSummeryRequest.getLocationID())){
			status.setStatusCode(WebServicesErrorCodes.MISSING_LOCATION_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_LOCATION_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}else {
			status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
	   }

    }
	
	public void getCustomerList(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		
		log.info("Inside getCustomerList, request::" + objectMapper.writeValueAsString(request));
		Map<String, String> validationList = webServicesImplUtil.createValidationMap(request);
		Status status=new Status();
		
		if(StringUtils.isEmpty(validationList.get("EnterpriseName"))) {
			status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISENAME.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISENAME.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}
		log.info("Exited getCustomerList, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}
	/**********************************************************************************************************
	 * @param task parameter of method
	 * @param request parameter of method
	 * @param taskList parameter of method
	 * @param externalInterfaceResponse parameter of method
	 * @throws JsonProcessingException
	 * This method is used for fetching eSAPDeviceTypes details and manipulate the db response based on device_realtype_id
	 **********************************************************************************************************/
	public void eSAPDeviceTypesRequest(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		
		String deviceRealtypeId="device_realtype_id";
		log.info("Inside  eSAPDeviceTypesRequest, request::" + objectMapper.writeValueAsString(request));
		Map<String,String> validationList =  webServicesImplUtil.createValidationMap(request);
		Status status=new Status();
		List<Specification>specList= new ArrayList<>();
		Specification spec= new Specification();
		spec.setSpecCode(request.getPayload().get(0).getSpecCode());
		spec.setSpecValue(request.getPayload().get(0).getSpecValue());
		specList.add(spec);
		Header header= new Header("ESAPDeviceTypesRequest","OPRO",specList);
		request.setHeader(header);
		if (StringUtils.isEmpty(validationList.get(transactionID))) {
			status.setStatusCode(WebServicesErrorCodes.ESAP_TRANSID_MISSING.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.ESAP_TRANSID_MISSING.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		else {
			List<PreparedQueryV3> preparedQueryList = null;
			
			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponse, preparedQueryList);
			List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(
					externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {
					});
			
			List<List<GenericTableData>> tableData = dbResponse.get(0).getDataList();
			int sizeTableData = tableData.size();
			for(int i=0;i<sizeTableData;i++) {
				List<GenericTableData> listTableData = tableData.get(i);
				listTableData.forEach(data->{
					if((deviceRealtypeId).equals(data.getColumnName())&&("0").equals(data.getColumnValue())) {
						data.setColumnValue("SIP_DEVICE");
					}
					else if((deviceRealtypeId).equals(data.getColumnName())&&(("1").equals(data.getColumnValue())||("2").equals(data.getColumnValue()))) {
						data.setColumnValue("ENTPRISE_GTWY");
					}
					else if((deviceRealtypeId).equals(data.getColumnName())&&("3").equals(data.getColumnValue())) {
						data.setColumnValue("CPE_LOCAL_GTWY");
					}
					else if((deviceRealtypeId).equals(data.getColumnName())&&(("4").equals(data.getColumnValue())||("5").equals(data.getColumnValue()))) {
						data.setColumnValue("CPE_ENTPRISE_GTWY_SHARED");
					}
				}
						);
			}
			dbResponse.get(0).getDataList().addAll(tableData);
			externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
			}
		log.info("Exited eSAPDeviceTypesRequest, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));		  			  
		}
	public void getGatewayListForEnterprise(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
			log.info("Inside getGatewayListForEnterprise, request::" + objectMapper.writeValueAsString(request));
			Map<String,String> validationList = webServicesImplUtil.createValidationMap(request);
			Status status = new Status();
			List<Specification>specList= new ArrayList<>();
			Specification spec= new Specification();
			spec.setSpecCode(request.getPayload().get(0).getSpecCode());
			spec.setSpecValue(request.getPayload().get(0).getSpecValue());
			specList.add(spec);
			Header header= new Header("getGatewayListForEnterprise","OPRO",specList);
			request.setHeader(header);
			if (StringUtils.isEmpty(validationList.get("TransactionID"))) {
			status.setStatusCode(WebServicesErrorCodes.ESAP_TRANSID_MISSING.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.ESAP_TRANSID_MISSING.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
			}
			else if (StringUtils.isEmpty(validationList.get("EnterpriseID"))){
				status.setStatusCode(WebServicesErrorCodes.ESAP_ENTID_MISSING.errorCode);
				status.setStatusDescription(WebServicesErrorCodes.ESAP_ENTID_MISSING.errorDesc);
				externalInterfaceResponse.setRawBusinessResponse(status);
			}
			else {
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil2.extractEnterpriseIdOfGetGatewayListForEnterpriseRequest(validationList);
				restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,externalInterfaceResponse,preparedQueryList);		
				log.info("Exited getGatewayListForEnterprise, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
			} 
	}
	
	public void getVAMPEntityDetails(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
		log.info("Entered getVAMPEntityDetails, response::"
				+ objectMapper.writeValueAsString(externalInterfaceResponse));
		Map<String, String> validationList = webServicesImplUtil.createValidationMap(request);
		LocationDetailsByTNwithCC details = new LocationDetailsByTNwithCC();
		if(!StringUtils.isEmpty(validationList.get("transactionId")))
			details.setTransactionId(validationList.get("transactionId"));
		if (StringUtils.isEmpty(validationList.get("vamp_ref_id"))) {
			details.setStatusCode(WebServicesErrorCodes.ESAP_INTERNAL_ERROR.errorCode);
			details.setStatusDescription(WebServicesErrorCodes.ESAP_INTERNAL_ERROR.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(details);
		}
		else {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
			externalInterfaceResponse.setRawBusinessResponse(details);
		}
		log.info(
				"Exited getVAMPEntityDetails, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));

	}

public void getLocationList(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  GetLocationList, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		GetLocationListRequest getLocationListRequest = webServicesImplUtil2.extractInputValuesOfGetLocationListRequest(request);
		if (StringUtils.isNotBlank(getLocationListRequest.getEnterpriseID())) {
			List<PreparedQueryV3> preparedQueryList = webServicesImplUtil.getLocationListPreparedQuery(getLocationListRequest);
			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList, externalInterfaceResponse, preparedQueryList);
			log.info("Exited  GetLocationList, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
		}else {
			status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
	   }
    }
	

	/**********************************************************************************************************
	 * @param task parameter of method
	 * @param request parameter of method
	 * @param taskList parameter of method
	 * @param externalInterfaceResponse parameter of method
	 * @throws JsonProcessingException
	 * This method is used for fetching Location SBC details From ITS source system
	 **********************************************************************************************************/
	public void getLocationSBCDetail(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
		log.info("Inside  getLocationSBCDetail, request::" + objectMapper.writeValueAsString(request));
		Status status=new Status();
		Map<String,String> validationList = webServicesImplUtil.createValidationMap(request);
		if (StringUtils.isNotBlank(validationList.get("locationID"))) {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
			log.info("Exited  getLocationSBCDetail, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
			setValuesForLocationSBCDetail(externalInterfaceResponse,task.getUrl());
		}else {
			status.setStatusCode(WebServicesErrorCodes.MISSING_LOCATION_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.MISSING_LOCATION_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
	   }

    }
	
private void setValuesForLocationSBCDetail(ExternalInterfaceResponse externalInterfaceResponse, String url) {
		
		if(externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null) {
		try {
			List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(
					externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>(){});
			GenericDataBaseResponseDTO table = dbResponse.get(dbResponse.size() - 1);
			List<List<GenericTableData>> tableCompleteList = new ArrayList<>();
			table.getDataList().forEach(tableData ->{
				Optional<GenericTableData> networkNodeDeviceId = tableData.stream()
						.filter(spec -> "NWK_NODE_DEVICE_ID".equalsIgnoreCase(spec.getColumnName())).findFirst();
				if (networkNodeDeviceId.isPresent()) {
					updateSBCMetadataInfoForLocation(tableData,networkNodeDeviceId.get().getColumnValue(),url);
					tableCompleteList.add((ArrayList<GenericTableData>) tableData);
				}
			});
			if(!tableCompleteList.isEmpty()) {
				dbResponse.get(0).getDataList().clear();
				dbResponse.get(0).setDataList(tableCompleteList);
				externalInterfaceResponse.setRawBusinessResponse(dbResponse);
			}
				
		} catch (JsonProcessingException e) {
			log.error("Error getting the SetValuesForLocationSBCDetail response:"+e);
		}
	}
}

private  void updateSBCMetadataInfoForLocation(List<GenericTableData> tableDataSBCLocation, String networkNodeId, String url) {
	List<GenericDataBaseResponseDTO> dbResponse = null;
	Task task = new Task();
	task.setTaskName("getSBCMetadataInfoForLocation");
	task.setUrl(url);
	Specification networkNodeIdSpec = new Specification();
	networkNodeIdSpec.setSpecCode("NWK_NODE_DEVICE_ID");
	networkNodeIdSpec.setSpecValue(networkNodeId);
	ExternalInterfaceRequest request = new ExternalInterfaceRequest();
	request.getPayload().add(networkNodeIdSpec);
	Header header = new Header("getSBCMetadataInfoForLocation", "ITS",  new ArrayList<>());
	request.setHeader(header);
	List<Task> taskList= new ArrayList<>();
	taskList.add(task);
	ExternalInterfaceResponse  externalInterfaceResponse = new ExternalInterfaceResponse();
	
	try {

		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
				externalInterfaceResponse);
		dbResponse = objectMapper.readValue(
				externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {
				});
		List<List<GenericTableData>> tableDataList = dbResponse.get(0).getDataList();
		tableDataList.forEach(tableData ->{
			if("DEF_VPN_BW".equalsIgnoreCase(tableData.get(0).getColumnValue()) || "SBC_BW_POLICING".equalsIgnoreCase(tableData.get(0).getColumnValue())) {
				GenericTableData genericTableData = new GenericTableData();
				genericTableData.setColumnName(tableData.get(0).getColumnValue());
				genericTableData.setColumnValue(tableData.get(1).getColumnValue());
				tableDataSBCLocation.add(genericTableData);
			}
				
		});
	} catch (Exception e1) {
		log.info("Exception updateSBCMetadataInfoForLocation" + e1);

	} 
}



/**********************************************************************************************************
 * @param task parameter of method
 * @param request parameter of method
 * @param taskList parameter of method
 * @param externalInterfaceResponse parameter of method
 * @throws JsonProcessingException
 * This method is used for fetching Location IPCC SBC Signalling details From ITS source system
 **********************************************************************************************************/
public void getIPCCSBCSignallingInfo(Task task, ExternalInterfaceRequest request,
		List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
	log.info("Inside  getIPCCSBCSignallingInfo, request::" + objectMapper.writeValueAsString(request));
	Status status=new Status();
	Map<String,String> validationList = webServicesImplUtil.createValidationMap(request);
	if (StringUtils.isNotBlank(validationList.get("IPTermFQDN"))) {
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  getIPCCSBCSignallingInfo, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
	}else {
		status.setStatusCode(WebServicesErrorCodes.IPCC_FQDN_ERROR.errorCode);
		status.setStatusDescription(WebServicesErrorCodes.IPCC_FQDN_ERROR.errorDesc);
		externalInterfaceResponse.setRawBusinessResponse(status);
   }

}

public void getEBISBCConfigDetail(Task task, ExternalInterfaceRequest request,
		List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
	
	log.info("Inside getEBISBCConfigDetail, request::" + objectMapper.writeValueAsString(request));
	Map<String, String> validationList = webServicesImplUtil.createValidationMap(request);
	Status status=new Status();
	
	if(StringUtils.isEmpty(validationList.get("EBIId"))) {
		status.setStatusCode(WebServicesErrorCodes.MISSING_EBI_ID.errorCode);
		status.setStatusDescription(WebServicesErrorCodes.MISSING_EBI_ID.errorDesc);
		externalInterfaceResponse.setRawBusinessResponse(status);
	}
	else {
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
	}
	log.info("Exited getCustomerList, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
}

public void getEnterpriseSBCDeviceList(Task task, ExternalInterfaceRequest request,
		List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
	log.info("Inside  GetEnterpriseSBCDeviceList, request :: " + objectMapper.writeValueAsString(request));
	Status status=new Status();
	GetEnterpriseSBCDeviceListRequest getEnterpriseSBCDeviceListRequest = webServicesImplUtil2.extractInputValuesOfGetEnterpriseSBCDeviceListRequest(request);
	if (StringUtils.isNotBlank(getEnterpriseSBCDeviceListRequest.getEnterpriseID())) {
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		log.info("Exited  GetEnterpriseSBCDeviceList, response :: " + objectMapper.writeValueAsString(externalInterfaceResponse));
	}else {
		status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorCode);
		status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorDesc);
		externalInterfaceResponse.setRawBusinessResponse(status);
   }
}

public void getEnterpriseSBCEBIList(Task task, ExternalInterfaceRequest request,
		List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
	log.info("Inside  GetEnterpriseSBCEBIList, request :: " + objectMapper.writeValueAsString(request));
	Status status=new Status();
	GetEnterpriseSBCDeviceListRequest getEnterpriseSBCEBIListRequest = webServicesImplUtil2.extractInputValuesOfGetEnterpriseSBCEBIListRequest(request);
	if (getEnterpriseSBCEBIListRequest.getEnterpriseID() != null && StringUtils.isNotBlank(getEnterpriseSBCEBIListRequest.getEnterpriseID())) {
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
	    log.info("Exited  GetEnterpriseSBCEBIList, response :: " + objectMapper.writeValueAsString(externalInterfaceResponse));
	}else {
		status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorCode);
		status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_ID.errorDesc);
		externalInterfaceResponse.setRawBusinessResponse(status);
   }
}

public void getLocationSBCDeviceList(Task task, ExternalInterfaceRequest request,
		List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse)  throws JsonProcessingException{
	log.info("Inside  GetLocationSBCDeviceList, request :: " + objectMapper.writeValueAsString(request));
	Status status=new Status();
	GetLocationSBCDeviceListRequest getLocationSBCDeviceListRequest = webServicesImplUtil2.extractInputValuesOfGetLocationSBCDeviceListRequest(request);
	if (getLocationSBCDeviceListRequest.getLocationID() != null && StringUtils.isNotBlank(getLocationSBCDeviceListRequest.getLocationID())) {
		String actualTaskName = task.getTaskName();
		task.setTaskName(RulesConstants.CHECK_LOCATION_ID_EXIST);
		task.setTaskName(actualTaskName);
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}else {
			status.setStatusCode(WebServicesErrorCodes.INVALID_LOCATION_ID.errorCode);
			status.setStatusDescription(WebServicesErrorCodes.INVALID_LOCATION_ID.errorDesc);
			externalInterfaceResponse.setRawBusinessResponse(status);
		}
		log.info("Exited  GetLocationSBCDeviceList, response :: " + objectMapper.writeValueAsString(externalInterfaceResponse));
	}

public void getETGroupDetails(Task task, ExternalInterfaceRequest request,
		List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
	
	log.info("Inside getETGroupDetails, request::" + objectMapper.writeValueAsString(request));
	Map<String, String> validationList = webServicesImplUtil.createValidationMap(request);
	Status status=new Status();
	
	if(StringUtils.isEmpty(validationList.get("TrunkGroupId"))) {
		status.setStatusCode(WebServicesErrorCodes.MISSING_TRUNKGROUP_ID.errorCode);
		status.setStatusDescription(WebServicesErrorCodes.MISSING_TRUNKGROUP_ID.errorDesc);
		externalInterfaceResponse.setRawBusinessResponse(status);
	}
	else {
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
	}
	log.info("Exited getCustomerList, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
}

public void getCustomerDetailsITS(Task task, ExternalInterfaceRequest request,
		List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
	
	log.info("Inside getETGroupDetails, request::" + objectMapper.writeValueAsString(request));
	Map<String, String> validationList = webServicesImplUtil.createValidationMap(request);
	Status status=new Status();
	
	if(StringUtils.isEmpty(validationList.get("EnterpriseID")) && StringUtils.isEmpty(validationList.get("PublicPhoneNumber"))) {
		status.setStatusCode(WebServicesErrorCodes.MISSING_ENTERPRISE_ID_PUBLIC_PHONE_NUMBER.errorCode);
		status.setStatusDescription(WebServicesErrorCodes.MISSING_ENTERPRISE_ID_PUBLIC_PHONE_NUMBER.errorDesc);
		externalInterfaceResponse.setRawBusinessResponse(status);
	}
	else {
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
	}
	log.info("Exited getCustomerDetailsITS, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
}

public void getDeviceSBCConfigDetail(Task task, ExternalInterfaceRequest request,
		List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse) throws JsonProcessingException {
	
	log.info("Inside getDeviceSBCConfigDetail, request::" + objectMapper.writeValueAsString(request));
	Map<String, String> validationList = webServicesImplUtil.createValidationMap(request);
	Status status=new Status();
	
	if(StringUtils.isEmpty(validationList.get("DeviceID")) && StringUtils.isEmpty(validationList.get("LocationID")) && StringUtils.isEmpty(validationList.get("EnterpriseID"))) {
		status.setStatusCode(WebServicesErrorCodes.MISSING_LOCATION_DEVICE_ENTERPRISE_ID.errorCode);
		status.setStatusDescription(WebServicesErrorCodes.MISSING_LOCATION_DEVICE_ENTERPRISE_ID.errorDesc);
		externalInterfaceResponse.setRawBusinessResponse(status);
	}
	else {
		restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
	}
	log.info("Exited getDeviceSBCConfigDetail, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
}
}
 