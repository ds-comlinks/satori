package marketing.satori.kintone.csv.exelog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import marketing.satori.kintone.csv.config.Config;


public class HostRecordLog {
	private static final String DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	private String hostName;

	private Operation ope;

	private Date date;

	private HostRecordLog() {

	}

	public static HostRecordLog getInstance(String hostName, Operation ope) throws FileNotFoundException, IOException, ParseException {
		HostRecordLog log = new HostRecordLog();
		log.hostName = hostName;
		log.ope = ope;
		String filePath = getFilePath(hostName, ope);
		File file = new File(filePath);
		if (file.exists() && file.isFile()) {
			try(BufferedReader br = new BufferedReader(new FileReader(file));) {
				SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
				String line = br.readLine();
				if (!StringUtils.isEmpty(line)) {
					log.date = sdf.parse(line);
				}
			}
		}

		return log;
	}

	public String getLastDate() {
		if (this.date == null) {
			return "";
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
			return sdf.format(this.date);
		}
	}

	public void save(Date newDate) throws IOException {
		String filePath = getFilePath(this.hostName, this.ope);
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
		try(PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(filePath))));) {
			writer.println(sdf.format(newDate));
		}
	}

	private static String getFilePath(String hostName, Operation ope) {
		Config config = Config.getInstance();
		return new StringBuilder(config.getExportInfo().getFolderPath())
				.append(hostName)
				.append("_")
				.append(ope.toString())
				.append(".dat").toString();
	}
}

