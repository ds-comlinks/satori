package marketing.satori.kintone.csv.app.tag.record;

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
public class TagRecordPostRequest extends KintonePostRequest {

	/** マスタデータ */
	KeyRecordMap masterMap;

	public TagRecordPostRequest(KintoneHost host, KeyRecordMap masterMap) throws JSONException {
		super(host, host.getTagRecord());
		this.masterMap = masterMap;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		String id = csvRecord.get("id");
		String leadIdentity = csvRecord.get("lead_identity");
		String customerid = csvRecord.get("customer_id");
		String actiontype = csvRecord.get("action_type");
		String influxsource = csvRecord.get("influx_source");
		//String actiondatetime = Utils.csvDateStringToJsonDateString(csvRecord.get("action_datetime"),
		//		Config.getInstance().getKintoneHost().getTimezone());
        String wkdatetime = csvRecord.get("action_datetime");
        wkdatetime = wkdatetime.replaceAll("-", "\\/").replaceAll("UTC", "").trim();
		String actiondatetime = Utils.csvDateStringToJsonDateString(wkdatetime,"Etc/GMT");

		String[] keyNameList = masterMap.getApp().getCsvKeyColumns();
		String[] keyValueList = new String[keyNameList.length];
		for (int i = 0; i < keyNameList.length; i++) {
			// customer_tag_idの取り出し
			String inluxsource = csvRecord.get(keyNameList[i]);
			JSONObject json = new JSONObject(inluxsource);

			if (json.has("customer_tag_id")) {
			    keyValueList[i] = String.valueOf(json.getInt("customer_tag_id"));
			} else {
				keyValueList[i] = "";
			}
			////String regex ="customer_tag_id=>([0-9]+)";
			////Pattern p = Pattern.compile(regex);
			////Matcher m = p.matcher(inluxsource);
			////if (m.find()) {
			////    keyValueList[i] = m.group(1);
			////} else {
			////	keyValueList[i] = "";
			////}
		}

		String customerTagName = "";
		if (masterMap.containsKey(keyValueList)) {
			customerTagName = masterMap.getRecord(keyValueList).get("CustomerTagName");
		}

		JSONObject jsonObj = new JSONObject();

		JSONObject recordObj = new JSONObject();
		KintoneRequest.addJsonValueRecordItem(jsonObj, "RecordId", id);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "LeadIdentity", leadIdentity);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CustomerId", customerid);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "ActionType", actiontype);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "ActionDatetime", actiondatetime);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "InfluxSource", influxsource);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CustomerTagName", customerTagName);

		jsonObj.put("record", recordObj);

		super.put(jsonObj);
	}

}
