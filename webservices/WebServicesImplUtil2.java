package com.verizon.connect.infra.interfaceservice.external.interfaces.webservices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.ExternalInterfaceRequest;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.ExternalInterfaceResponse;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.IproLocation;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.LocationDetails;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.LocationDetailsByTNwithCC;
import com.verizon.connect.infra.interfaceservice.external.interfaces.model.Specification;
import com.verizon.connect.infra.interfaceservice.external.interfaces.taskexecution.RestDbOperationServiceConnector;
import com.verizon.connect.infra.interfaceservice.external.interfaces.taskexecution.Task;
import com.verizon.connect.infra.interfaceservice.model.GetEnterpriseSBCDeviceListRequest;
import com.verizon.connect.infra.interfaceservice.model.GetLocationListRequest;
import com.verizon.connect.infra.interfaceservice.model.GetLocationSBCDeviceListRequest;
import com.verizon.connect.infra.interfaceservice.model.LocationEnum;
import com.verizon.connect.infra.interfaceservice.model.RegionMapping;
import com.verizon.connect.infra.interfaceservice.model.TNPoolSummeryRequest;
import com.verizon.connect.infra.interfaceservice.model.om.Status;
import com.verizon.infrastructure.connect.genericdatabase.dto.GenericDataBaseResponseDTO;
import com.verizon.infrastructure.connect.genericdatabase.dto.GenericTableData;
import com.verizon.infrastructure.connect.genericdatabase.dto.PreparedQueryV3;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebServicesImplUtil2 extends WebServicesImplUtil {
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	RestDbOperationServiceConnector restDbOperationServiceConnector;

	public void setEmptyFlag(GenericTableData emptyFlag, int esapDbTn, int reqTn, boolean isSingleSTN) {
		
		emptyFlag.setColumnName("empty_flag");
		if (esapDbTn > reqTn)
			emptyFlag.setColumnValue("N");
		else if (esapDbTn == reqTn)
			emptyFlag.setColumnValue("Y");
		if (isSingleSTN)
			emptyFlag.setColumnValue("N");
		else if (reqTn == 0)
			emptyFlag.setColumnValue("Y");
		
	}

	public String getFormattedStnDeviceDetails(Map<String, String> stnDevice) {
		if(stnDevice==null) return "";
		String formattedStnDevice = "";
		for(Entry<String, String> entry : stnDevice.entrySet()) {
			String s = entry.getKey().toString() + ","+entry.getValue().toString();
			formattedStnDevice += formattedStnDevice.isEmpty() ? s : "|" + s;  //tn,deviceId|tn,deviceId
		}
		return formattedStnDevice;
	}

	public String getFormattedNoRecLocTn(List<String> locAndTnNotInEsap) {

		String noRecLocTn = "";
		for (String i : locAndTnNotInEsap) {
			noRecLocTn += noRecLocTn.isEmpty() ? i : "," + i;
		}
		return noRecLocTn;
	}

	public IproLocation getFormattedEsapLocSegDetails(Map<String, ArrayList<String>> locAndTninEsap) {
		String esapLocTn = "";
		IproLocation iprLoc = new IproLocation();
		Iterator<String> itr = locAndTninEsap.keySet().iterator();
		while (itr.hasNext()) {
			Object loc = itr.next();
			ArrayList<String> tnList = (ArrayList) locAndTninEsap.get(loc);
			for (String i : tnList) {
				esapLocTn += esapLocTn.isEmpty() ? i : "," + i; // tn,tn,tn
			}
			iprLoc.setLocationId(loc.toString());
		}
		iprLoc.setLocationAndTninEsap(esapLocTn);
		return iprLoc;
	}

	public int setOtherLocTns(Map<String, ArrayList<String>> locDetail,
			ExternalInterfaceResponse externalInterfaceResponse, String tn, List<String> locAndTnNotInEsap, int k) throws JsonMappingException, JsonProcessingException {

		if (externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null) {

			List<GenericDataBaseResponseDTO> dbResponse = null;
			dbResponse = objectMapper.readValue(externalInterfaceResponse.getRawResponse(),
					new TypeReference<List<GenericDataBaseResponseDTO>>() {
					});

			GenericDataBaseResponseDTO table = dbResponse.get(0);
			if (table.getDataList() != null && table.getDataList().size() > 0) {

				List<GenericTableData> tableData = table.getDataList().get(0);
				LocationDetails loc = new LocationDetails();
				tableData.stream().forEach(data -> {

					if ("location_id".equalsIgnoreCase(data.getColumnName()))
						loc.setLocationId(data.getColumnValue());
					else if ("tn".equalsIgnoreCase(data.getColumnName()))
						loc.setTn(tn);
					else
						loc.setType(data.getColumnValue());

				});
				if (locDetail.get(loc.getLocationId()) == null)
					locDetail.put(loc.getLocationId(), new ArrayList<>(Arrays.asList(loc.getTn())));
				else
					locDetail.get(loc.getLocationId()).add(loc.getTn());

			locAndTnNotInEsap.remove(tn);
			}
			else k++;
		}else k++;
		return k;
	}
	
	public void setValues(List<GenericTableData> data, String taskname, RegionMapping jsonObject,
			Map<String, RegionMapping> regionMap) 
			throws JsonMappingException, JsonProcessingException {
		
		ObjectMapper mapper = new ObjectMapper();
		for (GenericTableData data1 : data) {
			if ("task_name".equalsIgnoreCase(data1.getColumnName()))
				taskname = data1.getColumnValue();
			else if ("regionmapping".equalsIgnoreCase(data1.getColumnName()))
				jsonObject = mapper.readValue(data1.getColumnValue().toString(), RegionMapping.class);
		}
		regionMap.put(taskname, jsonObject);
	}
	
	public String[] inputValuesSetBsCountryCodeMapping(ExternalInterfaceRequest request) {
		
		String[] inputValues = new String[10];
		request.getPayload().stream().forEach(spec -> {
			if ("transactionId".equalsIgnoreCase(spec.getSpecCode())) {
				inputValues[0] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
						? String.valueOf(spec.getSpecValue())
						: null;
			} else if("action".equalsIgnoreCase(spec.getSpecCode())) {
				inputValues[1] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
						? String.valueOf(spec.getSpecValue())
						: null;
			}else if("bsASName".equalsIgnoreCase(spec.getSpecCode())) {
				inputValues[2] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
						? String.valueOf(spec.getSpecValue())
						: null;
			}else if("bsASShortName".equalsIgnoreCase(spec.getSpecCode())) {
				inputValues[3] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
						? String.valueOf(spec.getSpecValue())
						: null;
			}else if("country".equalsIgnoreCase(spec.getSpecCode())) {
				inputValues[4] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
						? String.valueOf(spec.getSpecValue())
						: null;
			}else if("state".equalsIgnoreCase(spec.getSpecCode())) {
				inputValues[5] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
						? String.valueOf(spec.getSpecValue())
						: null;
			}else if("defaultIndicator".equalsIgnoreCase(spec.getSpecCode())) {
				inputValues[6] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
						? String.valueOf(spec.getSpecValue())
						: null;
			}else if("sourceSystem".equalsIgnoreCase(spec.getSpecCode())) {
				inputValues[7] = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)
						? String.valueOf(spec.getSpecValue())
						: null;
			}
		});
		
		return inputValues;
	
	}
	
	public void setOtherFormattedTns(LocationDetailsByTNwithCC details, Map<String, ArrayList<String>> otherLocTns) {

		String otherLocationTns = "";
		for(Entry<String, ArrayList<String>> locId : otherLocTns.entrySet()) {

			String tns = otherLocTns.get(locId.getKey()).stream().collect(Collectors.joining(","));
			if(!otherLocationTns.isEmpty())
				otherLocationTns = otherLocationTns+"|"+locId.getKey()+":"+tns;
			else
				otherLocationTns = locId.getKey()+":"+tns;
		}
		details.setOtherLocationTns(otherLocationTns);
	}
	
	public TNPoolSummeryRequest extractInputValuesOfGetTNPoolSummeryRequest(ExternalInterfaceRequest request) {
		TNPoolSummeryRequest tnPoolSummeryRequest = new TNPoolSummeryRequest();
		if(request == null || request.getPayload().isEmpty())
			return tnPoolSummeryRequest;
		request.getPayload().stream().forEach(spec -> {
				String paramValue =  (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0) ? 
						String.valueOf(spec.getSpecValue()): null;
				switch (spec.getSpecCode()) {
					case "EnterpriseID":
						tnPoolSummeryRequest.setEnterpriseID(paramValue);
						break;
					case "DepartmentID":
						tnPoolSummeryRequest.setDepartmentID(paramValue);
						break;
					case "LocationID":
						tnPoolSummeryRequest.setLocationID(paramValue);
						break;
					case "LocationName":
						tnPoolSummeryRequest.setLocationName(paramValue);
						break;
					case "TN":
						tnPoolSummeryRequest.setTn(paramValue);
						break;
					case "StartTN":
						tnPoolSummeryRequest.setStartTN(paramValue);
						break;
					case "EndTN":
						tnPoolSummeryRequest.setEndTN(paramValue);
						break;
					case "Activated":
						tnPoolSummeryRequest.setActivated(paramValue);
						break;
					case "IncludeAvailablePortPending":
						tnPoolSummeryRequest.setIncludeAvailablePortPending(paramValue);
						break;
					case "IncludeAssignedPortPending":
						tnPoolSummeryRequest.setIncludeAssignedPortPending(paramValue);
						break;
					case "SearchCondition":
						tnPoolSummeryRequest.setSearchCondition(paramValue);
						break;
					case "TNFields":
						tnPoolSummeryRequest.setTnFields(paramValue);
						break;
					case "SortCondition":
						tnPoolSummeryRequest.setSortCondition(paramValue);
						break;
					case "Offset":
						tnPoolSummeryRequest.setOffset(paramValue);
						break;
					case "Size":
						tnPoolSummeryRequest.setSize(paramValue);
						break;
					default:
						break;
				}
		});
		return tnPoolSummeryRequest;
	}
	
	public GetLocationListRequest extractInputValuesOfGetLocationListRequest(ExternalInterfaceRequest request) {
		GetLocationListRequest getLocationListRequest = new GetLocationListRequest();
		if (request == null || request.getPayload().isEmpty())
			return getLocationListRequest;
		request.getPayload().stream().forEach(spec -> {
			String paramValue = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)? String.valueOf(spec.getSpecValue()): null;
			switch (spec.getSpecCode()) {
			case "EntepriseId":
				getLocationListRequest.setEnterpriseID(paramValue);
				break;
			case "LocationName":
				getLocationListRequest.setLocationName(paramValue);
				break;
			case "CircuitId":
				getLocationListRequest.setCircuitId(paramValue);
				break;
			default:
				break;
			}
		});
		return getLocationListRequest;
	}
	
	public List<PreparedQueryV3> extractLocationIdsOfIproLocationDetailsRequest(Map<String, String> validationList) {
		List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
		PreparedQueryV3 preparedQuery = new PreparedQueryV3();
		 preparedQuery.setKey("LocationOrTnConditionCheck");
		StringBuffer buffer = new StringBuffer();
		if (StringUtils.isEmpty((validationList.get("LocationId"))) && !StringUtils.isEmpty((validationList.get("TN"))) ) {
		buffer.append(" (tninv.tn = '");
		buffer.append(validationList.get("TN") + "')");
		}
		if (!StringUtils.isEmpty((validationList.get("LocationId"))) && StringUtils.isEmpty((validationList.get("TN"))) ) {
		buffer.append(" (svcinv.entity_id = '");
		buffer.append(validationList.get("LocationId") + "')");
		}else if (!StringUtils.isEmpty((validationList.get("LocationId"))) && !StringUtils.isEmpty((validationList.get("TN")))) {
		buffer.append(" (svcinv.entity_id = '");
		buffer.append(validationList.get("LocationId") + "'");
		buffer.append(" and tninv.tn = '");
		buffer.append(validationList.get("TN") + "')");
		}
		preparedQuery.setSqlText(buffer.toString());
		preparedQueryList.add(preparedQuery);
		return preparedQueryList;
		}
	public List<PreparedQueryV3> extractEnterpriseIdOfGetGatewayListForEnterpriseRequest(Map<String, String> validationList) {
		List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
		PreparedQueryV3 preparedQuery = new PreparedQueryV3();
		preparedQuery.setKey("EnterpriseIdConditionCheck");
		StringBuffer buffer = new StringBuffer();
		if (!StringUtils.isEmpty((validationList.get("EnterpriseID")))) {
			buffer.append(" entityAttributes_enterpriseId->>'specValue' = '");
			buffer.append(validationList.get("EnterpriseID") + "'");
		}
		preparedQuery.setSqlText(buffer.toString());
		preparedQueryList.add(preparedQuery);
		return preparedQueryList;
		}

	public void getAddressInfo(ExternalInterfaceResponse response, Map<String, String> ipAddress) 
			throws JsonMappingException, JsonProcessingException {

		if (response != null && response.getRawResponse() != null) {
			List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(response.getRawResponse(),
					new TypeReference<List<GenericDataBaseResponseDTO>>() {
					});
			if (dbResponse != null && dbResponse.get(0) != null && dbResponse.get(0).getDataList() != null
					&& dbResponse.get(0).getDataList().get(0) != null) {
				List<GenericTableData> data = dbResponse.get(0).getDataList().get(0);
				
				data.stream().forEach(tableData -> {
					if (("prima_addr").equalsIgnoreCase(tableData.getColumnName()))
						ipAddress.put("Primary", tableData.getColumnValue());
					else
						ipAddress.put("Secondary", tableData.getColumnValue());
				});
			}
		}
	}
	
public String getLocationIdRequest(List<GenericDataBaseResponseDTO> dbResponse) {
		
		final StringBuilder locId = new StringBuilder();
		List<GenericTableData> tableData = dbResponse.get(0).getDataList().get(0);
		tableData.forEach(data -> {
			if("location_id".equalsIgnoreCase(data.getColumnName())) {
				locId.append(data.getColumnValue());
			}
		});
		return locId.toString();
		
	}

	public String getStnDevice(ExternalInterfaceResponse externalInterfaceResponseHub) 
			throws JsonMappingException, JsonProcessingException {

		String formattedStnDevice = "";
		if (externalInterfaceResponseHub != null && externalInterfaceResponseHub.getRawResponse() != null) {
			List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(
					externalInterfaceResponseHub.getRawResponse(),
					new TypeReference<List<GenericDataBaseResponseDTO>>() {
					});
			List<List<GenericTableData>> tData = dbResponse.get(0).getDataList();
			for (List<GenericTableData> data : tData) {// device:tn|device:tn
				LocationDetails loc = new LocationDetails();
				data.stream().forEach(d -> {
					if(("gateway_device_id").equalsIgnoreCase(d.getColumnName()))
						loc.setLocationId(d.getColumnValue());
					else
						loc.setTn(d.getColumnValue().substring(1));
				});
				formattedStnDevice += loc.getLocationId() + ":" + loc.getTn() + "|";
			}
		}
		return formattedStnDevice;
	}
	
	public GetEnterpriseSBCDeviceListRequest extractInputValuesOfGetEnterpriseSBCDeviceListRequest(ExternalInterfaceRequest request) {
		GetEnterpriseSBCDeviceListRequest getEnterpriseSBCDeviceListRequest = new GetEnterpriseSBCDeviceListRequest();
		if (request == null || request.getPayload().isEmpty())
			return getEnterpriseSBCDeviceListRequest;
		request.getPayload().stream().forEach(spec -> {
			String paramValue = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)? String.valueOf(spec.getSpecValue()): null;
			switch (spec.getSpecCode()) {
			case "enterpriseID":
				getEnterpriseSBCDeviceListRequest.setEnterpriseID(paramValue);
				break;
			default:
				break;
			}
		});
		return getEnterpriseSBCDeviceListRequest;
	}

	public GetEnterpriseSBCDeviceListRequest extractInputValuesOfGetEnterpriseSBCEBIListRequest(ExternalInterfaceRequest request) {
		GetEnterpriseSBCDeviceListRequest getEnterpriseSBCEBIListRequest = new GetEnterpriseSBCDeviceListRequest();
		if (request == null || request.getPayload().isEmpty())
			return getEnterpriseSBCEBIListRequest;
		request.getPayload().stream().forEach(spec -> {
			String paramValue = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)? String.valueOf(spec.getSpecValue()): null;
			switch (spec.getSpecCode()) {
			case "EnterpriseId":
				getEnterpriseSBCEBIListRequest.setEnterpriseID(paramValue);
				break;
			default:
				break;
			}
		});
		return getEnterpriseSBCEBIListRequest;
	}

	public GetLocationSBCDeviceListRequest extractInputValuesOfGetLocationSBCDeviceListRequest(ExternalInterfaceRequest request) {
		GetLocationSBCDeviceListRequest getLocationSBCDeviceListRequest = new GetLocationSBCDeviceListRequest();
		if (request == null || request.getPayload().isEmpty())
			return getLocationSBCDeviceListRequest;
		request.getPayload().stream().forEach(spec -> {
			String paramValue = (spec.getSpecValue() != null && spec.getSpecValue().toString().trim().length() > 0)? String.valueOf(spec.getSpecValue()): null;
			switch (spec.getSpecCode()) {
			case "locationID":
				getLocationSBCDeviceListRequest.setLocationID(paramValue);
				break;
			default:
				break;
			}
		});
		return getLocationSBCDeviceListRequest;
	}
	
	
	public void setSTNValues(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			List<GenericDataBaseResponseDTO> dbResponse, List<GenericTableData> tableData)
			throws JsonProcessingException {
		String locTypeStr = extractLocationTypeOfGetSTNDetails(dbResponse);
		
		if (("1").equals(locTypeStr) || ("HUB").equalsIgnoreCase(locTypeStr) ) {
			task.setTaskName("getSTNDetailsForHub");
			ExternalInterfaceResponse externalInterfaceResponseHub = new ExternalInterfaceResponse();
			getStnDeviceforHub(task, request, taskList, externalInterfaceResponseHub, tableData);
		} else if (("2").equals(locTypeStr) || ("REMOTE").equalsIgnoreCase(locTypeStr)) {
			task.setTaskName("getSTNDetailsForRemote");
			ExternalInterfaceResponse externalInterfaceResponseRemote = new ExternalInterfaceResponse();
			getStnForRemoteLoc(task, request, taskList, externalInterfaceResponseRemote, tableData);
		} 
	}
	
	public void getStnDeviceforHub(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse, List<GenericTableData> tableData)
					throws JsonProcessingException {
		
		log.info("Inside  getStnDeviceforHub, request::" + objectMapper.writeValueAsString(request));
		if (restDbOperationServiceConnector !=null) {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
		}
		log.info("Exited  getStnDeviceforHub, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
		
		if(externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null) {
			try {
				List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(externalInterfaceResponse.getRawResponse(),
						new TypeReference<List<GenericDataBaseResponseDTO>>() {});
				if(dbResponse.get(0).getDataList().isEmpty()) {
					return ;
				}
				List<GenericTableData> tableDataResponse = dbResponse.get(0).getDataList().get(0);
				List<GenericTableData> tableDataResult = new ArrayList<>();
				tableDataResponse.forEach(data -> {
					if(("DeviceId").equalsIgnoreCase(data.getColumnName()) || ("tn").equalsIgnoreCase(data.getColumnName())) {
						GenericTableData tData = new GenericTableData();
						tData.setColumnName(data.getColumnName());
						tData.setColumnValue(data.getColumnValue());
						tableDataResult.add(tData);
					}
				});
				
				tableData.addAll(tableDataResult);
			}catch(Exception e) {
				log.error("Invalid externalInterfaceResponse", e.getMessage());
			}
		}
	}
	
	public void getStnForRemoteLoc(Task task, ExternalInterfaceRequest request,
			List<Task> taskList, ExternalInterfaceResponse externalInterfaceResponse, List<GenericTableData> tableData) throws JsonProcessingException {
		
		log.info("Inside  getStnForRemoteLoc, request::" + objectMapper.writeValueAsString(request));
		if(restDbOperationServiceConnector!=null) {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}
		
		log.info("Exited  getStnForRemoteLoc, response::" + objectMapper.writeValueAsString(externalInterfaceResponse));
		
		if(externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null) {
			try {
				List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(externalInterfaceResponse.getRawResponse(),
						new TypeReference<List<GenericDataBaseResponseDTO>>() {
		        });
				GenericTableData tData = new GenericTableData();
				List<GenericTableData> tableDataResponse = dbResponse.get(0).getDataList().get(0);
				
				tableDataResponse.forEach(data -> {
					if(("tn").equalsIgnoreCase(data.getColumnName())) {
						tData.setColumnName((String)data.getColumnName());
						tData.setColumnValue((String) data.getColumnValue());
					}
				});
				tableData.add(tData);
			}catch(Exception e) {
				log.error("Invalid externalInterfaceResponse",e.getMessage());
			}
		}
	}

	public void setStatusCodeAndTextForSuccess(List<GenericTableData> tableData) {
		GenericTableData statusCodeData = new GenericTableData();
		statusCodeData.setColumnName("StatusCode");
		statusCodeData.setColumnValue(WebServicesErrorCodes.SUCCESS.errorCode);
		tableData.add(statusCodeData);
		GenericTableData statusTextData = new GenericTableData();
		statusTextData.setColumnName("StatusText");
		statusTextData.setColumnValue(WebServicesErrorCodes.SUCCESS.errorDesc);
		tableData.add(statusTextData);
	}
	
	public void setRemoteLocationIds(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse, String locationId)
			throws JsonProcessingException, JsonMappingException {
		ExternalInterfaceResponse externalInterfaceResponsRemote = new ExternalInterfaceResponse();
		List<PreparedQueryV3> preparedQueryList =
				extractLocationIdsByHubLocation(locationId);
		task.setTaskName("getRemoteLocations");
		if(restDbOperationServiceConnector!=null) {
			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponsRemote, preparedQueryList);
		}
		
		ArrayList<String> remoteLocList = new ArrayList<>();
		
		if(externalInterfaceResponsRemote.getRawResponse()!=null) {
			List<GenericDataBaseResponseDTO> dbResponseRemote = objectMapper.readValue(
					externalInterfaceResponsRemote.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {
					});
			 List<List<GenericTableData>> dataListRemote = dbResponseRemote.get(0).getDataList();
			for (List<GenericTableData> data : dataListRemote) {
				
				remoteLocList.add(data.get(0).getColumnValue());
			}
			
			List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(externalInterfaceResponse.getRawResponse(),
					new TypeReference<List<GenericDataBaseResponseDTO>>() {
					});
			if (dbResponse != null) {
				List<GenericTableData> tableData = dbResponse.get(0).getDataList().get(0);
				GenericTableData ListOfRemoteLoc = new GenericTableData();
				ListOfRemoteLoc.setColumnName("ListOfRemoteLoc");
				ListOfRemoteLoc.setColumnValue(remoteLocList.stream().collect(Collectors.joining(",")));
				tableData.add(ListOfRemoteLoc);
				
				dbResponse.get(0).getDataList().clear();
				dbResponse.get(0).getDataList().add(tableData);
				externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
				//externalInterfaceResponse.setRawBusinessResponse(dbResponse);
			}
		}
		
		
	}
	
	public void setVmaTns(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse, String locationId)
			throws JsonProcessingException, JsonMappingException {
		ExternalInterfaceResponse externalInterfaceResponsVma = new ExternalInterfaceResponse();
		List<PreparedQueryV3> preparedQueryList =
				extractVmaTn(locationId);
		task.setTaskName("getVmaTn");
		if(restDbOperationServiceConnector!=null) {
			restDbOperationServiceConnector.executeRestDbOperationApiV3(task, request, taskList,
					externalInterfaceResponsVma, preparedQueryList);
		}
		String vmatn = "";
		if(externalInterfaceResponsVma.getRawResponse()!=null) {
			List<GenericDataBaseResponseDTO> dbResponseRemote = objectMapper.readValue(
					externalInterfaceResponsVma.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>() {
					});
			 List<List<GenericTableData>> dataListVma = dbResponseRemote.get(0).getDataList();
			for (List<GenericTableData> data : dataListVma) {
				
				vmatn = (data.get(0).getColumnValue());
			}
			
			List<GenericDataBaseResponseDTO> dbResponse = objectMapper.readValue(externalInterfaceResponse.getRawResponse(),
					new TypeReference<List<GenericDataBaseResponseDTO>>() {
					});
			if (dbResponse != null) {
				List<GenericTableData> tableData = dbResponse.get(0).getDataList().get(0);
				GenericTableData ListOfVmatn = new GenericTableData();
				ListOfVmatn.setColumnName("vmatn");
				ListOfVmatn.setColumnValue(vmatn);
				tableData.add(ListOfVmatn);
				
				dbResponse.get(0).getDataList().clear();
				dbResponse.get(0).getDataList().add(tableData);
				externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
				//externalInterfaceResponse.setRawBusinessResponse(dbResponse);
			}
		}
		
		
	}
	
	public List<PreparedQueryV3> extractLocationIdsByHubLocation(String locationId) {
		List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
		PreparedQueryV3 preparedQuery = new PreparedQueryV3();
		preparedQuery.setKey("LocationOrTnConditionCheck");
		StringBuilder buffer = new StringBuilder();
		if (!StringUtils.isEmpty(locationId)) {
			buffer.append(" entityAttributes_hub_location->>'specValue'= '");
			buffer.append(locationId + "'");
		}
		preparedQuery.setSqlText(buffer.toString());
		preparedQueryList.add(preparedQuery);
		return preparedQueryList;
	}
	
	public List<PreparedQueryV3> extractVmaTn(String locationId) {
		List<PreparedQueryV3> preparedQueryList = new ArrayList<>();
		PreparedQueryV3 preparedQuery = new PreparedQueryV3();
		preparedQuery.setKey("VmaTnparam");
		StringBuilder buffer = new StringBuilder();
		if (!StringUtils.isEmpty(locationId)) {
			buffer.append(" tninv .location_id= '");
			buffer.append(locationId + "'");
		}
		preparedQuery.setSqlText(buffer.toString());
		preparedQueryList.add(preparedQuery);
		return preparedQueryList;
	}
	
	public void setCommonErrorDetails(String errorCode, String errorDesc, LocationDetailsByTNwithCC details, ExternalInterfaceResponse externalInterfaceResponse) {
		details.setStatusCode(errorCode);
		details.setStatusDescription(errorDesc);
		externalInterfaceResponse.setRawBusinessResponse(details);
		
	}
	
	
	public void setCommonErrorStatus(String errorCode, String errorDesc, Status status, ExternalInterfaceResponse externalInterfaceResponse) {
		status.setStatusCode(errorCode);
		status.setStatusDescription(errorDesc);
		externalInterfaceResponse.setRawBusinessResponse(status);
		
	}


	public List<Specification> setCommonSpecs(ExternalInterfaceRequest request) {
		List<Specification> specList = new ArrayList<>();
		Specification spec = new Specification();
		spec.setSpecCode(request.getHeader().getSpecifications().get(0).getSpecCode());
		spec.setSpecValue(request.getHeader().getSpecifications().get(0).getSpecValue());
		specList.add(spec);
		return specList;
	}
	
	public void queryByTaskName(String taskName, Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) {
		task.setTaskName(taskName);
		if (restDbOperationServiceConnector!=null) {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList,
					externalInterfaceResponse);
		}
		
	}
	
	public void commonSetLocationSegmentDetails(LocationDetailsByTNwithCC details,
			List<String> locAndTnNotInEsap, 
			Map<String, ArrayList<String>> locAndTninEsap) {
		
		boolean locationPresent = false;
		boolean tnPresent = false;
		boolean noRecFoundTnPresent = false;
		IproLocation iproLoc = getFormattedEsapLocSegDetails(locAndTninEsap);
		String noRecTNs = getFormattedNoRecLocTn(locAndTnNotInEsap);

		if (iproLoc != null && iproLoc.getLocationId() != null && iproLoc.getLocationId().trim().length() > 0
				&& !"null".equals(iproLoc.getLocationId())) {
			locationPresent = true;
		}
		if (iproLoc != null && iproLoc.getEsapTn() != null && iproLoc.getEsapTn().trim().length() > 0) {
			tnPresent = true;
		}
		if (noRecTNs != null && noRecTNs.trim().length() > 0) {
			noRecFoundTnPresent = true;
		}

		if (!locationPresent && !tnPresent) {
			details.setStatusCode(WebServicesErrorCodes.NO_DATA_FOUND_TN.errorCode);
			details.setStatusDescription("Location & TN Not Found in ESAP DB");
		}
		if (!locationPresent) {
			details.setStatusCode(WebServicesErrorCodes.NO_DATA_FOUND_TN.errorCode);
			details.setStatusDescription("Location Not Found in ESAP DB");
		}
		if (iproLoc != null && iproLoc.getLocationId() != null && noRecFoundTnPresent) {
			details.setNoRecordFoundTNs(noRecTNs);
		}

		
	}

	public void getLocationSegmentDetails(Map<String, ArrayList<String>> locAndTninEsap, boolean isSingleSTN,
			ExternalInterfaceResponse externalInterfaceResponse, Task task, 
			List<Task> taskList, Map<String, String> locTypeDtl, String locationId) {
		
		Iterator<String> itr = locAndTninEsap.keySet().iterator();
		while (itr.hasNext()) {
			Object loc = itr.next();
			ArrayList<String> tnList = (ArrayList<String>) locAndTninEsap.get(loc);
			String locationType = locTypeDtl.get(loc);
			String listOfTns = tnList.stream().collect(Collectors.joining(","));
			ExternalInterfaceRequest req =  new ExternalInterfaceRequest();
			Specification spec = new Specification();
			spec.setSpecCode("location_id");
			spec.setSpecValue(loc);
			List<Specification> specs = new ArrayList<>();
			specs.add(spec);
			req.setPayload(specs);
			String locIdType = locationId+ "," +locationType;
			setLocationSegmentDetails(locIdType, isSingleSTN, externalInterfaceResponse, task,
					req, taskList, listOfTns);
		}
	}


	public void setLocationSegmentDetails(String locationIdType, boolean isSingleSTN, 
			ExternalInterfaceResponse externalInterfaceResponse,Task task, 
			ExternalInterfaceRequest request, List<Task> taskList, String listOfTns) {

		task.setTaskName("getLocationDetails");
		if(restDbOperationServiceConnector!=null) {
			restDbOperationServiceConnector.executeRestDbOperationApi(task, request, taskList, externalInterfaceResponse);
		}
		
		List<GenericDataBaseResponseDTO> dbResponse = null;
		Map<String, String> stnDevice = null;
		if (externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null) {
			try {
				dbResponse = objectMapper.readValue(externalInterfaceResponse.getRawResponse(),
						new TypeReference<List<GenericDataBaseResponseDTO>>() {
						});
			} catch (JsonProcessingException e) {
				log.error("Error retrieving database response:" + e);
			}
			String locationId = locationIdType.split(",")[0];
			String location_type = locationIdType.split(",").length>1? locationIdType.split(",")[1]:"";
			
			if (dbResponse != null) {
				GenericDataBaseResponseDTO table = dbResponse.get(dbResponse.size() - 1);
				GenericTableData listOfTn = new GenericTableData();
				listOfTn.setColumnName("ListOfTns");
				listOfTn.setColumnValue(listOfTns);
				if(!table.getDataList().isEmpty())  
					table.getDataList().get(0).add(listOfTn);
				else {
					table.getDataList().add(new ArrayList<>(Arrays.asList(listOfTn))) ;
				}
				ExternalInterfaceResponse resp = new ExternalInterfaceResponse();
				
				int esapDbTn = getTnCountinEsap(task, request, taskList, resp);
				int reqTn = listOfTns.split(",").length;
				GenericTableData emptyFlag = new GenericTableData();
				setEmptyFlag(emptyFlag,esapDbTn,reqTn,isSingleSTN);
				table.getDataList().get(0).add(emptyFlag);
				getStnDetailsLocationSegment(stnDevice,location_type,task,table,taskList,locationId, request);

				try {
					externalInterfaceResponse.setRawResponse(objectMapper.writeValueAsString(dbResponse));
				} catch (JsonProcessingException e) {
					log.error("Error: ====  ",e);
				}
			}
		}
	}
	
	public int getTnCountinEsap(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			ExternalInterfaceResponse externalInterfaceResponse) {

		queryByTaskName("getTnCountinEsap",task, request, taskList,
				externalInterfaceResponse);
		String len = "0";
		if (externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null) {

			String response = externalInterfaceResponse.getRawResponse();
			JSONArray jArray = new JSONArray(response);
			JSONObject jObj = jArray.getJSONObject(0);
			JSONArray dList = jObj.getJSONArray("dataList");
			JSONObject obj = dList.getJSONArray(0).getJSONObject(0);
			 len = (String) obj.get("columnValue");
		}
		return Integer.valueOf(len);
	}
	
	public void getStnDetailsLocationSegment(Map<String, String> stnDevice, String location_type, Task task, 
			GenericDataBaseResponseDTO table, List<Task> taskList, String locationId, ExternalInterfaceRequest request) {
		
		if (location_type.equals(LocationEnum.REMOTE.name())) {
			Specification spec = new Specification();
			spec.setSpecCode("location_id");
			spec.setSpecValue(locationId);
			List<Specification> specs = new ArrayList<>();
			specs.add(spec);
			request.setPayload(specs);
			task.setTaskName("getSTNDetailsForRemote");
			ExternalInterfaceResponse externalInterfaceResponseRemote = new ExternalInterfaceResponse();
			try {
				getStnForRemoteLoc(task, request, taskList, externalInterfaceResponseRemote, null);
			} catch (JsonProcessingException e) {
				log.error("Error: ===== "+stnDevice, e);
			}

			List<String> stn = new ArrayList<>();
			
			GenericTableData tdata = new GenericTableData();
			tdata.setColumnName("STNDeviceDetails");
			tdata.setColumnValue(stn.stream().collect(Collectors.joining(",")));
			table.getDataList().get(0).add(tdata);
		} else if (location_type.equals(LocationEnum.HUB.name())) {
			task.setTaskName("getSTNDetailsForHub");
			ExternalInterfaceResponse externalInterfaceResponseHub = new ExternalInterfaceResponse();
			try {
				getStnDeviceforHub(task, request, taskList, externalInterfaceResponseHub, null);
			} catch (JsonProcessingException e) {
				log.error("Error: ===== ",e);
			}

			GenericTableData tdata = new GenericTableData();
			tdata.setColumnName("STNDeviceDetails");
			tdata.setColumnValue(getFormattedStnDeviceDetails(stnDevice));
			table.getDataList().get(0).add(tdata);
		}
	}
	public void commonRemoveTn(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			List<String> locAndTnNotInEsap) {
		if (locAndTnNotInEsap != null && !locAndTnNotInEsap.isEmpty()) {
			for (int k = 0; k < locAndTnNotInEsap.size(); k++) {
				ExternalInterfaceRequest req = new ExternalInterfaceRequest();
				String tn = locAndTnNotInEsap.get(k);
				Specification spec = new Specification();
				spec.setSpecCode("ListOfTNs");
				spec.setSpecValue(tn);
				List<Specification> specs = new ArrayList<>();
				specs.add(spec);
				req.setPayload(specs);
				ExternalInterfaceResponse externalInterfaceResponseSingle = new ExternalInterfaceResponse();
				queryByTaskName("checkSingleStnESAP",task, request, taskList,
						externalInterfaceResponseSingle);
				
				locAndTnNotInEsap.remove(tn);
			}
		}
	}


	public void commonSetOtherLocations(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			String[] inputValues, List<String> locAndTnNotInEsap, Map<String, ArrayList<String>> otherLocTns, String cc)
			throws JsonMappingException, JsonProcessingException {
		if (locAndTnNotInEsap != null && !locAndTnNotInEsap.isEmpty()) {
			int k = 0;
			while (k < locAndTnNotInEsap.size()) {
				String s = locAndTnNotInEsap.get(k);
				ExternalInterfaceRequest req = new ExternalInterfaceRequest();
				Specification spec = new Specification();
				spec.setSpecCode("ListOfTNs");
				if(inputValues[1]!=null && !inputValues[1].isEmpty() ) {
					spec.setSpecValue(cc.concat(s));
				}else {
					spec.setSpecValue(s);
				}
				List<Specification> specs = new ArrayList<>();
				specs.add(spec);
				req.setPayload(specs);
				log.info("New code deployed:Request===  " + req);
				ExternalInterfaceResponse externalInterfaceResponse1 = new ExternalInterfaceResponse();
				queryByTaskName("getOtherLocTnsESAP",task, request, taskList,
						externalInterfaceResponse1);
				k = setOtherLocTns(otherLocTns, externalInterfaceResponse1, s,
						locAndTnNotInEsap, k);
			}
		}
	}


	public void commonGetLocationDetailsbySegmentESAP(Task task, ExternalInterfaceRequest request, List<Task> taskList,
			String[] inputValues, List<String> locAndTnNotInEsap,  Map<String, String> locTypeDtl,
			Map<String, ArrayList<String>> locAndTninEsap) {
		log.info("Block Start for getLocationDetailsbySegmentESAP");
		String[] tnstr = inputValues[0].replace("\r", "").replace("\n", "").replace("\t", "").split(",");
		for (String tnId : tnstr) {
			request.getPayload().stream().forEach(spec -> {
				if ("ListOfTNs".equalsIgnoreCase(spec.getSpecCode())) {
					if(inputValues[1]!=null && !inputValues[1].isEmpty() ) {
						spec.setSpecValue(inputValues[1].concat(tnId.replace("\r", "").replace("\n", "").replace("\t", "")));
					}else {
						spec.setSpecValue((tnId.replace("\r", "").replace("\n", "").replace("\t", "")));
					}
					
				}
			});
			ExternalInterfaceResponse externalInterfaceResponseSegment= new ExternalInterfaceResponse();
			queryByTaskName("getLocationDetailsbySegmentESAP",task, request, taskList,
					externalInterfaceResponseSegment);
			
			log.info("ExternalInterfaceResponse: === " + externalInterfaceResponseSegment);
			setMapLocIdAndTn(externalInterfaceResponseSegment, locTypeDtl, locAndTninEsap,tnId);
			checkNoRecordsFoundTn(locAndTnNotInEsap,externalInterfaceResponseSegment, tnId);
		}
		log.info("Block ends for getLocationDetailsbySegmentESAP ");
	}

	public void setMapLocIdAndTn(ExternalInterfaceResponse externalInterfaceResponse, Map<String, String> locIdAndType,
			Map<String, ArrayList<String>> locIdAndTn, String tnId) {
		
		log.info("setMapLocIdAndTn() method started=="+tnId);
		List<GenericDataBaseResponseDTO> dbResponse = null;
		if(externalInterfaceResponse != null && externalInterfaceResponse.getRawResponse() != null) {
		try {
			dbResponse = objectMapper.readValue(
					externalInterfaceResponse.getRawResponse(), new TypeReference<List<GenericDataBaseResponseDTO>>(){});
		} catch (JsonProcessingException e) {
			log.error("Error getting the response:"+e);;
		}
		GenericDataBaseResponseDTO table = dbResponse.get(dbResponse.size() - 1);

		if(table.getDataList() != null && table.getDataList().size() > 0) {
			List<GenericTableData> tableData = table.getDataList().get(0);
			LocationDetails loc = new LocationDetails();
			tableData.forEach(data -> {
				switch (data.getColumnName()) {
				case "location_id":
					loc.setLocationId(data.getColumnValue());
					break;
				case "tn":
					loc.setTn(tnId);
					break;
				case "location_type":
					loc.setType(data.getColumnValue());
					break;
				}
			});
			locIdAndType.put(loc.getLocationId(), loc.getType());
			if(locIdAndTn.get(loc.getLocationId()) != null)
				locIdAndTn.get(loc.getLocationId()).add(loc.getTn());
			else locIdAndTn.put(loc.getLocationId(), new ArrayList<>(Arrays.asList(loc.getTn())));
		}
		}
		log.info("setMapLocIdAndTn() method ended=="+tnId);
	}

	public boolean isSingleSTN(Task task, ExternalInterfaceRequest request, List<Task> taskList, boolean isSingleSTN,
			int tnLength) {
		boolean isNonRIVLoc;
		ExternalInterfaceResponse externalInterfaceResponseNOnRiv = new ExternalInterfaceResponse();
		queryByTaskName("checkNonRivLocation",task, request, taskList,
				externalInterfaceResponseNOnRiv);

		isNonRIVLoc = checkDataListIsEmpty(externalInterfaceResponseNOnRiv);
		if (tnLength == 1 && !isNonRIVLoc) {
			ExternalInterfaceResponse externalInterfaceResponseStn = new ExternalInterfaceResponse();
			queryByTaskName("checkSingleStn",task, request, taskList,
					externalInterfaceResponseStn);
			isSingleSTN = checkDataListIsEmpty(externalInterfaceResponseStn);
		}
		return isSingleSTN;
	}
}
