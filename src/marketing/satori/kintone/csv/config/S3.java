package marketing.satori.kintone.csv.config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class S3 {
	private String accessKey;

	private String secretKey;

	private String endPointUrl;

	private String region;

	private String bucketName;

	private String[] accessUserEmails;

	public S3(JSONObject json) throws JSONException {
		this.accessKey = json.getString("accessKey");
		this.secretKey = json.getString("secretKey");
		this.endPointUrl = json.getString("endPointUrl");
		this.region = json.getString("region");
		this.bucketName = json.getString("bucketName");
		JSONArray arr = json.getJSONArray("accessUserEmails");
		this.accessUserEmails = new String[arr.length()];
		for (int i = 0; i < arr.length(); i++) {
			this.accessUserEmails[i] = arr.getString(i);
		}
	}

	/**
	 * @return accessKey
	 */
	public String getAccessKey() {
		return accessKey;
	}

	/**
	 * @return secretKey
	 */
	public String getSecretKey() {
		return secretKey;
	}

	/**
	 * @return endPointUrl
	 */
	public String getEndPointUrl() {
		return endPointUrl;
	}

	/**
	 * @return region
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * @return bucketName
	 */
	public String getBucketName() {
		return bucketName;
	}

	public String[] getAccessUserEmails() {
		return accessUserEmails;
	}
}
