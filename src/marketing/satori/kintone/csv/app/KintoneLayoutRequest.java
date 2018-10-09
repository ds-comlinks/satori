package marketing.satori.kintone.csv.app;


import java.io.IOException;
import java.net.HttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 * kintoneアプリケーションへのlayout.jsonリクエストを表す。
 */
public class KintoneLayoutRequest extends KintoneRequest {
	private String method = "";
	private JSONObject jsonObject;

	/**
	 * オブジェクトを作成する。
	 * @param host ホスト設定
	 * @param app アプリ設定
	 */
	public KintoneLayoutRequest(KintoneHost host, KintoneApplication app) {
		super(host, app);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getUrl() {
		return this.getHost().getKintoneLayoutUrl();
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

	/**
	 * メソッドを指定して変更を適用する。
	 * @param method メソッド
	 * @return KINTONEへのリクエスト結果
	 * @throws JSONException JSONのエラー時に発生
	 * @throws IOException 入出力エラー時に発生
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
