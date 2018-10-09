package marketing.satori.kintone.csv.app.ccfield;

import java.util.HashMap;
import java.util.Iterator;
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
public class CcFieldPostRequest extends KintonePostRequest {

	KintoneHost host = null;

	public CcFieldPostRequest(KintoneHost host) throws JSONException {
		super(host, host.getCcFieldMaster());
		this.host = host;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		String customerCustomFieldId = csvRecord.get("customer_custom_field_id");
		String name = csvRecord.get("name");
		String description = csvRecord.get("description");
		String fieldName = csvRecord.get("field_name");
		String fieldKind = csvRecord.get("field_kind");
		String validationRule = csvRecord.get("validation_rule");
		String validationValue = csvRecord.get("validation_value");
		String uniqueField = csvRecord.get("unique_field");

		// Satori、kintoneのフィールドMAP作成
		JSONObject jsonMap = host.getFieldMap();

		Map<String,String> fieldMap = new HashMap<String, String>();
		Iterator<?> keyset = null;
		if (jsonMap != null) {
			keyset = jsonMap.keys();
		    while (keyset.hasNext()) {
                String mapKey =  (String) keyset.next();
                String mapValue = jsonMap.getString(mapKey);
                //System.out.print("\n mapKey : "+mapKey);
                fieldMap.put(mapKey, mapValue);
            }
		}

		JSONObject jsonObj = new JSONObject();
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CustomerCustomFieldId", customerCustomFieldId);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Name", name);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Description", description);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "FieldName", fieldMap.get(fieldName));
		KintoneRequest.addJsonValueRecordItem(jsonObj, "FieldKind", fieldKind);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "ValidationRule", validationRule);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "ValidationValue", validationValue);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "UniqueField", uniqueField);

		super.put(jsonObj);
	}

}
