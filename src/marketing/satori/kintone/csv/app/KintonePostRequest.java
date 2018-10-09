package marketing.satori.kintone.csv.app;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 *
 * KintoneアプリケーションへのPOSTリクエストを表します。
 *
 */
public abstract class KintonePostRequest extends KintoneRecordsRequest {
	public static final String METHOD = "POST";

	JSONObject jsonObject;
	JSONArray recordsJsonArary;

	public KintonePostRequest(KintoneHost host, KintoneApplication appConfig) throws JSONException {
		super(host, appConfig);
		this.jsonObject = new JSONObject();
		jsonObject.put("app", appConfig.getAppNo());
		this.recordsJsonArary = new JSONArray();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void put(Map<String, String> csvRecord) throws JSONException;


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void put(JSONObject jsonObj) {
		this.recordsJsonArary.put(jsonObj);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		this.recordsJsonArary = new JSONArray();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONObject toJsonObject() throws JSONException {
		jsonObject.put("records", this.recordsJsonArary);
		return jsonObject;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMethod() {
		return KintonePostRequest.METHOD;
	}

}
