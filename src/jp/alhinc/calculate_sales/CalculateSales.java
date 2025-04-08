package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// 処理内容2-1
		// コマンドライン引数でディレクトリを指定
		File[] files = new File(args[1]).listFiles();
		List<File> rcdFiles = new ArrayList<>();

		// 指定したディレクトリから、拡張子がrcd、且つファイル名が数字8桁のファイルを検索
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().matches("^[0-9]{8}.rcd$")) {
				// 該当したファイルを、売上ファイルとしてListに保持
				rcdFiles.add(files[i]);
				System.out.println(rcdFiles.get(i));
			}
		}

		// 処理内容2-2
		// 売上ファイルを読み込み、支店コード、売上金額を抽出

		// 読み込んだファイルの中身（支店コード、売上金額）を格納するリスト（contents）を作成
		List<String> contents = new ArrayList<>();

		// rcdFilesリストについて、1行ずつ処理
		boolean readPossibility = false;
		BufferedReader br = null;
		for (int i = 0; i < rcdFiles.size(); i++) {
			try {
				File file = new File(args[1]);
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				String line;
				// 一行ずつ読み込む
				while ((line = br.readLine()) != null) {
					// 売上情報（支店コード、売上金額）を格納
					contents.add(line);
					System.out.println(line);
				}

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				readPossibility = false;
			} finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						readPossibility = false;
					}
				}
			}
			readPossibility = true;
		}

		// readFile(args[0], rcdFiles.get(i), fileSale, saleAmount);
		// long fileSale = Long.parseLong();
		// Long saleAmount = branchSales.get(key) + fileSale;

		// 抽出した売上額を該当する支店の合計金額にそれぞれ加算
		// 加算処理を売上ファイルの数だけ繰り返す

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// 処理内容1-2
				// items[0] には支店コード、items[1] には支店名を格納
				String[] items = line.split(",");

				// 支店情報（支店コード、支店名）を格納
				branchNames.put(items[0], items[1]);
				// 売上情報（支店コード、売上金額は一旦0）を格納
				branchSales.put(items[0], 0L);

				System.out.println(line);
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		// 処理内容3-1
		// コマンドライン引数で指定したディレクトリに支店別集計ファイルを作成
		// 支店別集計ファイルに、全支店の支店コード、支店名、合計金額を出力

		for (String key : branchNames.keySet()) {
			// 支店コード、支店名
			System.out.println(key + "," + branchNames.get(key));
			// 合計金額
			System.out.println();
		}

		return true;
	}

}
