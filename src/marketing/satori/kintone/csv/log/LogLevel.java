package marketing.satori.kintone.csv.log;

/**
 *
 * ログ出力されるログレベルです。
 *
 */
public enum LogLevel {
	/** ログレベル:fatal */
	FATAL("fatal"),

	/** ログレベル:error */
	ERROR("error"),

	/** ログレベル:warn */
	WARN("warn"),

	/** ログレベル:info */
	INFO("info"),

	/** ログレベル:debug */
	DEBUG("debug"),

	/** ログレベル:trace */
	TRACE("trace");

	/** ログレベルの文言 */
	private String value;

	/**
	 * ログレベル文言を引数としてオブジェクトを作成します。
	 * @param value ログレベル文言
	 */
	private LogLevel(String value) {
		this.value = value;
	}

	/**
	 * ログレベル文言を返します。
	 * @return ログレベル文言
	 */
	public String toString() {
		return this.value;
	}
}
