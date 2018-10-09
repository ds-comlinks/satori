package marketing.satori.kintone.csv.app.leadcontact;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.Utils;
import marketing.satori.kintone.csv.app.KintonePutRequest;
import marketing.satori.kintone.csv.app.KintoneRequest;
import marketing.satori.kintone.csv.config.KintoneHost;
import marketing.satori.kintone.csv.config.S3Config;

public class LeadContactDeletePutRequest extends KintonePutRequest {

	public LeadContactDeletePutRequest(KintoneHost host) throws JSONException {
		super(host, host.getLeadContact());
	}

	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		String leadIdentity = csvRecord.get("lead_identity");
		String email = csvRecord.get("email");
		String datetime = Utils.csvDateStringToJsonDateString(csvRecord.get("datetime"), this.getHost().getTimezone());

		if (StringUtils.isEmpty(leadIdentity)) {
			leadIdentity = Utils.toSha256(String.valueOf(S3Config.getInstance().getCompanyId()), email);
		}

		JSONObject jsonObj = new JSONObject();

		JSONObject recordObj = new JSONObject();
		KintoneRequest.addJsonValueRecordItem(recordObj, "LeadIdentity", leadIdentity);
		KintoneRequest.addJsonValueRecordItem(recordObj, "DeleteFlag", Integer.valueOf(1));
		KintoneRequest.addJsonValueRecordItem(recordObj, "DeletedDateTime", datetime);

		JSONObject updateKeyObj = new JSONObject();
		updateKeyObj.put("field", "Email");
		updateKeyObj.put("value", email);

		jsonObj.put("updateKey", updateKeyObj);
		jsonObj.put("record", recordObj);

		super.put(jsonObj);
	}

}
