package marketing.satori.kintone.csv.app;

import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.Utils;
import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 *
 * JSON文字列をCSVに変換する操作です。
 *
 */
public abstract class KintoneJsonToCsvOperation {
	private KintoneHost host;
	private KintoneApplication app;

	public KintoneJsonToCsvOperation(KintoneHost host, KintoneApplication app) {
		this.host = host;
		this.app = app;
	}

	/**
	 * CSVのタイトル行を返します。
	 * @return CSVのタイトル行
	 */
	public String getColumnNameCsv() {
		return Utils.listToCsvString(this.getColumnNameList());
	}

	/**
	 * レコードを表すJSONオブジェクトをCSV文字列に変換します。
	 * @param record
	 * @return
	 * @throws JSONException
	 */
	public abstract String jsonToCsv(JSONObject record) throws JSONException;

	public abstract  String[] jsonToList(JSONObject record) throws JSONException;

	/**
	 * ファイル名を返します。
	 * @return ファイル名
	 */
	public abstract String getFileName();

	/**
	 * CSVのカラム名のリストを返します。
	 * @return CSVのカラム名のリスト
	 */
	protected abstract List<String> getColumnNameList();

	/**
	 * CSVのカラム名のリストに追加します。。
	 * @return なし
	 */
	public abstract void addTitle(Iterator<?> keys);
	/**
	 * ホスト設定を返します。
	 * @return ホスト設定
	 */
	public KintoneHost getHost() {
		return this.host;
	}

	/**
	 * アプリケーション設定を返します。
	 * @return アプリケーション設定
	 */
	public KintoneApplication getApp() {
		return this.app;
	}
}
