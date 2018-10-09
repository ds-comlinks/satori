package marketing.satori.kintone.csv.app;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 *
 * KintoneアプリケーションへのDELETEリクエストを表します。
 *
 */
public class KintoneDeleteRequest extends KintoneRecordsRequest {
	public static final String METHOD = "DELETE";

	/** リクエストに利用するJSONオブジェクト */
	private JSONObject jsonObject;

	/** キーとレコードIDのマップ */
	private KeyRecordIdMap keyIdMap;

	/** 対象レコードIDのJSON配列 */
	private JSONArray idsArary;

	/**
	 * アプリケーション設定とキーとレコードIDのマップより、オブジェクトを作成します。
	 * @param appConfig
	 * @param keyRecordMap
	 * @throws JSONException
	 */
	public KintoneDeleteRequest(KintoneHost host, KintoneApplication app, KeyRecordIdMap keyRecordMap) throws JSONException {
		super(host, app);
		this.jsonObject = new JSONObject();
		this.jsonObject.put("app", app.getAppNo());
		this.keyIdMap = keyRecordMap;
		this.idsArary = new JSONArray();
	}

	/**
	 * CSVレコード中のキー項目より対象レコードのレコードIDを取得し、このオブジェクトの操作対象としてレコードIDを登録します。
	 * @param recordObj 操作対象レコードが格納されたCSVRecordオブジェクト
	 * @throws JSONException CSVレコードからJSONObjectへの変換でエラーが発生した場合
	 */
	public void put(int id) throws JSONException {
		this.idsArary.put(id);
	}


	/**
	 * JSONObjectを、このオブジェクトの操作対象となるレコードとして登録します。
	 * @param recordObj 操作対象レコードが格納されたJSONObjectオブジェクト
	 */
	@Override
	protected void put(JSONObject recordObj) {
		idsArary.put(recordObj);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		this.idsArary = new JSONArray();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONObject toJsonObject() throws JSONException {
		jsonObject.put("ids", this.idsArary);
		return jsonObject;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMethod() {
		return KintoneDeleteRequest.METHOD;
	}


	/**
	 * このオブジェクトに対応するアプリケーションの、キー項目とレコードIDのマッピングを返します。
	 * @return キー項目とレコードIDのマッピング
	 */
	protected KeyRecordIdMap getKeyIdMap() {
		return keyIdMap;
	}

	/**
	 * @deprecated 使用を想定していないため空の処理として実装されています。
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {

	}

}
