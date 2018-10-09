package marketing.satori.kintone.csv.app.leadcontact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.util.MessageResources;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.CsvExportBatch;
import marketing.satori.kintone.csv.Utils;
import marketing.satori.kintone.csv.app.KintoneJsonToCsvOperation;
import marketing.satori.kintone.csv.config.KintoneHost;
import marketing.satori.kintone.csv.log.Log;

/**
 *
 * リード、コンタクトのJSON文字列をCSVに変換する操作です。
 *
 */
public class LeadContactJsonToCsvOperation extends KintoneJsonToCsvOperation {
	static List<String> columnNameList = new ArrayList<String>();
	static {
		columnNameList.add("lead_identity");
		columnNameList.add("hashcode");
		columnNameList.add("email");
		columnNameList.add("lead_company_name");
		columnNameList.add("department");
		columnNameList.add("position");
		columnNameList.add("phone_number");
		columnNameList.add("mobile_phone_number");
		columnNameList.add("delivery_permission");
		columnNameList.add("last_name");
		columnNameList.add("first_name");
		columnNameList.add("web_site");
		columnNameList.add("address");
		columnNameList.add("collection_route");
		columnNameList.add("collection_date");
		columnNameList.add("status");

		//columnNameList.add("id");
		//columnNameList.add("score");
		//columnNameList.add("joined_tags");
		//columnNameList.add("memo");
	}
	static ArrayList<String> addTitle = new ArrayList<String>();
	KintoneHost host = null;

	public LeadContactJsonToCsvOperation(KintoneHost host) {
		super(host, host.getLeadContact());
		this.host = host;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String jsonToCsv(JSONObject record) throws JSONException {
		MessageResources messageResources = Utils.getMessageResources();
		Log log = new Log(CsvExportBatch.class.getName());

		List<String> csvColumnList = new ArrayList<String>();
		csvColumnList.add(Utils.getJSONValueString(record, "LeadIdentity"));
		csvColumnList.add(Utils.getJSONValueString(record, "HashCode"));
		csvColumnList.add(Utils.getJSONValueString(record, "Email"));
		csvColumnList.add(Utils.getJSONValueString(record, "Company"));
		csvColumnList.add(Utils.getJSONValueString(record, "Department"));
		csvColumnList.add(Utils.getJSONValueString(record, "Title"));
		csvColumnList.add(Utils.getJSONValueString(record, "Phone"));
		csvColumnList.add(Utils.getJSONValueString(record, "MobilePhone"));
		csvColumnList.add(Utils.getJSONValueString(record, "HasOptedOutOfEmail"));
		csvColumnList.add(Utils.getJSONValueString(record, "LastName"));
		csvColumnList.add(Utils.getJSONValueString(record, "FirstName"));

		String webSite = Utils.getJSONValueString(record, "Website");
		if (StringUtils.isEmpty(webSite)) {
			webSite = Utils.getJSONValueString(record, "WebsiteText");
		}
		csvColumnList.add(webSite);
		csvColumnList.add(Utils.getJSONValueString(record, "Address"));
		csvColumnList.add(Utils.getJSONValueString(record, "CollectionRoute"));
		csvColumnList.add(Utils.getJSONValueString(record, "CollectionDate"));
		{
			String tmp = Utils.getJSONValueString(record, "Status");
			String val = "";
			if (tmp.equals("未対応")) {
				val = "regist";
			} else if (tmp.equals("返信済")) {
				val = "reply";
			} else if (tmp.equals("電話済")) {
				val = "phone";
			} else if (tmp.equals("訪問済")) {
				val = "visit";
			} else if (tmp.equals("契約済")) {
				val = "contract";
			}
			csvColumnList.add(val);
		}

		//csvColumnList.add(Utils.getJSONValueString(record, "Id"));
		//csvColumnList.add(Utils.getJSONValueString(record, "Score"));
		String tags = "";
		if (!record.isNull("JoinedTagsTable")) {
		    JSONObject sObj = Utils.getJSONValueObject(record, "JoinedTagsTable");
		    JSONArray sObj2 = sObj.getJSONArray("value");
		    for (int i = 0; i < sObj2.length(); i++) {
		        JSONObject sObj3 = sObj2.getJSONObject(i);
		        JSONObject sObj4 = sObj3.getJSONObject("value");
		        String tag = Utils.getJSONValueString(sObj4, "customer_tag_name");
		        if (i != 0) {
			        tags = tags + ",";
		        }
		        tags = tags + tag;
		    }
			csvColumnList.add(tags);
		} else {
			String message = messageResources.getMessage("kintone.csv.batch.fieldskipExp.app", "タグ集合文字列");
			log.info(0, message, "csv info");
		}

		if (!record.isNull("Memo")) {
		    csvColumnList.add(Utils.getJSONValueString(record, "Memo"));
		} else {
			String message = messageResources.getMessage("kintone.csv.batch.fieldskipExp.app", "メモ");
			log.info(0, message, "csv info");
		}

		// カスタマーカスタム項目の処理
		if (addTitle.size() > 0) {
		    for (int i = 0; i < addTitle.size(); i++) {
			    String objType =  Utils.getJSONValueType(record, addTitle.get(i));
			    if (objType.equals("DATE")) {
				    String inDate = Utils.getJSONValueString(record, addTitle.get(i)) + "T00:00:01Z";
				    //String dateTimeStr = Utils.jsonDateStringToCsvDateString(inDate, this.getHost().getTimezone());
				    String dateTimeStr = Utils.jsonDateStringToCsvDateString(inDate, "Etc/GMT");
				    String[] dateTimeAry = dateTimeStr.split(" ");
				    csvColumnList.add(dateTimeAry[0]);
			    } else {
                    csvColumnList.add(Utils.getJSONValueString(record, addTitle.get(i)));
			    }

            }
		} else {
			String message = messageResources.getMessage("kintone.csv.batch.fieldskipExp.app", "カスタマーカスタム項目");
			log.info(0, message, "csv info");
		}

		return Utils.listToCsvString(csvColumnList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] jsonToList(JSONObject record) throws JSONException {
		MessageResources messageResources = Utils.getMessageResources();
		Log log = new Log(CsvExportBatch.class.getName());

		List<String> csvColumnList = new ArrayList<String>();
		csvColumnList.add(Utils.getJSONValueString(record, "LeadIdentity"));
		csvColumnList.add(Utils.getJSONValueString(record, "HashCode"));
		csvColumnList.add(Utils.getJSONValueString(record, "Email"));
		csvColumnList.add(Utils.getJSONValueString(record, "Company"));
		csvColumnList.add(Utils.getJSONValueString(record, "Department"));
		csvColumnList.add(Utils.getJSONValueString(record, "Title"));
		csvColumnList.add(Utils.getJSONValueString(record, "Phone"));
		csvColumnList.add(Utils.getJSONValueString(record, "MobilePhone"));
		csvColumnList.add(Utils.getJSONValueString(record, "HasOptedOutOfEmail"));
		csvColumnList.add(Utils.getJSONValueString(record, "LastName"));
		csvColumnList.add(Utils.getJSONValueString(record, "FirstName"));

		String webSite = Utils.getJSONValueString(record, "Website");
		if (StringUtils.isEmpty(webSite)) {
			webSite = Utils.getJSONValueString(record, "WebsiteText");
		}
		csvColumnList.add(webSite);
		csvColumnList.add(Utils.getJSONValueString(record, "Address"));
		csvColumnList.add(Utils.getJSONValueString(record, "CollectionRoute"));
		csvColumnList.add(Utils.getJSONValueString(record, "CollectionDate"));
		{
			String tmp = Utils.getJSONValueString(record, "Status");
			String val = "";
			if (tmp.equals("未対応")) {
				val = "regist";
			} else if (tmp.equals("返信済")) {
				val = "reply";
			} else if (tmp.equals("電話済")) {
				val = "phone";
			} else if (tmp.equals("訪問済")) {
				val = "visit";
			} else if (tmp.equals("契約済")) {
				val = "contract";
			}
			csvColumnList.add(val);
		}

		//csvColumnList.add(Utils.getJSONValueString(record, "Id"));
		//csvColumnList.add(Utils.getJSONValueString(record, "Score"));
		String tags = "";
		if (!record.isNull("JoinedTagsTable")) {
		    JSONObject sObj = Utils.getJSONValueObject(record, "JoinedTagsTable");
		    JSONArray sObj2 = sObj.getJSONArray("value");
		    for (int i = 0; i < sObj2.length(); i++) {
		        JSONObject sObj3 = sObj2.getJSONObject(i);
		        JSONObject sObj4 = sObj3.getJSONObject("value");
		        String tag = Utils.getJSONValueString(sObj4, "customer_tag_name");
		        if (i != 0) {
			        tags = tags + ",";
		        }
		        tags = tags + tag;
		    }
			csvColumnList.add(tags);
		} else {
			String message = messageResources.getMessage("kintone.csv.batch.fieldskipExp.app", "タグ集合文字列");
			log.info(0, message, "csv info");
		}

		if (!record.isNull("Memo")) {
		    csvColumnList.add(Utils.getJSONValueString(record, "Memo"));
		} else {
			String message = messageResources.getMessage("kintone.csv.batch.fieldskipExp.app", "メモ");
			log.info(0, message, "csv info");
		}

		// カスタマーカスタム項目の処理
		if (addTitle.size() > 0) {
		    for (int i = 0; i < addTitle.size(); i++) {
			    String objType =  Utils.getJSONValueType(record, addTitle.get(i));
			    if (objType.equals("DATE")) {
			    	String inDate = null;
			    	if (Utils.getJSONValueString(record, addTitle.get(i)).isEmpty()) {
			    		 inDate = Utils.getJSONValueString(record, addTitle.get(i)) ;
			    	} else {
				        inDate = Utils.getJSONValueString(record, addTitle.get(i)) + "T00:00:01Z";
			    	}
				    //String dateTimeStr = Utils.jsonDateStringToCsvDateString(inDate, this.getHost().getTimezone());
				    String dateTimeStr = Utils.jsonDateStringToCsvDateString(inDate, "Etc/GMT");
				    String[] dateTimeAry = dateTimeStr.split(" ");
				    csvColumnList.add(dateTimeAry[0]);
			    } else {
                    csvColumnList.add(Utils.getJSONValueString(record, addTitle.get(i)));
			    }

            }
		} else {
			String message = messageResources.getMessage("kintone.csv.batch.fieldskipExp.app", "カスタマーカスタム項目");
			log.info(0, message, "csv info");
		}

		return csvColumnList.toArray(new String[csvColumnList.size()]);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFileName() {
		return "customer";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<String> getColumnNameList() {
		return columnNameList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addTitle(Iterator<?> keys) {

		List<String> wkList =  new ArrayList<String>();
		while(keys.hasNext()){
		    String key = (String)keys.next();
		    wkList.add(key);
		}

		Iterator<?> tmpkeys = wkList.iterator();
		while(tmpkeys.hasNext()){
		    String key = (String)tmpkeys.next();
			if (key.equals("JoinedTagsTable")) {
				//columnNameList.add(key);
				columnNameList.add("joined_tags");
			}
		}
		tmpkeys = wkList.iterator();
		while(tmpkeys.hasNext()){
		    String key = (String)tmpkeys.next();
			if (key.equals("Memo")) {
				//columnNameList.add(key);
				columnNameList.add("memo");
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
                String mapValue = "";;
				try {
					mapValue = jsonObject.getString(mapKey);
				} catch (JSONException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
                //System.out.print("\n mapKey : "+mapKey);
                fieldMap.put(mapValue, mapKey);
            }
		}
		tmpkeys = wkList.iterator();
		while(tmpkeys.hasNext()){
		    String key = (String)tmpkeys.next();
		    //String value = obj.getString(key);
			// カスタマーカスタム項目か
			if (key.contains("customer_custom_field__")) {
				String kintonef = key.replace("customer_custom_field__", "");
				String satorif = "customer_custom_field__";
				if (fieldMap.get(kintonef) != null) {
					satorif = satorif + fieldMap.get(kintonef);
					columnNameList.add(satorif);
					addTitle.add(key);
				}
			}
		}
	}
}
