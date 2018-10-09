package marketing.satori.kintone.csv;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.util.MessageResources;

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
import com.amazonaws.services.s3.model.Permission;

import marketing.satori.kintone.csv.app.KeyRecordIdMap;
import marketing.satori.kintone.csv.app.KintoneDeleteRequest;
import marketing.satori.kintone.csv.config.Config;
import marketing.satori.kintone.csv.config.FileInfo;
import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;
import marketing.satori.kintone.csv.config.S3;
import marketing.satori.kintone.csv.log.Log;

public class AllDeleteBatch {

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
			// S3サーバに接続。
			//*********************************************************************************
			S3 s3Config = config.getS3();
			final String ACCESS_KEY = s3Config.getAccessKey();
			final String SECRET_KEY = s3Config.getSecretKey();
			final String ENDPOINT_URL = s3Config.getEndPointUrl();
			final String REGION = s3Config.getRegion();

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
	        }

			//*********************************************************************************
			// 各kintoneサーバの処理
			//*********************************************************************************
	        KintoneHost host = config.getKintoneHost();
	        try {
				KintoneApplication[] apps = {
						host.getLeadContact()
						,host.getCampaign()
						,host.getSegment()
						,host.getWRSegment()
						,host.getWRNoSegment()
						,host.getCampaignRecord()
						,host.getForm()
						,host.getFormRecord()};

				for (KintoneApplication app : apps) {
					log.info(0, "初期化中", "初期化中");
					KeyRecordIdMap keyData = new KeyRecordIdMap(host, app);
					keyData.loadKeyRecordMap(new String[]{});

		            KintoneDeleteRequest delReq = new KintoneDeleteRequest(host, app, keyData);
		            {
		                int count = 0;
		                for (String id : keyData.getAllIdsString()) {
		                	delReq.put(Integer.parseInt(id));
		                	count++;
		                	if (count > 0 && count % 100 == 0) {
		                		delReq.apply();
		                		delReq.clear();
			                	log.info(0, "削除中..." + count + "件処理しました。", "削除中");
		                	}
		                }
		                if (count % 100 != 0) {
		            		delReq.apply();
		            		delReq.clear();
		                }
		            }
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
	}
}
