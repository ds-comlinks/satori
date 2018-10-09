package marketing.satori.kintone.csv.config;

import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.Utils;
import java.io.IOException;

/**
 *
 * アプリケーション設定を設定ファイルよりロードし保持します。
 *
 */
public class Config {

	/** シングルトンインスタンス */
	private static Config instance = new Config();

	/** 改行コード */
	private String lineSeparator;

	/** kintoneホスト */
	private KintoneHost kintoneHost;

	/** S3 */
	private S3 s3;

	/** import情報 */
	private FileInfo importInfo;

	/** export情報 */
	private FileInfo exportInfo;

	/** fields,layout情報 */
	private FileInfo fieldsImportInfo;

	/** sha salt */
	private String salt;

	/**
	 * クラス外でのコンストラクタ呼出しは禁止します。
	 */
	private Config() {

	}

	/**
	 * シングルトンインスタンスを取得します。
	 * @return シングルトンインスタンス
	 */
	public static Config getInstance() {
		return instance;
	}


	/**
	 * 設定ファイルより設定を読み込みます。
	 * @throws IOException 設定ファイルの読込みに失敗した場合
	 * @throws JSONException 設定ファイルのJSONにエラーが含まれている場合
	 */
	public void load(String configFilePath) throws IOException, JSONException {
		String json = Utils.loadJsonString(configFilePath);
		JSONObject jsonObject = null;
		jsonObject = new JSONObject(json);

		this.lineSeparator = jsonObject.getString("lineSeparator");

		this.kintoneHost = new KintoneHost(jsonObject.getJSONObject("kintoneHost"));

		this.salt = jsonObject.getString("salt");

		this.s3 = new S3(jsonObject.getJSONObject("s3"));

		this.importInfo = new FileInfo(jsonObject.getJSONObject("import"));

		this.exportInfo = new FileInfo(jsonObject.getJSONObject("export"));

		if (jsonObject.has("fields")) {
			this.fieldsImportInfo = new FileInfo(jsonObject.getJSONObject("fields"));
		}
	}

	/**
	 * 改行コードを返します。
	 * @return 改行コード
	 */
	public String getLineSeparator() {
		return lineSeparator;
	}


	/**
	 * @return kintoneHostList
	 */
	public KintoneHost getKintoneHost() {
		return kintoneHost;
	}

	public String getSalt() {
		return salt;
	}

	/**
	 * @return s3
	 */
	public S3 getS3() {
		return s3;
	}


	/**
	 * @return importInfo
	 */
	public FileInfo getImportInfo() {
		return importInfo;
	}


	/**
	 * @return exportInfo
	 */
	public FileInfo getExportInfo() {
		return exportInfo;
	}

	/**
	 * @return fieldsImportInfo
	 */
	public FileInfo getFieldsImportInfo() {
		return fieldsImportInfo;
	}
}

