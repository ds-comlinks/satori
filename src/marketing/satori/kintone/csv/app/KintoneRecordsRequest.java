package marketing.satori.kintone.csv.app;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 * kintoneアプリケーションに対するREST API呼出しを表す抽象クラスです。
 */
public abstract class KintoneRecordsRequest extends KintoneRequest {

	public KintoneRecordsRequest(KintoneHost host, KintoneApplication app) {
		super(host, app);
	}

	/**
	 * このオブジェクトで保持しているレコードを全て削除します。<BR>
	 * KINTONE_OPERATION_COUNT_LIMIT_BY_ONE件以上のレコード操作時に、直近の操作対象となったレコードを削除するのに用います。
	 */
	public abstract void clear();


	/**
	 * CSVレコードをJSONObjectに変換し、このオブジェクトの操作対象となるレコードとして登録します。
	 * @param recordObj 操作対象レコードが格納されたCSVRecordオブジェクト
	 * @throws JSONException CSVレコードからJSONObjectへの変換でエラーが発生した場合
	 */
	public abstract void put(Map<String, String> csvRecord) throws JSONException;


	/**
	 * JSONObjectを、このオブジェクトの操作対象となるレコードとして登録します。
	 * @param recordObj 操作対象レコードが格納されたJSONObjectオブジェクト
	 */
	protected abstract void put(JSONObject recordObj);

	@Override
	protected String getUrl() {
		return this.getHost().getKintoneRecordsUrl();
	}

}
