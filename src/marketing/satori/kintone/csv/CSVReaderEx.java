package marketing.satori.kintone.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.opencsv.CSVReader;

/**
 *
 * com.opencsv.CSVReaderを本バッチ用に拡張したクラスです。
 *
 */
public class CSVReaderEx extends CSVReader {

	/** タイトルを保持します。 */
	private Map<String, Integer> titleIndex = new HashMap<String, Integer>();

	/**
	 * java.io.Readerを引数として
	 * @param reader
	 */
	@SuppressWarnings("deprecation")
	public CSVReaderEx(Reader reader) {
		super(reader,',', '"', '\0');
	}

	/**
	 * 引数をタイトルとして読み込みます。
	 * @param titles タイトル
	 */
	public void loadTitle(String[] titles) {
		for (int i = 0; i < titles.length; i++) {
			titleIndex.put(titles[i], i);
		}
	}

	public Map<String, String> readNextMap() throws IOException {
		Map<String, String> record = new HashMap<String, String>();

		String[] next = super.readNext();
		if (next == null) {
			return null;
		} else {
			for (String title : titleIndex.keySet()) {
				record.put(title, next[titleIndex.get(title).intValue()]);
			}
			return record;
		}
	}

}
