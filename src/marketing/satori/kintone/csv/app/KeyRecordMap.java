package marketing.satori.kintone.csv.app;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.Utils;
import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 *
 * キーとレコードのマッピングを取得、保持します。
 *
 */
public class KeyRecordMap {

	/** 重複キーの区切り文字 */
	protected static final String SEPARATOR = "|";

	private KintoneHost host;

	/** アプリケーション設定 */
	private KintoneApplication app;

	private String[] keyNameList;

	/** キーとレコードのマッピング */
	protected Map<String, Map<String, String>> map;

	/**
	 * アプリケーション設定よりオブジェクトを作成します。
	 * @param appConf
	 * @throws MalformedURLException
	 */
	public KeyRecordMap(KintoneHost host, KintoneApplication app, String[] keyNameList) {
		this.host = host;
		this.app = app;
		this.keyNameList = keyNameList;
	}


	public KeyRecordMap(KintoneHost host, KintoneApplication app) {
		this.host = host;
		this.app = app;
		this.keyNameList = app.getAppKeyFields();
	}

	/**
	 * 引数で指定されたキー値とレコードIDの対応をロードします。
	 * @param keyNameList キー項目のフィールドIDのリスト
	 * @param targetFieldNameList 保持するフィールド値
	 * @throws JSONException JSONオブジェクトの読取でエラーが発生した場合
	 * @throws IOException サーバとの通信でエラーが発生した場合
	 */
	public void loadKeyRecordMap(String[] targetFieldNameList) throws JSONException, IOException {
		String[] arr = (String[]) ArrayUtils.addAll(this.keyNameList, targetFieldNameList);
		KintoneGetRequest getRequest = new KintoneGetRequest(this.host, this.app, arr);
		HttpURLConnection http = getRequest.apply();

		this.map = new HashMap<String, Map<String, String>>();
		int count = 0;

		// オフセットをずらしながらレコードを取得し、キーとレコードIDとの対応を取得する。
		while (true) {
			JSONObject responsJson = Utils.httpURLConnectionToJson(http);
			JSONArray recordArray = responsJson.getJSONArray("records");

			// 取得レコードが無くなるとループを抜ける。
			if (recordArray.length() == 0) {
				break;
			}
			for (int i = 0; i < recordArray.length(); i++) {
				JSONObject record = recordArray.getJSONObject(i);

				StringJoiner sj = new StringJoiner(SEPARATOR);
				for (String keyName : keyNameList) {
					sj.add(record.getJSONObject(keyName).getString("value"));
				}

				Map<String, String> value = new HashMap<String, String>();
				for (String fieldName : targetFieldNameList) {
					if (!record.isNull(fieldName)) {
					    value.put(fieldName, record.getJSONObject(fieldName).getString("value"));
					}
				}

				map.put(sj.toString(), value);
				count++;
			}

			getRequest.setOffset(count);
			http = getRequest.apply();
		}
	}

	public boolean containsKey(String[] keyValueList) {
		return this.map.containsKey(
				arrayToSeparatedString(keyValueList));
	}

	/**
	 * キーを指定してマップのオブジェクトを取得します。
	 * @param keyValueList キーのリスト
	 * @return マップのオブジェクト
	 */
	public Map<String, String> getRecord(String[] keyValueList) {
		return this.map.get(
				arrayToSeparatedString(keyValueList));
	}

	public static String arrayToSeparatedString(String[] expr) {
		StringJoiner sj = new StringJoiner(SEPARATOR);
		for (String keyValue : expr) {
			sj.add(keyValue);
		}
		return sj.toString();
	}

	public KintoneApplication getApp() {
		return this.app;
	}
}
