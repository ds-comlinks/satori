package marketing.satori.kintone.csv.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 * kintoneアプリケーションに対するREST API呼出しを表す抽象クラスです。
 */
public class KintoneDeployRequest extends KintoneRequest {
	public static final String METHOD = "POST";

	JSONObject jsonObject;

	public KintoneDeployRequest(KintoneHost host, KintoneApplication appConfig, boolean revert) throws JSONException {
		super(host, appConfig);
		this.jsonObject = new JSONObject();
		jsonObject.put("apps", new JSONArray()
				.put(new JSONObject()
						.put("app", appConfig.getAppNo())));
		if (revert) {
			jsonObject.put("revert", revert);
		}
	}

	protected String getUrl() {
		return this.getHost().getKintoneDeployUrl();
	}

	@Override
	public JSONObject toJsonObject() throws JSONException {
		return jsonObject;
	}

	@Override
	public String getMethod() {
		return KintoneDeployRequest.METHOD;
	}
}
