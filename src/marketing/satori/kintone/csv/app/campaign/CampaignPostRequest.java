package marketing.satori.kintone.csv.app.campaign;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.app.KintonePostRequest;
import marketing.satori.kintone.csv.app.KintoneRequest;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 *
 * CampaignへのPostリクエストです。
 *
 */
public class CampaignPostRequest extends KintonePostRequest {

	/**
	 *
	 * @param keyIdMap
	 * @throws JSONException
	 */
	public CampaignPostRequest(KintoneHost host) throws JSONException {
		super(host, host.getCampaign());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		String campaignId = csvRecord.get("campaign_id");
		String campaignName = csvRecord.get("campaign_name");

		JSONObject jsonObj = new JSONObject();
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CampaignId", campaignId);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Name", campaignName);
		KintoneRequest.addJsonArrayRecordItem(jsonObj, "IsActive", new String[]{"有効"});

		super.put(jsonObj);
	}

}
