package marketing.satori.kintone.csv.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.config.Config;
import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 *
 * KintoneアプリケーションへのGetリクエストを表します。
 *
 */
public class KintoneGetRequest extends KintoneRecordsRequest {
	public static final String METHOD = "GET";
	JSONObject jsonObject;

	String condition;
	String orderBy;
	Integer limit;
	Integer offset;

	/**
	 * アプリケーション設定よりオブジェクトを作成します。
	 * 全項目を取得します。
	 * @param appConfig
	 * @throws JSONException
	 */
	public KintoneGetRequest(KintoneHost host, KintoneApplication app) throws JSONException {
		this(host, app, new String[]{});
	}

	/**
	 * アプリケーション設定より
	 * @param appConfig
	 * @param fields <BR>中身が無い場合全項目を取得します。
	 * @throws JSONException
	 */
	public KintoneGetRequest(KintoneHost host, KintoneApplication app, String[] fields) throws JSONException {
		super(host, app);
		jsonObject = new JSONObject();
		jsonObject.put("app", app.getAppNo());

		JSONArray fieldsArray = new JSONArray();

		if (fields.length > 0) {
			for (int i = 0; i < fields.length; i++) {
				fieldsArray.put(fields[i]);
			}
			jsonObject.put("fields", fieldsArray);
		}
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * レコード取得のoffsetを変更します。
	 * @param offset
	 * @throws JSONException
	 */
	public void setOffset(int offset) throws JSONException {
		this.offset = offset;
	}

	public String getQueryString() {
		StringBuilder builder = new StringBuilder();

		if (!StringUtils.isEmpty(condition)) {
			builder.append(condition);
		}

		if (!StringUtils.isEmpty(orderBy)) {
			builder.append("order by ").append(orderBy);
		}

		if (limit != null) {
			builder.append("limit ").append(limit);
		}

		if (offset != null) {
			builder.append("offset ").append(offset);
		}

		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONObject toJsonObject() throws JSONException {
		String queryStr = getQueryString();
		jsonObject.put("query", queryStr);
		return jsonObject;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMethod() {
		return KintoneGetRequest.METHOD;
	}


	/**
	 * @deprecated 使用を想定していないため空の処理として実装されています。
	 */
	@Override
	public void put(Map<String, String> csvRecord) throws JSONException {
		// 使用しない。
	}


	/**
	 * @deprecated 使用を想定していないため空の処理として実装されています。
	 */
	@Override
	public void put(JSONObject recordObj) {
		// 使用しない。
	}


	/**
	 * @deprecated 使用を想定していないため空の処理として実装されています。
	 */
	@Override
	public void clear() {
		// 使用しない。
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpURLConnection apply() throws JSONException, IOException {
		StringBuilder query = new StringBuilder("?app=")
				.append(this.toJsonObject().getInt("app"));

		JSONObject jsonObj = this.toJsonObject();

		if (jsonObj.has("fields")) {
			JSONArray fieldsArray = jsonObj.getJSONArray("fields");
			for (int i = 0; i < fieldsArray.length(); i++) {
				query
					.append("&fields[")
					.append(i)
					.append("]=")
					.append(URLEncoder.encode(fieldsArray.getString(i), "UTF-8"));
			}
		}
		if (jsonObj.has("query")) {
			query.append("&query=");
			query.append(URLEncoder.encode(jsonObj.getString("query"), "UTF-8"));
		}
		query.append("&totalCount=true");

		URL url = new URL(this.getHost().getKintoneRecordsUrl() + query.toString());
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setRequestMethod(this.getMethod());
		http.setDoInput(true);
        //http.setRequestProperty("X-Cybozu-API-Token", this.getApp().getApiToken());
        //http.setRequestProperty("Authorization", "Basic " + this.getApp().getApiToken());

        Config config = Config.getInstance();
        KintoneHost host = config.getKintoneHost();
        String mngUserName = host.getManageUserName();
        String mngUserPass = host.getManageUserPassword();
        if (StringUtils.isEmpty(mngUserName) || StringUtils.isEmpty(mngUserPass)) {
        	// ユーザ名、パスワードが設定されていなければAPI Tokenを利用して接続する。
        	http.setRequestProperty("X-Cybozu-API-Token", this.getApp().getApiToken());
        	http.setRequestProperty("Authorization", "Basic " + this.getApp().getApiToken());
        } else {
        	// ユーザ名、パスワードが設定されていれば管理者認証を利用して接続する。
        	http.setRequestProperty("X-Cybozu-Authorization", host.getAuthorization());
        	http.setRequestProperty("Authorization", "Basic " + host.getAuthorization());
        }

        http.connect();
        //http.disconnect();

		if (http.getResponseCode() != HttpURLConnection.HTTP_OK) {
			System.out.println(http.getResponseCode());
			System.out.println(http.getResponseMessage());
			BufferedReader br = new BufferedReader(new InputStreamReader(http.getErrorStream()));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			sb.append(this.toJsonObject().toString());
			log.error(http.getResponseCode(), sb.toString(), "kintone error");
		}

		return http;
	}
}
