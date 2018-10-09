package marketing.satori.kintone.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.PropertyMessageResourcesFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import marketing.satori.kintone.csv.config.Config;
import marketing.satori.kintone.csv.log.Log;

public class Utils {
	static Log log = new Log(Utils.class.getName());
	static String regex;
	static Pattern pattern;
	static {
		regex = "^(http://|https://)[a-z|A-Z|0-9][a-z|A-Z|0-9|\\-\\_\\!\\~\\*\\'\\(\\)\\:\\@\\&\\=\\+\\$\\,\\;\\?\\#\\%]+\\.[a-z|A-Z|0-9|\\-\\_\\!\\~\\*\\'\\(\\)\\:\\@\\&\\=\\+\\$\\,\\;\\?\\#\\%]+[a-z|A-Z|0-9|\\-\\_\\!\\~\\*\\'\\(\\)\\:\\@\\&\\=\\+\\$\\,\\;\\?\\#\\%\\.\\/]*";
		pattern = Pattern.compile(regex);
	}

	/**
	 * メッセージリソースオブジェクトを返します。
	 * @return メッセージリソースオブジェクト
	 */
	public static MessageResources getMessageResources() {
		PropertyMessageResourcesFactory mesResFactory = new PropertyMessageResourcesFactory();
		return mesResFactory.createResources("marketing.satori.kintone.csv.resources.Message_Resources");
	}

	/**
	 * 例外オブジェクトの保持するスタックトレースを文字列として返します。
	 * @param e 例外
	 * @return スタックトレースの文字列表現
	 */
	public static String getStackTraceString(Exception e) {
		StackTraceElement[] steArr = e.getStackTrace();
		StringBuilder rtBuilder = new StringBuilder();

		for (StackTraceElement ste : steArr) {
			rtBuilder.append("\t");
			rtBuilder.append(ste.toString());
			rtBuilder.append("\n");
		}

		return rtBuilder.toString();
	}

	/**
	 * 引数で指定されたバッファに対し、文字列項目をCSV項目として追加します。
	 * @param strb
	 * @param str
	 * @return
	 */
	public static void appendCsvString(StringBuilder strb, String str) {
		strb.append("\"");
		strb.append(str.replaceAll("\\\"", "\""));
		strb.append("\"");
	}


	public static JSONObject getJSONValueObject(JSONObject parentObj, String key) throws JSONException {
		if (parentObj.isNull(key)) {
			return new JSONObject();
		}

		JSONObject obj = parentObj.getJSONObject(key);

		return obj;
	}

	/**
	 * キーを指定して、JSONオブジェクトに含まれる配列を取得します。
	 * @param parentObj 対象のJSONオブジェクト
	 * @param key キー
	 * @return JSON配列オブジェクト
	 * @throws JSONException JSON配列オブジェクトの取得でエラーが発生した場合
	 */
	public static JSONArray getJSONValueArray(JSONObject parentObj, String key) throws JSONException {
		if (parentObj.isNull(key)) {
			return new JSONArray();
		}

		JSONObject obj = parentObj.getJSONObject(key);

		if (obj.isNull(("value"))) {
			return new JSONArray();
		}

		return obj.getJSONArray("value");
	}

	/**
	 * キーを指定して、JSONオブジェクトに含まれる文字列を取得します。
	 * @param parentObj 対象のJSONオブジェクト
	 * @param key キー
	 * @return 文字列
	 * @throws JSONException JSONからの文字列の取得でエラーが発生した場合
	 */
	public static String getJSONValueString(JSONObject parentObj, String key) throws JSONException {
		if (parentObj.isNull(key)) {
			return "";
		}

		JSONObject obj = parentObj.getJSONObject(key);

		if (obj.isNull(("value"))) {
			return "";
		}
//String aa = obj.getString("value");
//aa = aa.replaceAll("\"", "\\\\\"");

		return obj.getString("value").replaceAll("\n", "\\\\n");
	}

	/**
	 * キーを指定して、JSONオブジェクトに含まれる値の型を取得します。
	 * @param parentObj 対象のJSONオブジェクト
	 * @param key キー
	 * @return 文字列（値の型）
	 * @throws JSONException JSONからの文字列の取得でエラーが発生した場合
	 */
	public static String getJSONValueType(JSONObject parentObj, String key) throws JSONException {
		if (parentObj.isNull(key)) {
			return "";
		}

		JSONObject obj = parentObj.getJSONObject(key);

		if (obj.isNull(("type"))) {
			return "";
		}

		return obj.getString("type");
	}
	/**
	 * リストオブジェクトをCSV文字列に変換します。
	 * @param list リストオブジェクト
	 * @return CSV文字列
	 */
	public static String listToCsvString(List<String> list) {
		StringBuilder sb = new StringBuilder();
		for (String elm : list) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			appendCsvString(sb, elm);
		}
		return sb.toString();
	}

	/**
	 *
	 * @param source
	 * @param add
	 * @return
	 */
	public static String addStringIfNot(String source, String add) {
		if (!source.endsWith(add)) {
			return new StringBuilder(source).append(add).toString();
		}
		return source;
	}

	public static String csvDateStringToJsonDateString(String s, String timeZone) {
		if (StringUtils.isEmpty(s)) {
			return "";
		} else {
			String rt = "";
			try {
				SimpleDateFormat sdfTZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

				sdfTZ.setTimeZone(TimeZone.getTimeZone(timeZone));
				rt = sdfTZ.format(sdf.parse(s));
			} catch (ParseException e) {
				MessageResources messageResources = getMessageResources();
				log.warn(0, messageResources.getMessage("kintone.csv.batch.error.invalid.date.format", s), "invalid format");
			}
			return rt;
		}
	}

	public static String jsonDateStringToCsvDateString(String s, String timeZone) {
		if (StringUtils.isEmpty(s)) {
			return "";
		} else {
			String rt = "";
			try {
				SimpleDateFormat sdfTZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

				sdfTZ.setTimeZone(TimeZone.getTimeZone(timeZone));
				rt = sdf.format(sdfTZ.parse(s));
			} catch (ParseException e) {
				MessageResources messageResources = getMessageResources();
				log.warn(0, messageResources.getMessage("kintone.csv.batch.error.invalid.date.format", s), "invalid format");
			}
			return rt;
		}
	}

	public static String toSha256(String companyId, String email) {
		String salt = Config.getInstance().getSalt();
		String seed = new StringBuilder(companyId)
				.append("____")
				.append(email)
				.append("____")
				.append(salt).toString();

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			// 初回実行分 + strech4回
			for (int i = 0; i < 5; i++) {
				byte[] digest = md.digest(seed.getBytes());
				StringBuilder sb = new StringBuilder(2 * digest.length);
				for(byte bt: digest) {
					sb.append(String.format("%02x", bt&0xff) );
				}
				seed = sb.toString();
			}
			return seed;
		} catch (NoSuchAlgorithmException e) {
			// (Algorithmはオンコードであるため、動作確認後には実行されないはず。)
			log.error(0, "No Such Algorithm Exception", "fatal log");
			return "";
		}
	}

	public static PutObjectResult amazonS3PutObject(AmazonS3 s3client, String bucketName, String path, File file, AccessControlList acl, String redirectLocation) {
		return s3client.putObject(
				new PutObjectRequest(bucketName, path, file)
					.withAccessControlList(acl)
					.withRedirectLocation(redirectLocation));
	}

	public static PutObjectResult amazonS3PutObject(AmazonS3 s3client, String bucketName, String path, String str, AccessControlList acl, String redirectLocation) {
		return s3client.putObject(
				new PutObjectRequest(bucketName, path, str)
					.withAccessControlList(acl)
					.withRedirectLocation(redirectLocation));
	}

	public static boolean isUrlString(String expr) {
		Matcher matcher = Utils.pattern.matcher(expr);

		if (matcher.find() && StringUtils.equals(matcher.group(), expr)) {
			return true;
		}

		return false;
	}

	public static String loadJsonString(String configFilePath) throws IOException {
		return loadJsonString(new File(configFilePath));
	}

	public static String loadJsonString(File configFile) throws IOException {
		try (InputStream input = new FileInputStream(configFile)) {
			byte[] buffer = new byte[input.available()];
			input.read(buffer);
			return new String(buffer);
		}
	}

	public static String httpURLConnectionToJsonString(HttpURLConnection httpCon) throws IOException {
		InputStreamReader isr = new InputStreamReader(httpCon.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(isr);
        String line;

        StringBuilder message = new StringBuilder();
        while ((line = reader.readLine()) != null) {
        	message.append(line);
        }

		return message.toString();
	}

	public static JSONObject httpURLConnectionToJson(HttpURLConnection httpCon) throws IOException, JSONException {
		return new JSONObject(Utils.httpURLConnectionToJsonString(httpCon));
	}
}
