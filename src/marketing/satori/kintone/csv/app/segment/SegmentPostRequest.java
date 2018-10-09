package marketing.satori.kintone.csv.app.segment;

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
public class SegmentPostRequest extends KintonePostRequest {

	public SegmentPostRequest(KintoneHost host) throws JSONException {
		super(host, host.getSegment());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		String segmentId = csvRecord.get("segment_id");
		String segmentName = csvRecord.get("segment_name");

		JSONObject jsonObj = new JSONObject();
		KintoneRequest.addJsonValueRecordItem(jsonObj, "SegmentId", segmentId);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "SegmentName", segmentName);

		super.put(jsonObj);
	}

}
