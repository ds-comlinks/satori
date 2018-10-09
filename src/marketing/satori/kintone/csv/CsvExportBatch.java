package marketing.satori.kintone.csv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.util.MessageResources;
import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.AmazonServiceException;
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
import com.opencsv.CSVWriter;

import marketing.satori.kintone.csv.app.KintoneFieldsRequest;
import marketing.satori.kintone.csv.app.KintoneGetRequest;
import marketing.satori.kintone.csv.app.KintoneJsonToCsvOperation;
import marketing.satori.kintone.csv.app.leadcontact.LeadContactJsonToCsvOperation;
import marketing.satori.kintone.csv.app.leadcontact.LeadContactJsonToDeleteCsvOperation;
import marketing.satori.kintone.csv.config.Config;
import marketing.satori.kintone.csv.config.FileInfo;
import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;
import marketing.satori.kintone.csv.config.S3;
import marketing.satori.kintone.csv.exelog.HostRecordLog;
import marketing.satori.kintone.csv.exelog.Operation;
import marketing.satori.kintone.csv.log.Log;

/**
 *
 * kintoneアプリの内容をCSVに出力するバッチプログラムです。
 *
 */
public class CsvExportBatch {

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
				System.out.println("vm引数[-Dlog4j.configurationFile]で、ログファイルのパスを指定して下さい。");
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
		AmazonS3 client = null;

		try {
			//*********************************************************************************
			// 設定ファイルを読み込む
			//*********************************************************************************
			Config config = Config.getInstance();
			try {
				config.load(configFilePath);
			} catch(Exception e) {
				log.error(0, messageResources.getMessage(
								"kintone.csv.batch.error.config.load",
								configFilePath,
								e.getMessage()),
						"config file load error"
						,e);
				return;
			}

			FileInfo fileInfo = config.getExportInfo();

			String folderPath = fileInfo.getFolderPath();
			String fileEncode = fileInfo.getEncode();

			//*********************************************************************************
			// 設定ファイル項目のうち、サーバ接続前に確認可能な項目をチェックする。
			//*********************************************************************************
			File folder = new File(folderPath);
			if (!folder.isDirectory()) {
				log.error(0,
						messageResources.getMessage(
								"kintone.csv.batch.error.export.folder.notfound", folderPath),
						"config file item error");
				isError = true;
			}

			if (StringUtils.isEmpty(config.getS3().getAccessKey())) {
				log.error(0,
						messageResources.getMessage(
								"kintone.csv.batch.error.config.s3.accesskey", config.getS3().getAccessKey()),
						"config file item error");
				isError = true;
			}

			if (StringUtils.isEmpty(config.getS3().getSecretKey())) {
				log.error(0,
						messageResources.getMessage(
								"kintone.csv.batch.error.config.s3.secretkey", config.getS3().getAccessKey()),
						"config file item error");
				isError = true;
			}

			if (StringUtils.isEmpty(config.getS3().getEndPointUrl())) {
				log.error(0,
						messageResources.getMessage(
								"kintone.csv.batch.error.config.s3.endpointurl", config.getS3().getAccessKey()),
						"config file item error");
				isError = true;
			}

			if (StringUtils.isEmpty(config.getS3().getRegion())) {
				log.error(0,
						messageResources.getMessage(
								"kintone.csv.batch.error.config.s3.region", config.getS3().getAccessKey()),
						"config file item error");
				isError = true;
			}

			if (StringUtils.isEmpty(config.getS3().getBucketName())) {
				log.error(0,
						messageResources.getMessage(
								"kintone.csv.batch.error.config.s3.bucketname", config.getS3().getAccessKey()),
						"config file item error");
				isError = true;
			}

			if (!folder.isDirectory()) {
				log.error(0,
						messageResources.getMessage(
								"kintone.csv.batch.error.export.folder.notfound", folderPath),
						"config file item error");
				isError = true;
			}

			if (StringUtils.isEmpty(fileEncode)) {
				log.error(0,
						messageResources.getMessage(
								"kintone.csv.batch.error.config.encode", config.getS3().getAccessKey()),
						"config file item error");
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

			log.info(0,
					messageResources.getMessage("kintone.csv.batch.export.start"), "start log.");

			//*********************************************************************************
			// S3サーバに接続し、制御ファイルを確認する。
			//*********************************************************************************
			S3 s3Config = config.getS3();
			final String ACCESS_KEY = s3Config.getAccessKey();
			final String SECRET_KEY = s3Config.getSecretKey();
			final String ENDPOINT_URL = s3Config.getEndPointUrl();
			final String REGION = s3Config.getRegion();
			final String bucketName = s3Config.getBucketName();

			AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
			ClientConfiguration clientConfig = new ClientConfiguration();
			clientConfig.setProtocol(Protocol.HTTPS);
			clientConfig.setConnectionTimeout(10000);

			EndpointConfiguration endpointConfiguration = new EndpointConfiguration(ENDPOINT_URL, REGION);
	        client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withClientConfiguration(clientConfig)
                    .withEndpointConfiguration(endpointConfiguration).build();

	        AccessControlList acl = new AccessControlList();
	        String[] accessUserEmails = s3Config.getAccessUserEmails();
	        for (int i = 0; i < accessUserEmails.length; i++) {
	        	acl.grantPermission(new EmailAddressGrantee(accessUserEmails[i]), Permission.FullControl);
	        	////acl.grantPermission(new CanonicalGrantee(accessUserEmails[i]), Permission.FullControl);
	        }

			//*********************************************************************************
			// 各kintoneサーバの処理
			//*********************************************************************************
	        KintoneHost host = config.getKintoneHost();
			try {
	            final String KINTONE_FOLDER_PATH =
	            		new StringBuilder("sfdc_kintone/").append(host.getS3FoldarName()).append("/kintone/").toString();

				final int recCountLimit = fileInfo.getRecordCountByOneFile();

		        // FINISHEDファイルが置かれているか確認する。
		        final String DATA_FOLDER_PATH =
		        		new StringBuilder("sfdc_kintone/").append(host.getS3FoldarName()).append("/kintone/data/").toString();
	 	        ObjectListing result = client.listObjects(bucketName, DATA_FOLDER_PATH + "FINISHED");

	            // FINISHEDファイルが無ければ処理をしない。
	            if (result.getObjectSummaries().size() == 0) {
	            	log.warn(0,
	            			messageResources.getMessage("kintone.csv.batch.export.stop.under.control", host.getFqdn()),
	            			"stop by control file");
	            	return;
	            }

	            HostRecordLog hostRecLog = HostRecordLog.getInstance(host.getFqdn(), Operation.EXPORT);
	            Date hostRecLogDate = new Date();
	            String lastDtStr = hostRecLog.getLastDate();

				//*********************************************************************************
				// FINISHEDファイルを削除する。
				//*********************************************************************************
	            client.deleteObject(bucketName, DATA_FOLDER_PATH + "FINISHED");

				//*********************************************************************************
				// S3サーバに制御ファイルをアップロードする。
				//*********************************************************************************
	            Utils.amazonS3PutObject(client, bucketName, DATA_FOLDER_PATH + "EXPORTING", "", acl, "https://" + ENDPOINT_URL);

				//*********************************************************************************
				// 行う作業や対象アプリケーションを整理する。
	            // カスタマー管理以外は入力のみ。
				//*********************************************************************************
				LinkedHashMap<KintoneJsonToCsvOperation, KintoneGetRequest> map = new LinkedHashMap<KintoneJsonToCsvOperation, KintoneGetRequest>();

				// Lead/Contact
				{
					KintoneJsonToCsvOperation ope = new LeadContactJsonToCsvOperation(host);
					KintoneGetRequest getRequest = new KintoneGetRequest(host, ope.getApp());
					StringBuilder condBldr = new StringBuilder("DeleteFlag = 0");
					if (!StringUtils.isEmpty(lastDtStr)) {
						condBldr.append(" and LastModifiedDate > \"").append(lastDtStr).append("\"");
					}
					getRequest.setCondition(condBldr.toString());

					if (!host.getManageUserName().equals("")) {
					    // 取得
					    KintoneApplication app = host.getApp("leadContact");
					    KintoneFieldsRequest getReq = new KintoneFieldsRequest(host, app);
					    getReq.setParameter(new JSONObject().put("app", app.getAppNo())	);
					    http = getReq.apply("GET");

					    // 読み込み。
					    JSONObject prevDesignJson = Utils.httpURLConnectionToJson(http).getJSONObject("properties");
					    Iterator<?> keys = prevDesignJson.keys();
					    ope.addTitle(keys);
					}

					map.put(ope, getRequest);
				}

				// Lead/Contact削除データ
				{
					KintoneJsonToCsvOperation ope = new LeadContactJsonToDeleteCsvOperation(host);
					KintoneGetRequest getRequest = new KintoneGetRequest(host, ope.getApp());
					StringBuilder condBldr = new StringBuilder("DeleteFlag = 1");
					if (!StringUtils.isEmpty(lastDtStr)) {
						condBldr.append(" and DeletedDateTime > \"").append(lastDtStr).append("\"");
					}
					getRequest.setCondition(condBldr.toString());
					map.put(ope, getRequest);
				}

				String currentFilePath = "";
				List<String> createdFilePathList = new ArrayList<String>();
				boolean exportFlag = false;

				int count = 0;
				KintoneJsonToCsvOperation ope = null;
				KintoneGetRequest getRequest = null;
				OutputStreamWriter writer = null;
				CSVWriter csvwriter = null;  // add
				//String csvRecord = "";   // delete

				try {
					for (Map.Entry<KintoneJsonToCsvOperation,KintoneGetRequest> entry : map.entrySet()) {
						//*********************************************************************************
						// レコードを取得しながら、CSVを出力していく。
						// REST APIの仕様により、1回で全件取得出来ないため、
						// offsetをずらしながら取得件数0件になるまで取得する。
						//*********************************************************************************
						count = 0;

						ope = entry.getKey();
						getRequest = entry.getValue();
						http = getRequest.apply();
						//http.disconnect();

						while (true) {
							// JSONデータを読み込む。
							JSONObject responsJson = Utils.httpURLConnectionToJson(http);
							// totalCountには1回の取得分ではなく、条件に合致した全件数が設定される。
							int totalCount = responsJson.getInt("totalCount");
							//log.debug(0, "totalCount:" + totalCount, "debug");

							JSONArray recordArray = responsJson.getJSONArray("records");

							// 該当CSVファイルの初回のオープン
							if (count == 0) {
								currentFilePath = getFilePath(folderPath, ope.getFileName(), recCountLimit, 0, totalCount);
								writer = new OutputStreamWriter(new FileOutputStream(new File(currentFilePath)), fileEncode);
            					csvwriter = new CSVWriter(writer,',','"','"',config.getLineSeparator());  // add
            					//csvwriter = new CSVWriter(writer);  // add
								// タイトル行の出力
								writer.write(ope.getColumnNameCsv());
								writer.write(config.getLineSeparator());
							}

							// offsetの変更により、取得レコードが無くなればループを抜ける。
							if (recordArray.length() == 0) {
								csvwriter.close();  // add
								writer.close();
								createdFilePathList.add(currentFilePath);
								break;
							}

							for (int i = 0; i < recordArray.length(); i++) {
								// 1ファイルのレコード数に応じてファイルを開き直す。
								if (recCountLimit != 0 && count > 0 && count % recCountLimit == 0) {
									csvwriter.close();  // add
									writer.close();
									createdFilePathList.add(currentFilePath);
									currentFilePath = getFilePath(folderPath, ope.getFileName(), recCountLimit, count, totalCount);
									writer = new OutputStreamWriter(new FileOutputStream(new File(currentFilePath)), fileEncode);
									csvwriter = new CSVWriter(writer,',','"','"',config.getLineSeparator());  // add

									// タイトル行の出力
									writer.write(ope.getColumnNameCsv());
									writer.write(config.getLineSeparator());
								}

								JSONObject record = recordArray.getJSONObject(i);
								//csvRecord = ope.jsonToCsv(record);   // dekete
								String[] jsonList = ope.jsonToList(record);  // add
								csvwriter.writeNext(jsonList);  // add
								csvwriter.flush();              // add
								//writer.write(csvRecord);      // delete
								//writer.write(config.getLineSeparator());

								count++;
							}

							// 最大100件ずつレコードを取得しながら、CSVを出力していく。
							http.disconnect();
							getRequest.setOffset(count);
							http = getRequest.apply();
						}
					}
					exportFlag = true;
				} catch(Exception e) {
					int status = (http == null ? 0 : http.getResponseCode());
					String target = (ope == null ? "" : ope.getFileName());
					String message = messageResources.getMessage("kintone.csv.batch.error.export.csv",
							target, currentFilePath, count);
					log.error(status, message, "csv export error", e);
					return;

				} finally {
					if (http != null) {
						http.disconnect();
					}
					if (writer != null) {
						writer.close();
					}
				}

				if (exportFlag) {
					//*********************************************************************************
					// 作成したファイルをS3にアップロードする。
					// バックアップフォルダにもアップロードする。
					//*********************************************************************************
					// dataフォルダのアップロード先は、DATA_FOLDER_PATH + file.getName()
					// バックアップフォルダ名を確定する。
					String bkFolderPath = "";
					for (int i = 0; true; i++) {
						bkFolderPath = new StringBuilder(KINTONE_FOLDER_PATH).append("backup")
								.append(new SimpleDateFormat("/yyyyMMdd").format(new Date()))
								.append("_")
								.append(String.format("%04d", i + 1))
								.append("/").toString();
						ObjectListing backupresult = client.listObjects(bucketName, bkFolderPath);

			            if (backupresult.getObjectSummaries().size() == 0) {
			            	break;
			            }
					}

			        // アップロード実行
			        for (String filePath : createdFilePathList) {
			        	File file = new File(filePath);
			        	Utils.amazonS3PutObject(client, bucketName, bkFolderPath + file.getName(), file, acl, "https://" + ENDPOINT_URL);
			        	Utils.amazonS3PutObject(client, bucketName, DATA_FOLDER_PATH + file.getName(), file, acl, "https://" + ENDPOINT_URL);
			        }

			        if (createdFilePathList.size() > 0) {
			        	hostRecLog.save(hostRecLogDate);
			        }

			        // EXPORTINGファイルを削除する。
			        client.deleteObject(bucketName, DATA_FOLDER_PATH + "EXPORTING");
				}

			} catch(AmazonServiceException e) {
				int status = e.getStatusCode();
				String message = messageResources.getMessage("kintone.csv.batch.error.s3",
						host.getFqdn(),
						e.getErrorType().name(),
						e.getMessage());
				log.error(status, message, "s3 request error", e);
			} catch(Exception e) {
				int status = (http == null ? 0 : http.getResponseCode());
				String message = messageResources.getMessage("kintone.csv.batch.error.export.host", host.getFqdn());
				log.error(status, message, "host error", e);
			}

		} catch(Exception e) {
			log.error(0, "", "common error", e);
		} finally {
			if (http != null) {
				http.disconnect();
			}
			if (client != null) {
				client.shutdown();
			}
		}

		log.info(0, messageResources.getMessage("kintone.csv.batch.export.end"), "end log.");
	}

	/**
	 * ファイルの階層名を返します。
	 * @param folderPath 階層名
	 * @param fileInitial ファイル名の接尾語
	 * @param recCountLimit 1ファイル辺りの最大レコード数
	 * @param count 処理済みのレコード数
	 * @return ファイルの階層名
	 */
	static String getFilePath(String folderPath, String fileKey, int recCountLimit, int count, int totalCount) {
		StringBuilder s3FilePathBldr = new StringBuilder(folderPath).append(fileKey);
		if (recCountLimit < totalCount) {
			String no = String.format("%03d", (count / recCountLimit) + 1);
			s3FilePathBldr.append(".part").append(no);
		}

		s3FilePathBldr.append(".csv");
		return s3FilePathBldr.toString();
	}
}
