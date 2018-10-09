package marketing.satori.kintone.csv.config;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * S3の設定ファイルより設定をロードし保持します。
 *
 */
public class S3Config {
	private int companyId;
	private String priority;

	/** シングルトンインスタンス */
	private static S3Config instance = new S3Config();

	/**
	 * クラス外でのコンストラクタ呼出しは禁止します。
	 */
	private S3Config() {

	}

	/**
	 * シングルトンインスタンスを取得します。
	 * @return シングルトンインスタンス
	 */
	public static S3Config getInstance() {
		return instance;
	}


	/**
	 * 設定ファイルより設定を読み込みます。
	 * @throws IOException 設定ファイルの読込みに失敗した場合
	 * @throws JSONException 設定ファイルのJSONにエラーが含まれている場合
	 */
	public void load(FilterInputStream stream) throws UnsupportedEncodingException, IOException, JSONException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
        	StringBuffer buffer = new StringBuffer();

        	String line;;
        	while ((line  = br.readLine()) != null) {
        		buffer.append(line);
        	}

        	JSONObject json = new JSONObject(buffer.toString());
        	this.companyId = json.getInt("company_id");
        	this.priority = json.getString("priority");
        };
	}

	public int getCompanyId() {
		return this.companyId;
	}

	public String getPriority() {
		return this.priority;
	}

	public boolean isPriorityKintone() {
		return (this.priority.equals("kintone"));
	}
}
