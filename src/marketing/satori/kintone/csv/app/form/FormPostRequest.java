package marketing.satori.kintone.csv.app.form;

import java.util.Map;

import org.apache.struts.util.MessageResources;
import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.CsvExportBatch;
import marketing.satori.kintone.csv.Utils;
import marketing.satori.kintone.csv.app.KintonePostRequest;
import marketing.satori.kintone.csv.app.KintoneRequest;
import marketing.satori.kintone.csv.config.KintoneHost;
import marketing.satori.kintone.csv.log.Log;

/**
 *
 * SegmentへのPOSTリクエストです。
 *
 */
public class FormPostRequest extends KintonePostRequest {

	JSONObject fieldsObject = null;

	public FormPostRequest(KintoneHost host) throws JSONException {
		super(host, host.getForm());
	}

	public void setFiels(JSONObject fields) throws JSONException {
		fieldsObject = fields;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		MessageResources messageResources = Utils.getMessageResources();
		Log log = new Log(CsvExportBatch.class.getName());

		String formId = csvRecord.get("campaign_form_id");
		String formName = csvRecord.get("campaign_form_name");
		String extraParamJson = csvRecord.get("extra_param_json");

		JSONObject jsonObj = new JSONObject();
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CampaignFormId", formId);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CampaignFormName", formName);
		if (fieldsObject != null && !fieldsObject.isNull("ExtraParamJson")) {
		    KintoneRequest.addJsonValueRecordItem(jsonObj, "ExtraParamJson", extraParamJson);
		} else {
			String message = messageResources.getMessage("kintone.csv.batch.fieldskip.app", "hash値定義");
			log.info(0, message, "csv info");
		}

		super.put(jsonObj);
	}

}
