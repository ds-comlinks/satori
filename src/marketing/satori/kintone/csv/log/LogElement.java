package marketing.satori.kintone.csv.log;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * ログのJSON要素を保持します。
 * toString()メソッドにより、JSON文字列を取得可能です。
 *
 * 	public static void main(String[] args) {
 *		func();
 *	}
 *
 *	public static void func() {
 *		LogElement elm = new LogElement(LogLevel.DEBUG, 400, "テスト", "テスト2");
 *		System.out.println(elm.toString());
 *
 *	}
 */
public class LogElement {
	/** ログレベル */
	private final LogLevel level;

	/** サービス(固定値) */
	public static final String SERVICE = "kintone";

	/** ステータス */
	private final int status;

	/** メッセージ */
	private final String message;

	/** 分類 */
	private final String classification;

	/** コールスタック */
	private final StackTraceElement[] callStack;

	/**
	 * ログ出力の必要項目でオブジェクトを初期化します。
	 * @param level
	 * @param status
	 * @param message
	 * @param classification
	 */
	public LogElement(LogLevel level, int status, String message, String classification, int skipStackTraceCount) {
		this.level = level;
		this.status = status;
		this.message = message;
		this.classification = classification;

		// スタックトレースの設定
		// 本メソッドとThread.currentThread().getStackTrace()のスタックトレースを削って設定する。
		StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
		this.callStack = Arrays.copyOfRange(callStack, skipStackTraceCount, callStack.length);
	}

	/**
	 * 例外ログ出力の必要項目でオブジェクトを初期化します。
	 * @param level
	 * @param status
	 * @param message
	 * @param classification
	 * @param e
	 */
	public LogElement(LogLevel level, int status, String message, String classification, Exception e) {
		this.level = level;
		this.status = status;
		this.message = new StringBuilder(e.getClass().getCanonicalName())
				.append(":")
				.append(e.getMessage())
				.append("\\n")
				.append(message).toString();
		this.classification = classification;
		this.callStack = e.getStackTrace();
	}

	/**
	 * {@inheritDoc}
	 * (JSONObjectのメソッドは例外を投げるため利用しません。)
	 */
	@Override
	public String toString() {
		StringBuilder callstackBuilder = new StringBuilder("[");
		for (int i = 0; i < this.callStack.length; i++) {
			if (i != 0) {
				callstackBuilder.append(",");
			}
			callstackBuilder.append("\"");
			callstackBuilder.append(this.callStack[i].toString());
			callstackBuilder.append("\"");
		}
		callstackBuilder.append("]");

		return MessageFormat.format("'{'level: \"{0}\", service: \"{1}\", status: {2}, message: \"{3}\", classification: \"{4}\", call_stack: {5}'}'",
				this.level.toString(),
				LogElement.SERVICE,
				this.status,
				this.message.replace("\"", "\\\""),
				this.classification.replace("\"", "\\\""),
				callstackBuilder.toString());
	}
}
