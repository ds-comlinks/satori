package marketing.satori.kintone.csv.config;

import marketing.satori.kintone.csv.Utils;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * ファイル入出力設定をロードし管理します。
 *
 */
public class FileInfo {
	/** フォルダーパス */
	private String folderPath;

	/** エンコード */
	private String encode;

	/** 1ファイル辺りのレコード数 */
	private int recordCountByOneFile;

	/**
	 * フォルダーパスとエンコードからオブジェクトを作成します。
	 * @param folderPath フォルダーパス
	 * @param encode エンコード
	 * @throws JSONException
	 */
	public FileInfo(JSONObject json) throws JSONException {

		try {
			String folderPath = json.getString("folderPath");
			this.folderPath = StringUtils.isEmpty(folderPath) ?
					"" :
					Utils.addStringIfNot(folderPath, File.separator);
		} catch (JSONException e) {
			// importの場合はfolderPathは利用しないため任意。
			this.folderPath = "";
		}

		this.encode = json.getString("encode");
		this.recordCountByOneFile = json.getInt("recordCountByOneFile");
	}

	/**
	 * フォルダーパスを返します。
	 * @return フォルダーパス
	 */
	public String getFolderPath() {
		return folderPath;
	}

	/**
	 * エンコードを返します。
	 * @return エンコード
	 */
	public String getEncode() {
		return encode;
	}

	/**
	 * 1ファイル辺りのレコード数を返します。
	 * @return 1ファイル辺りのレコード数
	 */
	public int getRecordCountByOneFile() {
		return recordCountByOneFile;
	}
}
