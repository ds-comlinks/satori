package marketing.satori.kintone.csv.app;

import java.io.IOException;
import java.util.Set;
import java.util.StringJoiner;

import org.json.JSONException;

import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;

/**
 *
 * レコードIDとレコードのマッピングを取得、保持します。
 *
 */
public class KeyRecordIdMap extends KeyRecordMap {

	/**
	 * ホスト設定、アプリケーション設定でオブジェクトを初期化します。
	 * @param host ホスト設定
	 * @param app アプリケーション設定　
	 */
	public KeyRecordIdMap(KintoneHost host, KintoneApplication app) {
		super(host, app, new String[]{"$id"});
	}

	/**
	 * 引数で指定されたキー値とレコードIDの対応を返します。
	 * @param targetFieldNameList キー名
	 * @throws JSONException
	 * @throws IOException
	 */
	public void loadKeyRecordMap(String[] targetFieldNameList) throws JSONException, IOException {
		super.loadKeyRecordMap(targetFieldNameList);
	}

	/**
	 * キー値を指定して、レコードIDを取得します。
	 * @param keyValueList キー値のリスト
	 * @return レコードID
	 */
	public int getId(String[] keyValueList) {
		StringJoiner sj = new StringJoiner(KeyRecordMap.SEPARATOR);
		for (String keyValue : keyValueList) {
			sj.add(keyValue);
		}

		return Integer.parseInt(super.getRecord(keyValueList).get("$id"));
	}

	public Set<String> getAllIdsString() {
		return this.map.keySet();
	}
}
