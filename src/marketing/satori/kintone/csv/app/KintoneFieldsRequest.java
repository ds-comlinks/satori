package marketing.satori.kintone.csv.app;


import java.io.IOException;
import java.net.HttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;
/**
 * kintoneアプリケーションへのfields.jsonリクエストを表す。
 */
public class KintoneFieldsRequest extends KintoneRequest {
	private String method = "";
	private JSONObject jsonObject;

	/**
	 * オブジェクトを作成する。
	 * @param host ホスト設定
	 * @param app アプリ設定
	 */
	public KintoneFieldsRequest(KintoneHost host, KintoneApplication app) {
		super(host, app);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getUrl() {
		return this.getHost().getKintoneFieldsUrl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONObject toJsonObject() throws JSONException {
		return this.jsonObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMethod() {
		return this.method;
	}

	/* (非 Javadoc)
	 * @see marketing.satori.kintone.csv.app.KintoneRequest#apply()
	 */
	public HttpURLConnection apply(String method) throws JSONException, IOException {
		this.method = method;
		return super.apply();
	}

	/**
	 * リクエストのパラメータを設定する。
	 * @param postJson リクエストのパラメータ
	 */
	public void setParameter(JSONObject postJson) {
		this.jsonObject = postJson;
	}

}
