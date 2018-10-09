package marketing.satori.kintone.csv;

import org.apache.struts.util.MessageResources;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;

import marketing.satori.kintone.csv.config.KintoneHost;
import marketing.satori.kintone.csv.log.Log;

public class S3ClientRapper {
	public static interface Runner {
		public void run(AmazonS3 s3) throws AmazonServiceException;
	}

	public static boolean run(AmazonS3 s3, Runner r, Log log, KintoneHost host) {
		try {
			r.run(s3);
		} catch (AmazonServiceException e) {
			MessageResources messageResources = Utils.getMessageResources();
			String message = messageResources.getMessage("kintone.csv.batch.error.export.request",
					host.getFqdn(),
					e.getErrorType().name(),
					e.getMessage());
			log.error(e.getStatusCode(), message, "s3 error");
			return false;
		}
		return true;
	}
}
