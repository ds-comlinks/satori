package marketing.satori.kintone.csv.app.campaign.record;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.Utils;
import marketing.satori.kintone.csv.app.KeyRecordMap;
import marketing.satori.kintone.csv.app.KintonePostRequest;
import marketing.satori.kintone.csv.app.KintoneRequest;
import marketing.satori.kintone.csv.config.Config;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 *
 * Campaign履歴へのPUTリクエストです。
 *
 */
public class CampaignRecordPostRequest extends KintonePostRequest {

	/** マスタデータ */
	KeyRecordMap campaignMap;

	KeyRecordMap customerMap;

	public CampaignRecordPostRequest(KintoneHost host, KeyRecordMap campaignMap, KeyRecordMap customerMap) throws JSONException {
		super(host, host.getCampaignRecord());
		this.campaignMap = campaignMap;
		this.customerMap = customerMap;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		String leadIdentity = csvRecord.get("lead_identity");
		String campaignId = csvRecord.get("campaign_id");
		String datetime = Utils.csvDateStringToJsonDateString(csvRecord.get("datetime"),
				Config.getInstance().getKintoneHost().getTimezone());

		String campaignName = "";
		{
			String[] keyNameList = campaignMap.getApp().getCsvKeyColumns();
			String[] keyValueList = new String[keyNameList.length];
			for (int i = 0; i < keyNameList.length; i++) {
				keyValueList[i] = csvRecord.get(keyNameList[i]);
			}

			if (campaignMap.containsKey(new String[]{campaignId})) {
				campaignName = campaignMap.getRecord(new String[]{campaignId}).get("Name");
			}
		}

		String company = "";
		String lastName = "";
		String firstName = "";
		{
			String[] keyValueList = new String[]{leadIdentity};
			if (customerMap.containsKey(keyValueList)) {
				Map<String, String> cutomer = customerMap.getRecord(keyValueList);
				company = cutomer.get("Company");
				lastName = cutomer.get("LastName");
				firstName = cutomer.get("FirstName");
			}
		}

		JSONObject jsonObj = new JSONObject();

		JSONObject recordObj = new JSONObject();
		KintoneRequest.addJsonValueRecordItem(jsonObj, "LeadIdentity", leadIdentity);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CampaignId", campaignId);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Datetime", datetime);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CampaignName", campaignName);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Company", company);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "LastName", lastName);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "FirstName", firstName);

		jsonObj.put("record", recordObj);

		super.put(jsonObj);
	}

}
