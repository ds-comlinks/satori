package marketing.satori.kintone.csv;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.util.MessageResources;
import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.services.s3.AmazonS3;

import marketing.satori.kintone.csv.app.KintoneDeployRequest;
import marketing.satori.kintone.csv.app.KintoneFieldsRequest;
import marketing.satori.kintone.csv.app.KintoneLayoutRequest;
import marketing.satori.kintone.csv.config.Config;
import marketing.satori.kintone.csv.config.FileInfo;
import marketing.satori.kintone.csv.config.KintoneApplication;
import marketing.satori.kintone.csv.config.KintoneHost;
import marketing.satori.kintone.csv.log.Log;

public class FieldsImportBatch {

	/** ログオブジェクト */
	private static Log log = new Log(CsvExportBatch.class.getName());

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

			FileInfo fileInfo = config.getFieldsImportInfo();

			String fileEncode = fileInfo.getEncode();

			//*********************************************************************************
			// 設定ファイル項目のうち、サーバ接続前に確認可能な項目をチェックする。
			//*********************************************************************************
			String folderPath = fileInfo.getFolderPath();
			File folder = new File(folderPath);
			if (!folder.isDirectory()) {
				log.error(0,
						messageResources.getMessage(
								"kintone.csv.batch.error.export.folder.notfound", folderPath),
						"config file item error");
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

			log.info(0, messageResources.getMessage("kintone.csv.batch.fieldsimport.start"), "start log.");

			//*********************************************************************************
			// 各kintoneサーバの処理
			//*********************************************************************************
            KintoneHost host = config.getKintoneHost();
			List<String> appStrList = Arrays.asList(
				new String[]{
						"leadContact",
						"account",
						"opportunity",
						"campaign",
						"segment",
						"wRSegment",
						"wRNoSegment",
						"campaignRecord",
						"form",
						"formRecord"
				});
			for (String appStr : appStrList) {
				KintoneApplication app = host.getApp(appStr);
				try {

					//*********************************************************************************
		            // ソースjsonファイルを取得する。
					//*********************************************************************************
					// fields
					File fieldsFile = new File(folderPath + appStr + "_fields.json");
					if (fieldsFile.isDirectory() || !fieldsFile.exists()) {
						// ファイルが存在しない場合はスキップする。
						continue;
					}

					// layout
					File layoutFile = new File(folderPath + appStr + "_layout.json");
					if (layoutFile.isDirectory() || !layoutFile.exists()) {
						// ファイルが存在しない場合はスキップする。
						continue;
					}


					//*********************************************************************************
		            // fieldsを処理する。
					//*********************************************************************************
					{
						String json = Utils.loadJsonString(fieldsFile);
						JSONObject fileJson = new JSONObject(json).getJSONObject("properties");

						//*********************************************************************************
			            // 既存の設計情報を取得する。
						//*********************************************************************************
						// 取得。
						KintoneFieldsRequest getReq = new KintoneFieldsRequest(host, app);
						getReq.setParameter(new JSONObject()
								.put("app", app.getAppNo())
							);
						http = getReq.apply("GET");

						// 読み込み。
						JSONObject prevDesignJson = Utils.httpURLConnectionToJson(http).getJSONObject("properties");

						//*********************************************************************************
			            // 追加用jsonと更新用jsonを作成し処理する。
						//*********************************************************************************
						JSONObject postJson = new JSONObject();
						JSONObject putJson = new JSONObject();
						JSONObject deleteJson = new JSONObject();
						{
							// ソースjsonファイルの内容の内、
							// 既存の設計情報に無いものは追加用json、
							// 既存の設計情報に有るものは更新用jsonで処理する。
							JSONObject postJsonProps = new JSONObject();
							JSONObject putJsonProps = new JSONObject();
							boolean postFlag = false;
							boolean putFlag = false;
							Iterator<?> keys = fileJson.keys();
							while (keys.hasNext()) {
								String key = (String)keys.next();
								JSONObject valueObj = fileJson.getJSONObject(key);
								if (!prevDesignJson.has(key)) {
									// 追加
									postJsonProps.put(key, valueObj);
									postFlag = true;
								} else {
									// 更新
									putJsonProps.put(key, valueObj);
									putFlag = true;
								}
							}
							if (postFlag) {
								postJson.put("app", app.getAppNo());
								postJson.put("properties", postJsonProps);

								KintoneFieldsRequest req = new KintoneFieldsRequest(host, app);
								req.setParameter(postJson);
								req.apply("POST");
							}
							if (putFlag) {
								putJson.put("app", app.getAppNo());
								putJson.put("properties", putJsonProps);

								KintoneFieldsRequest req = new KintoneFieldsRequest(host, app);
								req.setParameter(putJson);
								req.apply("PUT");
							}
						}

						//*********************************************************************************
			            // ソースjsonファイルに無いフィールドを削除する。
						//*********************************************************************************
						{
							JSONArray deleteJsonFields = new JSONArray();
							boolean deleteFlag = false;
							Iterator<?> keys = prevDesignJson.keys();
							while (keys.hasNext()) {
								String key = (String)keys.next();
								if (!fileJson.has(key)) {
									// 削除
									deleteJsonFields.put(key);
									deleteFlag = true;
								}
							}
							if (deleteFlag) {
								deleteJson.put("app", app.getAppNo());
								deleteJson.put("fields", deleteJsonFields);

								KintoneFieldsRequest delReq = new KintoneFieldsRequest(host, app);
								delReq.setParameter(deleteJson);
								delReq.apply("DELETE");
							}
						}
					}

					//*********************************************************************************
		            // layoutを処理する。
					//*********************************************************************************
					{
						// JSONデータを読み込む。
						String json = Utils.loadJsonString(layoutFile);
						JSONObject fileJson = new JSONObject(json);

						JSONObject putJson = new JSONObject();
						putJson.put("app", app.getAppNo());
						putJson.put("layout", fileJson.getJSONArray("layout"));

						KintoneLayoutRequest req = new KintoneLayoutRequest(host, app);
						req.setParameter(putJson);
						req.apply("PUT");
					}

					//*********************************************************************************
		            // 変更をcommitする。
					//*********************************************************************************
					{
						KintoneDeployRequest reqest = new KintoneDeployRequest(host, app, false);
						reqest.apply();
					}

				} catch (Exception e) {
					int status = (http == null ? 0 : http.getResponseCode());
					String message = messageResources.getMessage("kintone.csv.batch.error.fieldsimport.host", host.getFqdn());
					log.error(status, message, "host error", e);

					//*********************************************************************************
		            // 変更をRollbackする。
					//*********************************************************************************
					{
						KintoneDeployRequest reqest = new KintoneDeployRequest(host, app, true);
						reqest.apply();
					}
				}
			}

			log.info(0, messageResources.getMessage("kintone.csv.batch.fieldsimport.end"), "end log.");


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
}
