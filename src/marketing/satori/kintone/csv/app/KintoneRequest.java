package marketing.satori.kintone.csv.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.config.Config;
import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;
import marketing.satori.kintone.csv.log.Log;

/**
 * kintoneアプリケーションに対するREST API呼出しを表す抽象クラスです。
 */
public abstract class KintoneRequest {
	Log log = new Log(this.getClass().getName());

	private KintoneHost host;
	private KintoneApplication app;

	/** kintoneでの1回のリクエストで処理可能なレコード数の上限値 */
	public static int KINTONE_OPERATION_COUNT_LIMIT_BY_ONE = 100;

	/**
	 * オブジェクトを作成する。
	 * @param host ホスト設定
	 * @param app アプリ設定
	 */
	public KintoneRequest(KintoneHost host, KintoneApplication app) {
		this.host = host;
		this.app = app;
	}

	/**
	 * このオブジェクトによるREST API呼出しに用いるJSONオブジェクトを返します。
	 * @return REST API呼出しに用いるJSONオブジェクト
	 * @throws JSONException JSON文字列生成時にエラーが発生した場合
	 */
	public abstract JSONObject toJsonObject() throws JSONException;


	/**
	 * このオブジェクトによるREST API呼出しのメソッドを返します。
	 * @return REST API呼出しのメソッド
	 */
	public abstract String getMethod();

	/**
	 * KINTONEへリクエストを送信します。<BR>
	 * KintoneRequest.KINTONE_OPERATION_COUNT_LIMIT_BY_ONE 件ごとに呼び出される前提。
	 * @return KINTONEへのリクエスト結果
	 * @throws JSONException
	 * @throws IOException
	 */
	public HttpURLConnection apply() throws JSONException, IOException {
		URL url = new URL(this.getUrl());
		HttpURLConnection http = (HttpURLConnection) url.openConnection();

		// htmlメソッドはフォーム送信するためにPOSTにしておく。
		// kintoneメソッドの指定は[X-HTTP-Method-Override]で行う。
		http.setRequestMethod("POST");
		http.setDoOutput(true);
        http.setDoInput(true);
        http.setConnectTimeout(100000);
        http.setRequestProperty("Content-Type","application/json");
        http.setRequestProperty("X-HTTP-Method-Override",this.getMethod());

        Config config = Config.getInstance();
        KintoneHost host = config.getKintoneHost();
        String mngUserName = host.getManageUserName();
        String mngUserPass = host.getManageUserPassword();
        if (StringUtils.isEmpty(mngUserName) || StringUtils.isEmpty(mngUserPass)) {
        	// ユーザ名、パスワードが設定されていなければAPI Tokenを利用して接続する。
        	http.setRequestProperty("X-Cybozu-API-Token", this.app.getApiToken());
        	http.setRequestProperty("Authorization", "Basic " + this.app.getApiToken());
        } else {
        	// ユーザ名、パスワードが設定されていれば管理者認証を利用して接続する。
        	http.setRequestProperty("X-Cybozu-Authorization", host.getAuthorization());
        	http.setRequestProperty("Authorization", "Basic " + host.getAuthorization());
        }
        //String a = this.toJsonObject().toString();
        OutputStream oStreamQuery = http.getOutputStream();
        oStreamQuery.write(this.toJsonObject().toString().getBytes());

		http.connect();
		http.disconnect();

		if (http.getResponseCode() != HttpURLConnection.HTTP_OK) {
			System.out.println(http.getResponseCode());
			System.out.println(http.getResponseMessage());

			InputStream errStream = http.getErrorStream();
			if (errStream != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(errStream));
				StringBuffer sb = new StringBuffer();
				String line = "";
				while((line = br.readLine()) != null) {
					sb.append(line);
					sb.append("\n");
				}
				JSONObject json = this.toJsonObject();
				if (json != null) {
					sb.append(this.toJsonObject().toString());
				}
				log.error(http.getResponseCode(), sb.toString(), "kintone error");
			}

			InputStream iStream = http.getInputStream();
			if (iStream != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
				StringBuffer sb = new StringBuffer();
				String line = "";
				while((line = br.readLine()) != null) {
					sb.append(line);
					sb.append("\n");
				}
				JSONObject json = this.toJsonObject();
				if (json != null) {
					sb.append(this.toJsonObject().toString());
				}
				log.error(http.getResponseCode(), sb.toString(), "kintone error");
			}
		}

		return http;
	}


	/**
	 * キー項目とIDのマップより、引数のレコードのレコードIDを探索し返します。<BR>
	 * 更新時、削除時に利用する想定です。
	 * @param keyIdMap
	 * @param keyNameList
	 * @param recordObj
	 * @return
	 */
	public static int getId(KeyRecordIdMap keyRecordIdMap, String[] keyNameList, Map<String, String> csvRecord) {
		String[] keyValueList = new String[keyNameList.length];
		for (int i = 0; i < keyNameList.length; i++) {
			keyValueList[i] = csvRecord.get(keyNameList[i]);
		}

		return keyRecordIdMap.getId(keyValueList);
	}

	/**
	 * このオブジェクトのホスト設定を返します。
	 * @return このオブジェクトのホスト設定
	 */
	protected KintoneHost getHost() {
		return this.host;
	}

	/**
	 * このオブジェクトのアプリケーション設定を返します。
	 * @return このオブジェクトのアプリケーション設定
	 */
	protected KintoneApplication getApp() {
		return this.app;
	}

	/**
	 * リクエストのURLを返す。
	 * @return リクエストのURL
	 */
	abstract protected String getUrl();

	protected static <T> void addJsonValueRecordItem(JSONObject jsonOBj, String key, T value) throws JSONException {
		jsonOBj.put(key, new JSONObject().put("value", value));
	}

	protected static void addJsonArrayRecordItem(JSONObject jsonOBj, String key, String[] array) throws JSONException {
		jsonOBj.put(key, new JSONObject().put("value", new JSONArray(array)));
	}
}
