package marketing.satori.kintone.csv.config;

import org.json.JSONException;
import org.json.JSONObject;

public class KintoneApplication {
	/** APP No */
	private int appNo;

	/** API Token */
	private String apiToken;

	/** CSVのキーカラム名 */
	private String[] csvKeyColumns;

	/** アプリケーションのキーフィールド名 */
	private String[] appKeyFields;

	public KintoneApplication(JSONObject json, String[] csvKeyColumns, String[] appKeyFields) throws JSONException {

        this.appNo = json.getInt("appNo");
	    this.apiToken = json.getString("apiToken");
	    this.csvKeyColumns = csvKeyColumns;
	    this.appKeyFields = appKeyFields;
	}

	/**
	 * APP Noを返します。
	 * @return appNo APP No
	 */
	public int getAppNo() {
		return appNo;
	}

	/**
	 * @return apiToken
	 */
	public String getApiToken() {
		return apiToken;
	}

	/**
	 * @return csvKeyColumns
	 */
	public String[] getCsvKeyColumns() {
		return csvKeyColumns;
	}

	/**
	 * @return appKeyFields
	 */
	public String[] getAppKeyFields() {
		return appKeyFields;
	}


}
