package marketing.satori.kintone.csv.log;


import org.apache.log4j.Logger;

/**
 *
 * log4jのロガーを本バッチ用にカスタマイズしたラッパークラスです。
 *
 */
public class Log {
	/** log4jのロガー */
	private Logger log;

	/**
	 * カテゴリ名を引数としてオブジェクトを作成します。
	 * @param カテゴリ名
	 */
	public Log(String categoryName) {
		log = Logger.getLogger(categoryName);
	}

	/**
	 * fatalログを出力します。
	 * @param status ステータス
	 * @param message メッセージ
	 * @param classification 分類
	 * @param e 例外オブジェクト
	 */
	public void fatal(int status, String message, String classification, Exception e) {
		LogElement elm = new LogElement(LogLevel.FATAL, status, message, classification, e);
		log.fatal(elm.toString());
	}

	/**
	 * 例外発生時のerrorログを出力します。
	 * @param status ステータス
	 * @param message メッセージ
	 * @param classification 分類
	 * @param e 例外オブジェクト
	 */
	public void error(int status, String message, String classification, Exception e) {
		LogElement elm = new LogElement(LogLevel.ERROR, status, message, classification, e);
		System.out.println(elm.toString());
		log.error(elm.toString());
	}

	/**
	 * 例外発生時以外のerrorログを出力します。
	 * @param status ステータス
	 * @param message メッセージ
	 * @param classification 分類
	 */
	public void error(int status, String message, String classification) {
		LogElement elm = new LogElement(LogLevel.ERROR, status, message, classification, 3);
		log.error(elm.toString());
	}

	/**
	 * warnログを出力します。
	 * @param status ステータス
	 * @param message メッセージ
	 * @param classification 分類
	 */
	public void warn(int status, String message, String classification) {
		LogElement elm = new LogElement(LogLevel.WARN, status, message, classification, 3);
		log.warn(elm.toString());
	}

	/**
	 * infoログを出力します。
	 * @param status ステータス
	 * @param message メッセージ
	 * @param classification 分類
	 */
	public void info(int status, String message, String classification) {
		LogElement elm = new LogElement(LogLevel.INFO, status, message, classification, 3);
		log.info(elm.toString());
	}

	/**
	 * debugログを出力します。
	 * @param status ステータス
	 * @param message メッセージ
	 * @param classification 分類
	 */
	public void debug(int status, String message, String classification) {
		LogElement elm = new LogElement(LogLevel.DEBUG, status, message, classification, 3);
		log.debug(elm.toString());
	}

	/**
	 * traceログを出力します。
	 * @param status ステータス
	 * @param message メッセージ
	 * @param classification 分類
	 */
	public void trace(int status, String message, String classification) {
		LogElement elm = new LogElement(LogLevel.TRACE, status, message, classification, 3);
		log.trace(elm.toString());
	}
}

