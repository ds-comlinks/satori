package marketing.satori.kintone.csv.app.tag;

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
public class TagPostRequest extends KintonePostRequest {

	public TagPostRequest(KintoneHost host) throws JSONException {
		super(host, host.getTagMaster());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		String customerTagId = csvRecord.get("customer_tag_id");
		String customerTagName = csvRecord.get("customer_tag_name");

		JSONObject jsonObj = new JSONObject();
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CustomerTagId", customerTagId);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CustomerTagName", customerTagName);

		super.put(jsonObj);
	}

}
