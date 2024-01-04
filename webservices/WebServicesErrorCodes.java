package com.verizon.connect.infra.interfaceservice.external.interfaces.webservices;

import java.util.HashMap;

public enum WebServicesErrorCodes {
	    INTERNAL_ERROR("1", "internal error"),
	    SUCCESS("0", "SUCCESS"),
	    MISSING_ENTERPRISE_ID("1000", "Enterprise id not in Connect DB"),
	    MISSING_ENTERPRISE_TRUNK_ID("1206", "Missing enterprise Trunk id."),
	    MISSING_KEY_ID("100", "ESAP Internal error"),
		MISSING_OFFSET_ID("Error","Error"),
		MISSING_PBX_ID("4","Missing/Invalid Field"),
		PBX_ID_VALIDATION("1867","Exception Occured, Failed to get Details"),
		MISSING_DEVICE_ID("2405","DBError; failed to get device details"),
		MISSING_PBXID("1801","PBXID not found"),
		MISSING_RECORDS("2","No Records Found"),
		MISSING_OFFSET("Error1","Error1"),
		MISSING_KEY_GRP_ID("1200", "Missing Key id."),
	    MISSING_LOCATION_ID("1300", "missing location id"),
	    MISSING_TSO_LOCATION_ID("1000", "Location Not Found in ESAP DB"),
	    INVALID_TSO_LOCATION_ID("1001", "No Data Found"),
        MISSING_TN("1400", "missing tn"), 
        MISSING_DIALING_COUNTRY_CODE("1500", "missing dialing country code"), 
		MISSING_LOC_ID("1000","Missing LocationId and TN in Connect DB"),
		FAILURE("1000","FAILURE"),
		NO_DATA_FOUND_TN("1001","No Data Found"),
		MISSING_TRUNK_LOCATION_ID("30","Missing LocationID in request XML in DB"),
		MISSING_WORK_ORDER_ID("400","Missing WorkOrderId in request"),
		MISSING_CUSTOMERNAME("30","Missing CustomerName in request XML in DB"),
		FAILED_WORK_ORDER_STATUS("2001", "Work order status is failed"),
		FAILED_WORK_ENTITY_TYPE("1001", "Work order failed due to invalid entity"),
		MISSING_ENTERPRISENAME("100","Missing Enterprisename in request XML in DB"),
		ESAP_TRANSID_MISSING("20", "Transaction ID missing in Request XML"),
		ESAP_INTERNAL_ERROR("1002","Connection Not able to create from Database"),
		ESAP_ENTID_MISSING("10", "Enterprise ID missing in Request XML"),
		IPCC_FQDN_ERROR("104","IPCC FQDN doesn't exists"),
		INVALID_LOCATION_ID("2", "Not a Valid Location ID : "),
		NOT_AVAILABLE_LOCATION_ID("3", "Location Not Existins/Deleted in the ESAP locationID: "), 
		MISSING_EBI_ID("Error","Error"),
		MISSING_TRUNKGROUP_ID("Error","Error"),
		MISSING_ENTERPRISE_ID_PUBLIC_PHONE_NUMBER("Error","Error"),
		MISSING_LOCATION_DEVICE_ENTERPRISE_ID("Error","Error");

	
        public final String errorCode;
	    public final String errorDesc;

	    static private HashMap<String, String> values = new HashMap<>();

	    private WebServicesErrorCodes(String errorCode, String errorDesc)
	    {
	        this.errorCode = errorCode;
	        this.errorDesc = errorDesc;
	    }

	    @Override
	    public String toString()
	    {
	        return String.valueOf(errorCode) + ":" + errorDesc;
	    }

	    public String getErrorCode()
	    {
	        return errorCode;
	    }

	    public String getErrorDesc()
	    {
	        return errorDesc;
	    }

	    public static String getErrorDesc(String errorCode)
	    {
	        if (values.size() == 0)
	        {
	                for (WebServicesErrorCodes cssop : WebServicesErrorCodes.values())
	                    values.put(cssop.getErrorCode(), cssop.getErrorDesc());

	        }

	        return values.get(errorCode);
	    };
}
