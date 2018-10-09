package marketing.satori.kintone.csv.app.mail;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.app.KintonePostRequest;
import marketing.satori.kintone.csv.app.KintoneRequest;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 *
 * SegmentへのPOSTリクエストです。
 *
 */
public class MailPostRequest extends KintonePostRequest {

	public MailPostRequest(KintoneHost host) throws JSONException {
		super(host, host.getMailMaster());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		String campaignMailId = csvRecord.get("campaign_mail_id");
		String campaignMailName = csvRecord.get("campaign_mail_name");

		JSONObject jsonObj = new JSONObject();
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CampaignMailId", campaignMailId);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CampaignMailName", campaignMailName);

		super.put(jsonObj);
	}

}
