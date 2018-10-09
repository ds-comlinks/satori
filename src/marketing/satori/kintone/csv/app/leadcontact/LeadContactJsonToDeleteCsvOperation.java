package marketing.satori.kintone.csv.app.leadcontact;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.Utils;
import marketing.satori.kintone.csv.app.KintoneJsonToCsvOperation;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 *
 * リード、コンタクトのJSON文字列をCSVに変換する操作です。
 *
 */
public class LeadContactJsonToDeleteCsvOperation extends KintoneJsonToCsvOperation {
	static List<String> columnNameList = new ArrayList<String>();
	static {
		columnNameList.add("lead_identity");
		columnNameList.add("email");
		columnNameList.add("datetime");
	}

	public LeadContactJsonToDeleteCsvOperation(KintoneHost host) {
		super(host, host.getLeadContact());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String jsonToCsv(JSONObject record) throws JSONException {
		List<String> csvColumnList = new ArrayList<String>();
		csvColumnList.add(Utils.getJSONValueString(record, "LeadIdentity"));
		csvColumnList.add(Utils.getJSONValueString(record, "Email"));
		csvColumnList.add(
				Utils.jsonDateStringToCsvDateString(
						Utils.getJSONValueString(record, "DeletedDateTime"), this.getHost().getTimezone()));

		return Utils.listToCsvString(csvColumnList);
	}

	public String[] jsonToList(JSONObject record) throws JSONException {
		List<String> csvColumnList = new ArrayList<String>();
		csvColumnList.add(Utils.getJSONValueString(record, "LeadIdentity"));
		csvColumnList.add(Utils.getJSONValueString(record, "Email"));
		csvColumnList.add(
				Utils.jsonDateStringToCsvDateString(
						Utils.getJSONValueString(record, "DeletedDateTime"), this.getHost().getTimezone()));

		return  csvColumnList.toArray(new String[csvColumnList.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFileName() {
		return "customer_delete";
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
	}
}
