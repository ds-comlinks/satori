package marketing.satori.kintone.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.util.MessageResources;
import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.EmailAddressGrantee;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import marketing.satori.kintone.csv.app.KeyRecordIdMap;
import marketing.satori.kintone.csv.app.KeyRecordMap;
import marketing.satori.kintone.csv.app.KintoneDeleteRequest;
import marketing.satori.kintone.csv.app.KintoneFieldsRequest;
import marketing.satori.kintone.csv.app.KintonePostRequest;
import marketing.satori.kintone.csv.app.campaign.CampaignPostRequest;
import marketing.satori.kintone.csv.app.campaign.record.CampaignRecordPostRequest;
import marketing.satori.kintone.csv.app.ccfield.CcFieldPostRequest;
import marketing.satori.kintone.csv.app.form.FormPostRequest;
import marketing.satori.kintone.csv.app.form.record.FormRecordPostRequest;
import marketing.satori.kintone.csv.app.leadcontact.LeadContactDeletePostRequest;
import marketing.satori.kintone.csv.app.leadcontact.LeadContactDeletePutRequest;
import marketing.satori.kintone.csv.app.leadcontact.LeadContactPostRequest;
import marketing.satori.kintone.csv.app.leadcontact.LeadContactPutRequest;
import marketing.satori.kintone.csv.app.leadcontact.LeadContactRejectPutRequest;
import marketing.satori.kintone.csv.app.mail.MailPostRequest;
import marketing.satori.kintone.csv.app.mail.record.MailRecordPostRequest;
import marketing.satori.kintone.csv.app.segment.SegmentPostRequest;
import marketing.satori.kintone.csv.app.tag.TagPostRequest;
import marketing.satori.kintone.csv.app.tag.record.TagRecordPostRequest;
import marketing.satori.kintone.csv.app.wrnosegment.WRNoSegmentPostRequest;
import marketing.satori.kintone.csv.app.wrsegment.WRSegmentPostRequest;
import marketing.satori.kintone.csv.config.Config;
import marketing.satori.kintone.csv.config.FileInfo;
import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;
import marketing.satori.kintone.csv.config.S3;
import marketing.satori.kintone.csv.config.S3Config;
import marketing.satori.kintone.csv.log.Log;

/**
 *
 * CSVファイルよりkintoneアプリのレコードを登録します。
 *
 */
public class CsvImportBatch {

	/** ログオブジェクト */
	private static Log log = new Log(CsvExportBatch.class.getName());

	/**
	 * バッチプログラムのアプリケーションエントリです。
	 * @param args コマンドライン引数
	 */
	public static void main(String[] args) {
		boolean isError = false;

		//*********************************************************************************
		// バッチオプションを確認、取得する。
		//*********************************************************************************
		List<String> argsArray = Arrays.asList(args);

		{
			String configurationFile = System.getProperty("log4j.configurationFile");
			if (StringUtils.isEmpty(configurationFile)) {
				System.out.println("vm引数[-Dlog4j.configurationFile]で、設定ファイルのパスを指定して下さい。");
				isError = true;
			}
		}

		String configFilePath = "";
		{
			int index = argsArray.indexOf("-configFile");
			if (index == argsArray.size() - 1) {
				System.out.println("バッチオプション[-configFile]で、設定ファイルのパスを指定して下さい。");
				isError = true;
			} else {
				configFilePath = argsArray.get(index + 1);
			}
		}

		if (isError) {
			System.out.println("プログラムは実行されません。");
			return;
		}

		MessageResources messageResources = Utils.getMessageResources();
		HttpURLConnection http = null;
		AmazonS3 amazonS3 = null;

		try {
			//*********************************************************************************
			// 設定ファイルを読み込む
			//*********************************************************************************
			Config config = Config.getInstance();
			try {
				config.load(configFilePath);
			} catch(Exception e) {
				log.error(0,
						messageResources.getMessage(
								"kintone.csv.batch.error.config.load",
								configFilePath,
								e.getMessage()),
						"config file load error"
						,e);
				return;
			}

			FileInfo fileInfo = config.getImportInfo();

			String fileEncode = fileInfo.getEncode();

			//*********************************************************************************
			// 設定ファイル項目のうち、サーバ接続前に確認可能な項目をチェックする。
			//*********************************************************************************
			if (StringUtils.isEmpty(config.getS3().getAccessKey())) {
				log.error(0, messageResources.getMessage(
						"kintone.csv.batch.error.config.s3.accesskey", config.getS3().getAccessKey()), "config file item error");
				isError = true;
			}

			if (StringUtils.isEmpty(config.getS3().getSecretKey())) {
				log.error(0, messageResources.getMessage(
						"kintone.csv.batch.error.config.s3.secretkey", config.getS3().getAccessKey()), "config file item error");
				isError = true;
			}

			if (StringUtils.isEmpty(config.getS3().getEndPointUrl())) {
				log.error(0, messageResources.getMessage(
						"kintone.csv.batch.error.config.s3.endpointurl", config.getS3().getAccessKey()), "config file item error");
				isError = true;
			}

			if (StringUtils.isEmpty(config.getS3().getRegion())) {
				log.error(0, messageResources.getMessage(
						"kintone.csv.batch.error.config.s3.region", config.getS3().getAccessKey()), "config file item error");
				isError = true;
			}

			if (StringUtils.isEmpty(config.getS3().getBucketName())) {
				log.error(0, messageResources.getMessage(
						"kintone.csv.batch.error.config.s3.bucketname", config.getS3().getAccessKey()), "config file item error");
				isError = true;
			}

			if (StringUtils.isEmpty(fileEncode)) {
				log.error(0, messageResources.getMessage(
						"kintone.csv.batch.error.config.encode", config.getS3().getAccessKey()), "config file item error");
				isError = true;
			}

			if (isError) {
				return;
			}

			//*********************************************************************************
			// 設定ファイルチェックオプションが指定されていたら終了する。
			//*********************************************************************************
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-config_check")) {
					return;
				}
			}

			log.info(0, messageResources.getMessage("kintone.csv.batch.import.start"), "start log.");

			//*********************************************************************************
			// S3サーバに接続し、制御ファイルを確認する。
			//*********************************************************************************
			S3 s3 = config.getS3();
			final String ACCESS_KEY = s3.getAccessKey();
			final String SECRET_KEY = s3.getSecretKey();
			final String ENDPOINT_URL = s3.getEndPointUrl();
			final String REGION = s3.getRegion();
			final String bucketName = s3.getBucketName();

			AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
			ClientConfiguration clientConfig = new ClientConfiguration();
			clientConfig.setProtocol(Protocol.HTTPS);
			clientConfig.setConnectionTimeout(10000);

			EndpointConfiguration endpointConfiguration = new EndpointConfiguration(ENDPOINT_URL, REGION);
			amazonS3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withClientConfiguration(clientConfig)
                    .withEndpointConfiguration(endpointConfiguration).build();

	        AccessControlList acl = new AccessControlList();
	        String[] accessUserEmails = s3.getAccessUserEmails();
	        for (int i = 0; i < accessUserEmails.length; i++) {
	        	acl.grantPermission(new EmailAddressGrantee(accessUserEmails[i]), Permission.FullControl);
	        	////acl.grantPermission(new CanonicalGrantee(accessUserEmails[i]), Permission.FullControl);
	        }

			//*********************************************************************************
			// 各kintoneサーバの処理
			//*********************************************************************************
            final int recCountLimit = fileInfo.getRecordCountByOneFile();
			KintoneHost host = config.getKintoneHost();
			try {
	            final String KINTONE_FOLDER_PATH =
						new StringBuilder("sfdc_kintone/").append(host.getS3FoldarName()).append("/kintone/").toString();

	            final String SATORI_FOLDER_PATH =
	            		new StringBuilder("sfdc_kintone/").append(host.getS3FoldarName()).append("/satori/").toString();

		        final String DATA_FOLDER_PATH =
		        		new StringBuilder(SATORI_FOLDER_PATH).append("data/").toString();

		        // S3のconfig.jsonを取得する。
		        String s3ConfigFilePath = new StringBuilder(KINTONE_FOLDER_PATH).append("config.json").toString();
		        S3Object configResult = amazonS3.getObject(bucketName, s3ConfigFilePath);
		        S3Config s3Config = S3Config.getInstance();
		        s3Config.load(configResult.getObjectContent());

		        // 制御ファイル[EXPORTING]が置かれているか確認する。
				{
			        ObjectListing result = amazonS3.listObjects(bucketName, DATA_FOLDER_PATH + "EXPORTING");

		            // 制御ファイル[EXPORTING]が有れば処理をしない。
		            if (result.getObjectSummaries().size() != 0) {
		            	log.warn(0,
		            			messageResources.getMessage("kintone.csv.batch.import.stop.under.control", host.getFqdn()),
		            			"stop by control file");
		            	return;
		            }
				}

				//*********************************************************************************
	            // 今回分のexportファイルを取得する。
				//*********************************************************************************
				Map<String, Integer> preExportedCustomerMap = new HashMap<>();

				if (s3Config.isPriorityKintone()) {
					String bkFolderPath = "";
					for (int i = 0; true; i++) {
						String tempPath = new StringBuilder(KINTONE_FOLDER_PATH).append("backup/")
								.append(new SimpleDateFormat("yyyyMMdd").format(new Date()))
								.append("_")
								.append(String.format("%04d", i + 1))
								.append("/").toString();
						ObjectListing result = amazonS3.listObjects(bucketName, tempPath);

			            if (result.getObjectSummaries().size() == 0) {
			            	break;
			            } else {
			            	bkFolderPath = tempPath;
			            }
					}

					loadS3FileMap(
							preExportedCustomerMap, bkFolderPath, amazonS3, bucketName
							,recCountLimit, messageResources, fileInfo.getEncode(), "customer", "email");

					loadS3FileMap(
							preExportedCustomerMap, bkFolderPath, amazonS3, bucketName
							,recCountLimit, messageResources, fileInfo.getEncode(), "customer_delete", "email");
				}


				//*********************************************************************************
	            //  delivery_permission_rejectを処理する。
				//*********************************************************************************
				Map<String, Integer> deliveryPermissionMap = new HashMap<>();
				{
					loadS3FileMap(
							deliveryPermissionMap, DATA_FOLDER_PATH, amazonS3, bucketName
							,recCountLimit, messageResources, fileInfo.getEncode(), "delivery_permission_reject", "email");
				}

				//*********************************************************************************
	            // customerを処理する。
				//*********************************************************************************
				// 新規か更新かの判断のために、予めキーデータを取得しておく。
				// (このマスタデータは、customer以外の処理でも使用する。)
				KeyRecordMap customerData = null;
				{
					KintoneApplication app = host.getLeadContact();
					KeyRecordMap keyData = new KeyRecordMap(host, app);
					keyData.loadKeyRecordMap(new String[]{});
					Map<String, Object> insertedMap = new HashMap<>();
					Map<String, Object> updatedRejectMap = new HashMap<>();

					String fileKey = "customer";

					// フィールド情報取得
					JSONObject prevDesignJson = null;
					if (!host.getManageUserName().equals("")) {
					    // 取得
					    //KintoneApplication app = host.getApp("leadContact");
					    KintoneFieldsRequest getReq = new KintoneFieldsRequest(host, app);
					    getReq.setParameter(new JSONObject().put("app", app.getAppNo())	);
					    http = getReq.apply("GET");

					    // 読み込み。
					    prevDesignJson = Utils.httpURLConnectionToJson(http).getJSONObject("properties");
					}

	            	// ファイルを取得する。
					String s3FilePath = "";
					int fileCount = 0;
	            	while (true) {
	            		s3FilePath = getS3FilePath(amazonS3, bucketName, DATA_FOLDER_PATH, fileKey, recCountLimit, fileCount++);
	            		if (StringUtils.isEmpty(s3FilePath)) {
	            			if (fileCount == 1) {
	            				log.warn(0,
	            						messageResources.getMessage("kintone.csv.batch.error.import.csv.not.found", fileKey),
	            						"csv.not.found");
	            			}
	            			break;
	        			}

			            LeadContactPostRequest postReq = new LeadContactPostRequest(host);
			            LeadContactPutRequest putReq = new LeadContactPutRequest(host);
			            LeadContactRejectPutRequest putRejectReq = new LeadContactRejectPutRequest(host);

			            postReq.setFiels(prevDesignJson);
			            putReq.setFiels(prevDesignJson);

			            S3Object result = amazonS3.getObject(bucketName, s3FilePath);
			            BufferedReader br = new BufferedReader(new InputStreamReader(result.getObjectContent(), fileInfo.getEncode()));
	            		CSVReaderEx reader = new CSVReaderEx(br);
			            try {
			            	// タイトルの読込み
			            	String[] record = reader.readNext();
			            	reader.loadTitle(record);

			            	int putCount = 0;
			            	int postCount = 0;
				            int putRejectCount = 0;

			            	Map<String, String> recordMap = reader.readNextMap();

			            	while (recordMap != null) {

				            	// レコードのキー項目を取得する。
				            	String[] csvKeyColumnList = app.getCsvKeyColumns();
				            	String[] keyValueList = new String[csvKeyColumnList.length];
				            	for (int i = 0; i < csvKeyColumnList.length; i++) {
				            		keyValueList[i] = recordMap.get(csvKeyColumnList[i]);
				            	}

						        // priorityがkintoneで、今回更新分に含まれている場合処理しない。
						        if (s3Config.isPriorityKintone() &&
						        		preExportedCustomerMap.containsKey(recordMap.get("email"))) {

					        		// delivery_permission_reject.csv　に存在するか
 					            	if (deliveryPermissionMap.containsKey(recordMap.get("email"))) {

						            	String keyString = recordMap.get("email");

						            	if (keyData.containsKey(keyValueList)) {
						            		// 更新
						            		if (updatedRejectMap.containsKey(keyString)) {
							            		log.warn(0,
							            				messageResources.getMessage("kintone.csv.batch.warn.duplicated", s3FilePath, keyString),
							            				"duplicated record");
						            		} else {
						            		    putRejectReq.put(recordMap);
						            		    updatedRejectMap.put(keyString, null);
						            		    putRejectCount++;
						            		    if (putRejectCount % 100 == 0) {
						            			    putRejectReq.apply();
						            			    putRejectReq.clear();
						            		    }
						            		}
						            	}
					            	}
					            	recordMap = reader.readNextMap();
					        		continue;
						        }


				            	if (deliveryPermissionMap.containsKey(recordMap.get("email"))) {
				            		recordMap.put("delivery_permission", "reject");
				            	}

				            	String keyString = recordMap.get("email");

				            	if (keyData.containsKey(keyValueList)) {
				            		// 更新
				            		putReq.put(recordMap);
				            		putCount++;
				            		if (putCount % 100 == 0) {
				            			putReq.apply();
				            			putReq.clear();
				            		}
				            	} else if (insertedMap.containsKey(keyString)) {
				            		log.warn(0,
				            				messageResources.getMessage("kintone.csv.batch.warn.duplicated", s3FilePath, keyString),
				            				"duplicated record");
				            	} else {
				            		// 登録
				            		postReq.put(recordMap);
				            		insertedMap.put(keyString, null);
				            		postCount++;
				            		if (postCount % 100 == 0) {
				            			postReq.apply();
				            			postReq.clear();
				            		}
				            	}
				            	recordMap = reader.readNextMap();
			            	}
			            	if (putCount % 100 != 0) {
			            		putReq.apply();
		            			putReq.clear();
			            	}
		            		if (postCount % 100 != 0) {
		            			postReq.apply();
		            			postReq.clear();
		            		}
			            	if (putRejectCount % 100 != 0) {
			            		putRejectReq.apply();
		            			putRejectReq.clear();
			            	}
			            } catch(JSONException e) {
							String message = messageResources.getMessage("kintone.csv.batch.error.import.csv", host.getFqdn(), s3FilePath);
							log.error(0, message, "csv error", e);
			            } finally {
			            	reader.close();
			            	br.close();
			            }

						if (fileCount > 0 && !s3FilePath.contains(".part")) {
							break;
						}
	            	}
				}

				//*********************************************************************************
	            // customer_deleteを処理する。
				//*********************************************************************************
				{
					KintoneApplication app = host.getLeadContact();
					KeyRecordMap keyData = new KeyRecordMap(host, app);
					keyData.loadKeyRecordMap(new String[]{});
					Map<String, Object> insertedMap = new HashMap<>();
					Map<String, Object> updatedMap = new HashMap<>();

					String fileKey = "customer_delete";

	            	// ファイルを取得する。
					int fileCount = 0;
					String s3FilePath = "";
					while (true) {
	            		if (recCountLimit == 0 && fileCount > 0) {
	            			break;
	            		}

	            		s3FilePath = getS3FilePath(amazonS3, bucketName, DATA_FOLDER_PATH, fileKey, recCountLimit, fileCount++);
	            		if (StringUtils.isEmpty(s3FilePath)) {
	            			if (fileCount == 1) {
	            				log.warn(0,
	            						messageResources.getMessage("kintone.csv.batch.error.import.csv.not.found", fileKey),
	            						"csv.not.found");
	            			}
	            			break;
	        			}

			            S3Object result = amazonS3.getObject(bucketName, s3FilePath);
			            LeadContactDeletePutRequest putReq = new LeadContactDeletePutRequest(host);
			            LeadContactDeletePostRequest postReq = new LeadContactDeletePostRequest(host);

			            int putCount = 0;
		            	int postCount = 0;
			            try (
			            		BufferedReader br = new BufferedReader(new InputStreamReader(result.getObjectContent(), fileInfo.getEncode()));
			            		CSVReaderEx reader = new CSVReaderEx(br);
			            ) {
			            	// タイトルの読込み
			            	String[] record = reader.readNext();
			            	reader.loadTitle(record);

			            	putCount = 0;
			            	postCount = 0;

		            		Map<String, String> recordMap = reader.readNextMap();

			            	while (recordMap != null) {
			            		// priorityがkintoneで、今回更新分に含まれている場合処理しない。
						        if (!s3Config.isPriorityKintone() &&
						        		preExportedCustomerMap.containsKey(recordMap.get("email"))) {
					        		recordMap = reader.readNextMap();
					        		continue;
						        }

				            	// レコードのキー項目を取得する。
				            	String[] csvKeyColumnList = app.getCsvKeyColumns();
				            	String[] keyValueList = new String[csvKeyColumnList.length];
				            	for (int i = 0; i < csvKeyColumnList.length; i++) {
				            		keyValueList[i] = recordMap.get(csvKeyColumnList[i]);
				            	}

				            	String keyString = recordMap.get("email");

				            	if (keyData.containsKey(keyValueList)) {
				            		// 更新
				            		if (updatedMap.containsKey(keyString)) {
					            		log.warn(0,
					            				messageResources.getMessage("kintone.csv.batch.warn.duplicated", s3FilePath, keyString),
					            				"duplicated record");
				            		} else {
				            		    putReq.put(recordMap);
				            		    updatedMap.put(keyString, null);
				            		    putCount++;
				            		    if (putCount % 100 == 0) {
				            			    putReq.apply();
				            			    putReq.clear();
				            		    }
				            		}
				            	} else if (insertedMap.containsKey(keyString)) {
				            		log.warn(0,
				            				messageResources.getMessage("kintone.csv.batch.warn.duplicated", s3FilePath, keyString),
				            				"duplicated record");
				            	} else {
				            		// 登録
				            		postReq.put(recordMap);
				            		insertedMap.put(keyString, null);
				            		postCount++;
				            		if (postCount % 100 == 0) {
				            			postReq.apply();
				            			postReq.clear();
				            		}
				            	}
				            	recordMap = reader.readNextMap();
			            	}
			            	if (putCount % 100 != 0) {
			            		putReq.apply();
		            			putReq.clear();
			            	}
			            } catch(JSONException e) {
							String message = messageResources.getMessage("kintone.csv.batch.error.import.csv", host.getFqdn(), s3FilePath, putCount + 1);
							log.error(0, message, "csv error", e);
			            }

						if (fileCount > 0 && !s3FilePath.contains(".part")) {
							break;
						}
					}
				}

				// マスタデータとして、customerを取得し直す。
				{
					KintoneApplication app = host.getLeadContact();
					customerData = new KeyRecordMap(host, app, new String[]{"LeadIdentity"});
					customerData.loadKeyRecordMap(new String[]{"LastName", "FirstName", "Company"});
				}

				//*********************************************************************************
				// その他のマスタの処理
				// 処理後には各マスタは再取得し、後述の履歴の処理で利用する。
				//*********************************************************************************
	            // segment_masterを処理
				//s3FilePath;
				KeyRecordMap segmentData = otherMasterOperation(
						"segment_master", DATA_FOLDER_PATH, recCountLimit,
						amazonS3, bucketName, host, host.getSegment(),
						new SegmentPostRequest(host),
						messageResources, new String[]{"SegmentName"});
				if (segmentData == null) {
					return;
				}

	            // campaign_masterを処理
				KeyRecordMap campaignData = otherMasterOperation(
						"campaign_master", DATA_FOLDER_PATH, recCountLimit,
						amazonS3, bucketName, host, host.getCampaign(),
						new CampaignPostRequest(host),
						messageResources, new String[]{"Name"});
				if (campaignData == null) {
					return;
				}

	            // form_masterを処理
				// フィールド情報取得
				JSONObject prevDesignJson = null;
				if (!host.getManageUserName().equals("")) {
				    // 取得
				    KintoneApplication app = host.getApp("form");
				    KintoneFieldsRequest getReq = new KintoneFieldsRequest(host, app);
				    getReq.setParameter(new JSONObject().put("app", app.getAppNo())	);
				    http = getReq.apply("GET");

				    // 読み込み。
				    prevDesignJson = Utils.httpURLConnectionToJson(http).getJSONObject("properties");
				}
				FormPostRequest formObj = new FormPostRequest(host);
				formObj.setFiels(prevDesignJson);
				KeyRecordMap formData = otherMasterOperation(
						"form_master", DATA_FOLDER_PATH, recCountLimit,
						amazonS3, bucketName, host, host.getForm(),
						formObj, messageResources, new String[]{"CampaignFormName","ExtraParamJson"});
				if (formData == null) {
					return;
				}

	            // mail_masterを処理
				KeyRecordMap mailData =  null;
				if (!Objects.isNull(host.getMailMaster())) {
				    mailData = otherMasterOperation(
						"campaign_mail_master", DATA_FOLDER_PATH, recCountLimit,
						amazonS3, bucketName, host, host.getMailMaster(),
						new MailPostRequest(host),
						messageResources, new String[]{"CampaignMailName"});
				    if (mailData == null) {
					    return;
				    }
				}

	            // tag_masterを処理
				KeyRecordMap tagData = null;
				if (!Objects.isNull(host.getTagMaster())) {
				    tagData = otherMasterOperation(
						"customer_tag_master", DATA_FOLDER_PATH, recCountLimit,
						amazonS3, bucketName, host, host.getTagMaster(),
						new TagPostRequest(host),
						messageResources, new String[]{"CustomerTagName"});
				    if (tagData == null) {
					    return;
				    }
				}

	            // customer_custom_field_masterを処理
				KeyRecordMap ccFieldData = null;
				if (!Objects.isNull(host.getCcFieldMaster())) {
				    ccFieldData = otherMasterOperation(
						"customer_custom_field_master", DATA_FOLDER_PATH, recCountLimit,
						amazonS3, bucketName, host, host.getCcFieldMaster(),
						new CcFieldPostRequest(host),
						messageResources, new String[]{"FieldName"});
				    if (ccFieldData == null) {
					    return;
				    }
				}

				//*********************************************************************************
				// 履歴の処理
				//*********************************************************************************
				boolean rt = false;
	            // segment_attachを処理
				rt = recordOperation(
						"segment_attach", DATA_FOLDER_PATH, recCountLimit,
						amazonS3, bucketName, host, host.getWRSegment(),
						new WRSegmentPostRequest(host, segmentData),
						messageResources);
				if (!rt) {
					return;
				}

	            // segment_detachを処理
				rt = recordOperation(
						"segment_detach", DATA_FOLDER_PATH, recCountLimit,
						amazonS3, bucketName, host, host.getWRNoSegment(),
						new WRNoSegmentPostRequest(host, segmentData),
						messageResources);
				if (!rt) {
					return;
				}

	            // campaign_historyを処理
				rt = recordOperation(
						"campaign_history", DATA_FOLDER_PATH, recCountLimit,
						amazonS3, bucketName, host, host.getCampaignRecord(),
						new CampaignRecordPostRequest(host, campaignData, customerData),
						messageResources);
				if (!rt) {
					return;
				}

	            // form_historyを処理
				rt = recordOperation(
						"form_history", DATA_FOLDER_PATH, recCountLimit,
						amazonS3, bucketName, host, host.getFormRecord(),
						new FormRecordPostRequest(host, formData),
						messageResources);
				if (!rt) {
					return;
				}

	            // customer_mail_actionを処理
				if (!Objects.isNull(host.getMailRecord())) {
				    rt = recordOperation(
						"customer_mail_action", DATA_FOLDER_PATH, recCountLimit,
						amazonS3, bucketName, host, host.getMailRecord(),
						new MailRecordPostRequest(host, mailData),
						messageResources);
				    if (!rt) {
					    return;
				    }
				}

	            // customer_tag_actionを処理
				if (!Objects.isNull(host.getTagRecord())) {
				    rt = recordOperation(
						"customer_tag_action", DATA_FOLDER_PATH, recCountLimit,
						amazonS3, bucketName, host, host.getTagRecord(),
						new TagRecordPostRequest(host, tagData),
						messageResources);
				    if (!rt) {
					    return;
				    }
				}
				//KeyRecordMap tagRecordData = otherMasterOperation(
				//		"customer_tag_action", DATA_FOLDER_PATH, recCountLimit,
				//		amazonS3, bucketName, host, host.getTagRecord(),
				//		new TagRecordPostRequest(host, null),
				//		messageResources, new String[]{"RecordId"});
				//if (tagRecordData == null) {
				//	return;
				//}

				//*********************************************************************
				// dataフォルダをクリアする。
				//*********************************************************************
				{
					ObjectListing list = amazonS3.listObjects(bucketName, DATA_FOLDER_PATH);
					for (S3ObjectSummary obj : list.getObjectSummaries()) {
						if (!StringUtils.equals(obj.getKey(), DATA_FOLDER_PATH)) {
							amazonS3.deleteObject(bucketName, obj.getKey());
						}
					}
				}

				// 制御ファイル[FINISHED]を出力する。
				Utils.amazonS3PutObject(amazonS3, bucketName, DATA_FOLDER_PATH + "FINISHED", "", acl, "https://" + ENDPOINT_URL);

			} catch(Exception e) {
				int status = (http == null ? 0 : http.getResponseCode());
				String message = messageResources.getMessage("kintone.csv.batch.error.import.host", host.getFqdn());
				log.error(status, message, "host error", e);
			}

			log.info(0, messageResources.getMessage("kintone.csv.batch.import.end"), "end log.");

		} catch(Exception e) {
			log.fatal(0, messageResources.getMessage("kintone.csv.batch.error.common"),
					"common error", e);
		} finally {
			if (http != null) {
				http.disconnect();
			}
			if (amazonS3 != null) {
				amazonS3.shutdown();
			}
		}
	}

	static String getS3FilePath(AmazonS3 s3client, String bucketName, String folderPath, String fileKey, int recCountLimit, int fileCount) {
		{
			StringBuilder s3FilePathBldr = new StringBuilder(folderPath).append(fileKey).append(".csv");
			String s3FilePath = s3FilePathBldr.toString();
			ObjectListing resultFileCheck = s3client.listObjects(bucketName, s3FilePath);
			if (resultFileCheck.getObjectSummaries().size() > 0) {
				return s3FilePath;
			}
		}

		if (recCountLimit > 0) {
			String no = String.format("%03d", fileCount + 1);
			StringBuilder s3FilePathBldr =
					new StringBuilder(folderPath).append(fileKey)
						.append(".part").append(no).append(".csv");
			String s3FilePath = s3FilePathBldr.toString();
			ObjectListing resultFileCheck = s3client.listObjects(bucketName, s3FilePath);
			if (resultFileCheck.getObjectSummaries().size() > 0) {
				return s3FilePath;
			}
		}

		return "";
	}

	static KeyRecordMap otherMasterOperation(
			String fileKey, String dataFolderPath, int recCountLimit,
			AmazonS3 client, String bucketName,
			KintoneHost host, KintoneApplication app, KintonePostRequest postReq,
			MessageResources messageResources, String[] masterFieldName) throws JSONException, IOException {
		{
			KeyRecordIdMap keyData = new KeyRecordIdMap(host, app);
			keyData.loadKeyRecordMap(new String[]{});

			// ファイルを取得する。
			int fileCount = 0;
			while (true) {
        		String s3FilePath = getS3FilePath(client, bucketName, dataFolderPath, fileKey, recCountLimit, fileCount++);

				//********************************************************************
				// ファイルの有無を確認する。
				//********************************************************************
        		if (StringUtils.isEmpty(s3FilePath)) {
	    			if (fileCount == 1) {
	    				log.warn(0, messageResources.getMessage("kintone.csv.batch.error.import.csv.not.found", fileKey), "csv.not.found");
	    			}
	    			break;
	    		}

	            S3Object result = client.getObject(bucketName, s3FilePath);

				//********************************************************************
	    		// 最初のみAppicationの全件を削除する。
				//********************************************************************
	    		if (fileCount == 1) {
		            KintoneDeleteRequest delReq = new KintoneDeleteRequest(host, app, keyData);
		            {
		                int count = 0;
		                for (String id : keyData.getAllIdsString()) {
		                	delReq.put(Integer.parseInt(id));
		                	count++;
		                	if (count > 0 && count % 100 == 0) {
		                		delReq.apply();
		                		delReq.clear();
		                	}
		                }
		                if (count % 100 != 0) {
		            		delReq.apply();
		            		delReq.clear();
		                }
		            }
	    		}

				//********************************************************************
	    		// CSVの全件を登録する。
				//********************************************************************
	    		int count = 0;
	            try (
	            		BufferedReader br = new BufferedReader(new InputStreamReader(result.getObjectContent(), Config.getInstance().getImportInfo().getEncode()));
	            		CSVReaderEx reader = new CSVReaderEx(br);
	            ) {
	            	// タイトルの読込み
	            	String[] record = reader.readNext();
	            	reader.loadTitle(record);

	            	Map<String, String> recordMap = reader.readNextMap();
	            	count = 0;
	            	while (recordMap != null) {
	            		postReq.put(recordMap);
	            		count++;
	            		if (count > 0 && count % 100 == 0) {
	            			postReq.apply();
	            			postReq.clear();
	            		}
	            		recordMap = reader.readNextMap();
	            	}
	            	if (count % 100 != 0) {
	        			postReq.apply();
	        			postReq.clear();
	            	}
	            } catch (JSONException e) {
					String message = messageResources.getMessage("kintone.csv.batch.error.import.csv", host.getFqdn(), s3FilePath, count + 1);
					log.error(0, message, "csv error", e);
					return null;
	            }

				if (fileCount > 0 && !s3FilePath.contains(".part")) {
					break;
				}
			}

		}

		// マスタデータを取得し直す。
	    KeyRecordMap masterData = new KeyRecordMap(host, app);
        masterData.loadKeyRecordMap(masterFieldName);
		return masterData;
	}

	static boolean recordOperation(
			String fileKey, String dataFolderPath, int recCountLimit,
			AmazonS3 client, String bucketName,
			KintoneHost host, KintoneApplication app, KintonePostRequest postReq, MessageResources messageResources) throws JSONException, IOException {

		KeyRecordIdMap keyData = new KeyRecordIdMap(host, app);
		keyData.loadKeyRecordMap(new String[]{});

		int fileCount = 0;
		while (true) {
    		if (recCountLimit == 0 && fileCount > 0) {
    			break;
    		}

			String s3FilePath = getS3FilePath(client, bucketName, dataFolderPath, fileKey, recCountLimit, fileCount++);

			//********************************************************************
			// ファイルの有無を確認する。
			//********************************************************************
			if (StringUtils.isEmpty(s3FilePath)) {
    			if (fileCount == 1) {
    				log.warn(0, messageResources.getMessage("kintone.csv.batch.error.import.csv.not.found", fileKey), "csv.not.found");
    			}
    			break;
    		}

    		S3Object result = client.getObject(bucketName, s3FilePath);

			//********************************************************************
    		// CSVの全件を登録する。
			//********************************************************************
        	int count = 0;
            try (
            		BufferedReader br = new BufferedReader(new InputStreamReader(result.getObjectContent(), Config.getInstance().getImportInfo().getEncode()));
            		CSVReaderEx reader = new CSVReaderEx(br);
            ) {
                // タイトルの読込み
            	String[] record = reader.readNext();
            	reader.loadTitle(record);

            	Map<String, String> recordMap = reader.readNextMap();
            	count = 0;
            	while (recordMap != null) {
            		postReq.put(recordMap);
            		count++;
            		if (count % 100 == 0) {
            			postReq.apply();
            			postReq.clear();
            		}
            		recordMap = reader.readNextMap();
            	}
            	if (count % 100 != 0) {
        			postReq.apply();
        			postReq.clear();
            	}
            } catch (JSONException e) {
				String message = messageResources.getMessage("kintone.csv.batch.error.import.csv", host.getFqdn(), s3FilePath, count + 1);
				log.error(0, message, "csv error", e);
				return false;
            }

			if (fileCount > 0 && !s3FilePath.contains(".part")) {
				break;
			}
		}

        return true;
	}

	public static void loadS3FileMap(
			Map<String, Integer> map, String folderPath, AmazonS3 amazonS3, String bucketName
			,int recCountLimit, MessageResources messageResources, String encode, String fileKey, String csvKey) throws IOException {

		if (!StringUtils.isEmpty(folderPath)) {
			int fileCount = 0;
			while (true) {
				String s3FilePath = getS3FilePath(amazonS3, bucketName, folderPath, fileKey, recCountLimit, fileCount++);
				if (StringUtils.isEmpty(s3FilePath)) {
	    			if (fileCount == 1) {
	    				log.warn(0, messageResources.getMessage("kintone.csv.batch.error.import.csv.not.found", fileKey), "csv.not.found");
	    			}
	    			break;
				}

	    		S3Object result = amazonS3.getObject(bucketName, s3FilePath);
	            BufferedReader br = new BufferedReader(new InputStreamReader(result.getObjectContent(), encode));
	    		CSVReaderEx reader = new CSVReaderEx(br);
	            try {
	            	// タイトルの読込み
	            	String[] record = reader.readNext();
	            	reader.loadTitle(record);

	            	Map<String, String> recordMap = reader.readNextMap();

	            	while (recordMap != null) {
	            		String email = recordMap.get(csvKey);
	            		map.put(email, Integer.valueOf(1));
	            		recordMap = reader.readNextMap();
	            	}
	            } finally {
	            	reader.close();
	            	br.close();
	            }

				if (fileCount > 0 && !s3FilePath.contains(".part")) {
					break;
				}
			}
		}

		return;
	}
}
