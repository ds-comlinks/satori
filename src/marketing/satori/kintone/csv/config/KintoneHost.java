package marketing.satori.kintone.csv.config;

import java.util.Base64;

import org.apache.struts.util.MessageResources;
import org.json.JSONException;
import org.json.JSONObject;

import marketing.satori.kintone.csv.CsvExportBatch;
import marketing.satori.kintone.csv.Utils;
import marketing.satori.kintone.csv.log.Log;


public class KintoneHost {

	/** kintone レコード操作 URL */
	private String kintoneRecordsUrl;

	/** kintone フィールド操作 URL */
	private String kintoneFieldsUrl;

	/** kintone レイアウト操作 URL */
	private String kintoneLayoutUrl;

	/** kintone デプロイ操作 URL */
	private String kintoneDeployUrl;

	/** S3フォルダ名 */
	private String s3FolderName;

	/** 管理者ログイン名 */
	private String manageUserName;

	/** 管理者パスワード */
	private String manageUserPassword;

	/** 管理者認証 */
	private String authorization;

	/** satoriユーザ */
	private String satoriUser;

	/** ホスト名 */
	private String fqdn;

	/** タイムゾーン */
	private String timeZone;

	/** Satori,Kintoneのフィールド対応 */
	private JSONObject fieldMap;

	/** アプリケーション設定 - Lead/リード,Contact/取引先責任者 */
	private KintoneApplication leadContact;

	/** アプリケーション設定 - Account/取引先 */
	private KintoneApplication account;

	/** アプリケーション設定 - Opportunity/商談 */
	private KintoneApplication opportunity;

	/** アプリケーション設定 - Campaign/キャンペーン */
	private KintoneApplication campaign;

	/** アプリケーション設定 - セグメントマスタ */
	private KintoneApplication segment;

	/** アプリケーション設定 - Web行動履歴(セグメント該当) */
	private KintoneApplication wRSegment;

	/** アプリケーション設定 - Web行動履歴(セグメント非該当) */
	private KintoneApplication wRNoSegment;

	/** アプリケーション設定 - キャンペーン履歴 */
	private KintoneApplication campaignRecord;

	/** アプリケーション設定 - フォームマスタ */
	private KintoneApplication form;

	/** アプリケーション設定 - フォーム履歴 */
	private KintoneApplication formRecord;

	/** アプリケーション設定 - メール配信・開封マスタ */
	private KintoneApplication mailMaster;

	/** アプリケーション設定 - メール配信・開封履歴 */
	private KintoneApplication mailRecord;

	/** アプリケーション設定 - タグマスタ */
	private KintoneApplication tagMaster;

	/** アプリケーション設定 - タグ履歴 */
	private KintoneApplication tagRecord;

	/** アプリケーション設定 - カスタマーカスタムフィールドマスタ */
	private KintoneApplication ccFieldMaster;

	private S3 s3;

	/**
	 * @return kintoneRecordsUrl
	 */
	public String getKintoneRecordsUrl() {
		return kintoneRecordsUrl;
	}

	/**
	 * @return kintoneFieldsUrl
	 */
	public String getKintoneFieldsUrl() {
		return kintoneFieldsUrl;
	}

	/**
	 * @return kintoneLayoutUrl
	 */
	public String getKintoneLayoutUrl() {
		return kintoneLayoutUrl;
	}

	/**
	 * @return kintoneLayoutUrl
	 */
	public String getKintoneDeployUrl() {
		return kintoneDeployUrl;
	}

	public String getS3FoldarName() {
		return s3FolderName;
	}

	/**
	 * @return manageUserName
	 */
	public String getManageUserName() {
		return manageUserName;
	}

	/**
	 * @return manageUserPassword
	 */
	public String getManageUserPassword() {
		return manageUserPassword;
	}

	/**
	 * @return authorization
	 */
	public String getAuthorization() {
		return authorization;
	}

	/**
	 * @return name
	 */
	public String getFqdn() {
		return fqdn;
	}

	/**
	 * @return timeZone
	 */
	public String getTimezone() {
		return timeZone;
	}

	/**
	 * @return fieldMap
	 */
	public JSONObject getFieldMap() {
		return fieldMap;
	}

	/**
	 * @return leadContact
	 */
	public KintoneApplication getLeadContact() {
		return leadContact;
	}

	/**
	 * @return account
	 */
	public KintoneApplication getAccount() {
		return account;
	}

	/**
	 * @return opportunity
	 */
	public KintoneApplication getOpportunity() {
		return opportunity;
	}

	/**
	 * @return campaign
	 */
	public KintoneApplication getCampaign() {
		return campaign;
	}

	/**
	 * @return segment
	 */
	public KintoneApplication getSegment() {
		return segment;
	}

	/**
	 * @return wRSegment
	 */
	public KintoneApplication getWRSegment() {
		return wRSegment;
	}

	/**
	 * @return wRNoSegment
	 */
	public KintoneApplication getWRNoSegment() {
		return wRNoSegment;
	}

	/**
	 * @return campaignRecord
	 */
	public KintoneApplication getCampaignRecord() {
		return campaignRecord;
	}

	/**
	 * @return form
	 */
	public KintoneApplication getForm() {
		return form;
	}


	/**
	 * @return formRecord
	 */
	public KintoneApplication getFormRecord() {
		return formRecord;
	}

	/**
	 * @return mailMaster
	 */
	public KintoneApplication getMailMaster() {
		return mailMaster;
	}

	/**
	 * @return mailRecord
	 */
	public KintoneApplication getMailRecord() {
		return mailRecord;
	}

	/**
	 * @return tagMaster
	 */
	public KintoneApplication getTagMaster() {
		return tagMaster;
	}

	/**
	 * @return tagRecord
	 */
	public KintoneApplication getTagRecord() {
		return tagRecord;
	}

	/**
	 * @return ccFieldMaster
	 */
	public KintoneApplication getCcFieldMaster() {
		return ccFieldMaster;
	}

	public KintoneApplication getApp(String key) {
		if (key.equals("leadContact")) {
			return this.getLeadContact();
		} else if (key.equals("account")) {
			return this.getAccount();
		} else if (key.equals("opportunity")) {
			return this.getOpportunity();
		} else if (key.equals("campaign")) {
			return this.getCampaign();
		} else if (key.equals("segment")) {
			return this.getSegment();
		} else if (key.equals("wRSegment")) {
			return this.getWRSegment();
		} else if (key.equals("wRNoSegment")) {
			return this.getWRNoSegment();
		} else if (key.equals("campaignRecord")) {
			return this.getCampaignRecord();
		} else if (key.equals("form")) {
			return this.getForm();
		} else if (key.equals("formRecord")) {
			return this.getFormRecord();
		} else if (key.equals("mailMaster")) {
			return this.getMailMaster();
		} else if (key.equals("mailRecord")) {
			return this.getMailRecord();
		} else if (key.equals("tagMaster")) {
			return this.getTagMaster();
		} else if (key.equals("tagRecord")) {
			return this.getTagRecord();
		} else if (key.equals("ccFieldMaster")) {
			return this.getCcFieldMaster();
		} else {
			return null;
		}
	}

	/**
	 * @return s3
	 */
	public S3 getS3() {
		return s3;
	}

	public String getSatoriUser() {
		return this.satoriUser;
	}

	public KintoneHost(JSONObject json) throws JSONException {

		MessageResources messageResources = Utils.getMessageResources();
		Log log = new Log(CsvExportBatch.class.getName());

		this.s3FolderName = json.getString("s3FolderName");
		this.fqdn = json.getString("fqdn");
		this.kintoneRecordsUrl = new StringBuilder("https://")
				.append(this.fqdn)
				.append("/k/v1/records.json")
				.toString();

		this.kintoneFieldsUrl = new StringBuilder("https://")
				.append(this.fqdn)
				.append("/k/v1/preview/app/form/fields.json")
				.toString();

		this.kintoneLayoutUrl = new StringBuilder("https://")
				.append(this.fqdn)
				.append("/k/v1/preview/app/form/layout.json")
				.toString();

		this.kintoneDeployUrl = new StringBuilder("https://")
				.append(this.fqdn)
				.append("/k/v1/preview/app/deploy.json")
				.toString();

		this.timeZone = json.getString("timeZone");

		if (json.has("manageUser")) {
			this.manageUserName = json.getJSONObject("manageUser").getString("loginName");

			this.manageUserPassword = json.getJSONObject("manageUser").getString("password");

			String tmpAuth = new StringBuilder(this.manageUserName)
					.append(":")
					.append(this.manageUserPassword)
					.toString();

			this.authorization = Base64.getEncoder().encodeToString(tmpAuth.getBytes());
		} else {
			this.manageUserName = "";
			this.manageUserPassword = "";
			this.authorization = "";
		}

		if (json.has("fieldMap")) {
			this.fieldMap = json.getJSONObject("fieldMap");
		} else {
			this.fieldMap = null;
		}
		this.satoriUser = json.getString("satoriUser");

		JSONObject appJson = json.getJSONObject("applications");


		this.leadContact = new KintoneApplication(appJson.getJSONObject("leadContact"),
				new String[]{"email"}, new String[]{"Email"});

		this.account = new KintoneApplication(appJson.getJSONObject("account"),
				new String[]{""}, new String[]{""});

		this.opportunity = new KintoneApplication(appJson.getJSONObject("opportunity"),
				new String[]{""}, new String[]{""});

		this.campaign = new KintoneApplication(appJson.getJSONObject("campaign"),
				new String[]{"campaign_id"}, new String[]{"CampaignId"});

		this.segment = new KintoneApplication(appJson.getJSONObject("segment"),
				new String[]{"segment_id"}, new String[]{"SegmentId"});

		this.wRSegment = new KintoneApplication(appJson.getJSONObject("wRSegment"),
				new String[]{"lead_identity", "segment_id"}, new String[]{"LeadIdentity", "SegmentId"});

		this.wRNoSegment = new KintoneApplication(appJson.getJSONObject("wRNoSegment"),
				new String[]{"lead_identity", "segment_id"}, new String[]{"LeadIdentity", "SegmentId"});

		this.campaignRecord = new KintoneApplication(appJson.getJSONObject("campaignRecord"),
				new String[]{"lead_identity", "campaign_id"}, new String[]{"LeadIdentity", "CampaignId"});

		this.form = new KintoneApplication(appJson.getJSONObject("form"),
				new String[]{"campaign_form_id"}, new String[]{"CampaignFormId"});

		this.formRecord = new KintoneApplication(appJson.getJSONObject("formRecord"),
				new String[]{"lead_identity", "campaign_form_id"}, new String[]{"LeadIdentity", "CampaignFormId"});

		if (!appJson.isNull("mailMaster")) {
		    this.mailMaster = new KintoneApplication(appJson.getJSONObject("mailMaster"),
				new String[]{"influx_source"}, new String[]{"CampaignMailId"});
	    } else {
	    	String message = messageResources.getMessage("kintone.csv.batch.skip.app", "メール配信・開封マスタ");
			log.info(0, message, "csv info");
	    }

		if (!appJson.isNull("mailRecord")) {
		    this.mailRecord = new KintoneApplication(appJson.getJSONObject("mailRecord"),
				new String[]{"lead_identity"}, new String[]{"LeadIdentity"});
	    } else {
	    	String message = messageResources.getMessage("kintone.csv.batch.skip.app", "メール配信・開封履歴");
			log.info(0, message, "csv info");
		}

		if (!appJson.isNull("tagMaster")) {
		    this.tagMaster = new KintoneApplication(appJson.getJSONObject("tagMaster"),
				new String[]{"influx_source"}, new String[]{"CustomerTagId"});
	    } else {
	    	String message = messageResources.getMessage("kintone.csv.batch.skip.app", "タグマスタ");
			log.info(0, message, "csv info");
		}

		if (!appJson.isNull("tagRecord")) {
		    this.tagRecord = new KintoneApplication(appJson.getJSONObject("tagRecord"),
				new String[]{"lead_identity"}, new String[]{"LeadIdentity"});
	    } else {
	    	String message = messageResources.getMessage("kintone.csv.batch.skip.app", "タグ履歴");
			log.info(0, message, "csv info");
		}

		if (!appJson.isNull("ccFieldMaster")) {
		    this.ccFieldMaster = new KintoneApplication(appJson.getJSONObject("ccFieldMaster"),
				new String[]{"field_name"}, new String[]{"FieldName"});
	    } else {
	    	String message = messageResources.getMessage("kintone.csv.batch.skip.app", "カスタマーカスタムフィールドマスタ");
			log.info(0, message, "csv info");
		}

	}

}
