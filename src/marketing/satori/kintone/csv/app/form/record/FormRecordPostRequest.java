package marketing.satori.kintone.csv.app.form.record;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.Utils;
import marketing.satori.kintone.csv.app.KeyRecordMap;
import marketing.satori.kintone.csv.app.KintonePostRequest;
import marketing.satori.kintone.csv.app.KintoneRequest;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 *
 * SegmentへのPOSTリクエストです。
 *
 */
public class FormRecordPostRequest extends KintonePostRequest {

	/** マスタデータ */
	KeyRecordMap masterMap;

	public FormRecordPostRequest(KintoneHost host, KeyRecordMap masterMap) throws JSONException {
		super(host, host.getFormRecord());
		this.masterMap = masterMap;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		String leadIdentity = csvRecord.get("lead_identity");
		String campaignFormId = csvRecord.get("campaign_form_id");
		String parameterJson = csvRecord.get("parameter_json");
		//String wkstring = csvRecord.get("datetime");
		//if (wkstring.indexOf("UTC") < 0) {
		//	wkstring = wkstring + " UTC";
		//}
		//String datetime = Utils.csvDateStringToJsonDateString(csvRecord.get("datetime"),
		//		Config.getInstance().getKintoneHost().getTimezone());
		String datetime = Utils.csvDateStringToJsonDateString(csvRecord.get("datetime"),"Etc/GMT");

		String[] keyNameList = masterMap.getApp().getCsvKeyColumns();
		String[] keyValueList = new String[keyNameList.length];
		for (int i = 0; i < keyNameList.length; i++) {
			keyValueList[i] = csvRecord.get(keyNameList[i]);
		}

		String campaignFormName = "";
		String extraParamJson = "";
		if (masterMap.containsKey(keyValueList)) {
			campaignFormName = masterMap.getRecord(keyValueList).get("CampaignFormName");
			if (masterMap.getRecord(keyValueList).containsKey("ExtraParamJson")) {
			    extraParamJson = masterMap.getRecord(keyValueList).get("ExtraParamJson");
			}
		}

		JSONObject jsonObj = new JSONObject();

		JSONObject recordObj = new JSONObject();
		KintoneRequest.addJsonValueRecordItem(jsonObj, "LeadIdentity", leadIdentity);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CampaignFormId", campaignFormId);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "ParameterJson", parameterJson);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Datetime", datetime);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CampaignFormName", campaignFormName);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "ExtraParamJson", extraParamJson);

		jsonObj.put("record", recordObj);

		super.put(jsonObj);
	}

}
