package marketing.satori.kintone.csv.app;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 *
 * KintoneアプリケーションへのPUTリクエストを表します。
 *
 */
public abstract class KintonePutRequest extends KintoneRecordsRequest {
	public static final String METHOD = "PUT";

	private JSONObject jsonObject;
	private JSONArray recordsJsonArary;
	private KeyRecordIdMap keyIdMap;

	public KintonePutRequest(KintoneHost host, KintoneApplication app) throws JSONException {
		super(host, app);
		jsonObject = new JSONObject();
		jsonObject.put("app", app.getAppNo());
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
	protected void put(JSONObject recordObj) {
		recordsJsonArary.put(recordObj);
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
		return KintonePutRequest.METHOD;
	}


	/**
	 * このオブジェクトに対応するアプリケーションの、キー項目とレコードIDのマッピングを返します。
	 * @return キー項目とレコードIDのマッピング
	 */
	protected KeyRecordIdMap getKeyIdMap() {
		return keyIdMap;
	}

}
