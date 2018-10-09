package marketing.satori.kintone.csv.app.wrsegment;

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
 * Web行動履歴(Segment該当)へのPOSTリクエストです。
 *
 */
public class WRSegmentPostRequest extends KintonePostRequest {

	/** マスタデータ */
	KeyRecordMap masterMap;

	public WRSegmentPostRequest(KintoneHost host, KeyRecordMap masterMap) throws JSONException {
		super(host, host.getWRSegment());
		this.masterMap = masterMap;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		String leadIdentity = csvRecord.get("lead_identity");
		String segmentId = csvRecord.get("segment_id");
		String datetime = Utils.csvDateStringToJsonDateString(csvRecord.get("datetime"),
				Config.getInstance().getKintoneHost().getTimezone());

		String[] keyNameList = masterMap.getApp().getCsvKeyColumns();
		String[] keyValueList = new String[keyNameList.length];
		for (int i = 0; i < keyNameList.length; i++) {
			keyValueList[i] = csvRecord.get(keyNameList[i]);
		}

		String segmentName = "";
		if (masterMap.containsKey(keyValueList)) {
			segmentName = masterMap.getRecord(keyValueList).get("SegmentName");
		}

		JSONObject jsonObj = new JSONObject();
		KintoneRequest.addJsonValueRecordItem(jsonObj, "LeadIdentity", leadIdentity);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "SegmentId", segmentId);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Datetime", datetime);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "SegmentName", segmentName);

		super.put(jsonObj);
	}

}
