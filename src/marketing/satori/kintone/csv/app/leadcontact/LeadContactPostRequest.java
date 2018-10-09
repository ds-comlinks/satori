package marketing.satori.kintone.csv.app.leadcontact;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.util.MessageResources;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.CsvExportBatch;
import marketing.satori.kintone.csv.Utils;
import marketing.satori.kintone.csv.app.KintonePostRequest;
import marketing.satori.kintone.csv.app.KintoneRequest;
import marketing.satori.kintone.csv.config.KintoneHost;
import marketing.satori.kintone.csv.config.S3Config;
import marketing.satori.kintone.csv.log.Log;

/**
 *
 * Lead、ContactへのPOSTリクエストです。
 *
 */
public class LeadContactPostRequest extends KintonePostRequest {

	JSONObject fieldsObject = null;
	KintoneHost host = null;

	public LeadContactPostRequest(KintoneHost host) throws JSONException {
		super(host, host.getLeadContact());
		this.host = host;
	}

	public void setFiels(JSONObject fields) throws JSONException {
		fieldsObject = fields;
	}

	/**
	 * {@inheritDoc}
	 * @throws ParseException
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		MessageResources messageResources = Utils.getMessageResources();
		Log log = new Log(CsvExportBatch.class.getName());

		String leadIdentity = csvRecord.get("lead_identity");
		String hashcode = csvRecord.get("hashcode");
		String email = csvRecord.get("email");
		String leadCompanyName = csvRecord.get("lead_company_name");
		String department = csvRecord.get("department");
		String position = csvRecord.get("position");
		String phoneNumber = csvRecord.get("phone_number");
		String mobilePhoneNumber = csvRecord.get("mobile_phone_number");

		String deliveryPermission = csvRecord.get("delivery_permission");
		String lastName = csvRecord.get("last_name");
		String firstName = csvRecord.get("first_name");
		String webSite = csvRecord.get("web_site");
		String address = csvRecord.get("address");
		String collectionRoute = csvRecord.get("collection_route");
		String collectionDate = csvRecord.get("collection_date");

		String statusTmp = csvRecord.get("status");
		String status = "";
		if (statusTmp.equals("regist")) {
			status = "未対応";
		} else if (statusTmp.equals("reply")) {
			status = "返信済";
		} else if (statusTmp.equals("phone")) {
			status = "電話済";
		} else if (statusTmp.equals("visit")) {
			status = "訪問済";
		} else if (statusTmp.equals("contract")) {
			status = "契約済";
		} else {
			status = "未対応";
		}

		String id = csvRecord.get("id");
		String score = csvRecord.get("score");
		String joined_tags = csvRecord.get("joined_tags");
		String memo = csvRecord.get("memo");
		//memo = memo.replaceAll("\\\\n", "\r\n");
		memo = memo.replaceAll("\\\\n", "\n");

		if (StringUtils.isEmpty(leadIdentity)) {
			leadIdentity = Utils.toSha256(String.valueOf(S3Config.getInstance().getCompanyId()), email);
		}

		JSONObject jsonObj = new JSONObject();
		KintoneRequest.addJsonValueRecordItem(jsonObj, "LeadIdentity", leadIdentity);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "HashCode", hashcode);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Email", email);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Company", leadCompanyName);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Department", department);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Title", position);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Phone", phoneNumber);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "MobilePhone", mobilePhoneNumber);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "HasOptedOutOfEmail", deliveryPermission);

		KintoneRequest.addJsonValueRecordItem(jsonObj, "LastName", lastName);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "FirstName", firstName);

		// URLはパースを行えた場合にURL項目に値設定し、失敗した場合はテキスト項目に値設定する。
		if (StringUtils.isEmpty(webSite)) {
			KintoneRequest.addJsonValueRecordItem(jsonObj, "Website", "");
			KintoneRequest.addJsonValueRecordItem(jsonObj, "WebsiteText", "");
		} else if (Utils.isUrlString(webSite)) {
			try {
				new URL(webSite);
				KintoneRequest.addJsonValueRecordItem(jsonObj, "Website", webSite);
				KintoneRequest.addJsonValueRecordItem(jsonObj, "WebsiteText", "");
			} catch (MalformedURLException e) {
				KintoneRequest.addJsonValueRecordItem(jsonObj, "Website", "");
				KintoneRequest.addJsonValueRecordItem(jsonObj, "WebsiteText", webSite);
			}
		} else {
			KintoneRequest.addJsonValueRecordItem(jsonObj, "Website", "");
			KintoneRequest.addJsonValueRecordItem(jsonObj, "WebsiteText", webSite);
		}

		KintoneRequest.addJsonValueRecordItem(jsonObj, "Address", address);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CollectionRoute", collectionRoute);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "CollectionDate", collectionDate);
		KintoneRequest.addJsonValueRecordItem(jsonObj, "Status", status);

		if (fieldsObject != null && !fieldsObject.isNull("Id")) {
		    KintoneRequest.addJsonValueRecordItem(jsonObj, "Id", id);
		} else {
			String message = messageResources.getMessage("kintone.csv.batch.fieldskip.app", "カスタマーID");
			log.info(0, message, "csv info");
	    }
		if (fieldsObject != null && !fieldsObject.isNull("Score")) {
		    KintoneRequest.addJsonValueRecordItem(jsonObj, "Score", score);
		} else {
			String message = messageResources.getMessage("kintone.csv.batch.fieldskip.app", "スコア");
			log.info(0, message, "csv info");
	    }
		//KintoneRequest.addJsonValueRecordItem(jsonObj, "JoinedTags", joined_tags);
		if (fieldsObject != null && !fieldsObject.isNull("Memo")) {
		    KintoneRequest.addJsonValueRecordItem(jsonObj, "Memo", memo);
		} else {
			String message = messageResources.getMessage("kintone.csv.batch.fieldskip.app", "メモ");
			log.info(0, message, "csv info");
	    }
		if (joined_tags != null) {
		    String[] tagName = joined_tags.split(",");
		    JSONObject[] tags = new JSONObject[0];

		    if (!joined_tags.equals("")) {
		        tags = new JSONObject[tagName.length];
		        for (int i = 0; i < tagName.length; i++){
  		            JSONObject sObj = new JSONObject();
		            JSONObject sObj2 = new JSONObject();
		            JSONObject sObj3 = new JSONObject();
		            sObj3.put("value", tagName[i]);
		            sObj2.put("customer_tag_name", sObj3);
		            sObj.put("value", sObj2);
		            tags[i] = sObj;
		        }
		    }
		    if (fieldsObject != null && !fieldsObject.isNull("JoinedTagsTable")) {
       	        KintoneRequest.addJsonValueRecordItem(jsonObj, "JoinedTagsTable", tags);
		    } else {
				String message = messageResources.getMessage("kintone.csv.batch.fieldskip.app", "タグ集合文字列");
				log.info(0, message, "csv info");
		    }
	    }

		// Satori、kintoneのフィールドMAP作成
		JSONObject jsonObject = host.getFieldMap();

		Map<String,String> fieldMap = new HashMap<String, String>();
		Iterator<?> keyset = null;
		if (jsonObject != null) {
			keyset = jsonObject.keys();
		    while (keyset.hasNext()) {
                String mapKey =  (String) keyset.next();
                String mapValue = jsonObject.getString(mapKey);
                //System.out.print("\n mapKey : "+mapKey);
                fieldMap.put(mapKey, mapValue);
            }
		}
		// カスタマーカスタム項目の取り出しと設定
		for(Map.Entry<String, String> entry : csvRecord.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			// カスタマーカスタム項目か
			if (key.contains("customer_custom_field__")) {
				String satorif = key.replace("customer_custom_field__", "");
				String kintonef = "customer_custom_field__";
				if (fieldMap.get(satorif) != null) {
					kintonef = kintonef + fieldMap.get(satorif);
				}
				if (fieldsObject != null && !fieldsObject.isNull(kintonef)) {
					//value = value.replaceAll("\\\\n", "\r\n");
					value = value.replaceAll("\\\\n", "\n");
				    KintoneRequest.addJsonValueRecordItem(jsonObj, kintonef, value);
			    } else {
					String message = messageResources.getMessage("kintone.csv.batch.fieldskip.app", key);
					log.info(0, message, "csv info");
			    }
			}
		}

		jsonObj.put("OwnerId",
				new JSONObject().put(
						"value", new JSONArray().put(
								new JSONObject().put("code", this.getHost().getSatoriUser()))));

		super.put(jsonObj);

	}

}
