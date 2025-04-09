package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
	private static final String FILE_NONSEQENTIAL = "売上ファイル名が連番になっていません";

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
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<>();

		// 指定したディレクトリから、拡張子がrcd、且つファイル名が数字8桁のファイルを検索
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().matches("^[0-9]{8}.rcd$")) {
				// 該当したファイルを、売上ファイルとしてListに保持
				rcdFiles.add(files[i]);
			}
		}

//		// 売上ファイルの連番チェック
//		for (int i = 0; i < rcdFiles.size() - 1; i++) {
//			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
//			int latter = Integer.parseInt(rcdFiles.get(i + 1)..substring(0, 8));
//
//			// 比較する2つのファイル名の先頭から数字の8文字を切り出し、int型に変換
//			if((latter - former) != 1) {
//				System.out.println(FILE_NONSEQENTIAL);
//			}
//
//		}

		// 処理内容2-2
		// 売上ファイルを読み込み、支店コード、売上金額を抽出

		// rcdFilesリストについて、1行ずつ処理
		// 抽出した売上額を該当する支店の合計金額にそれぞれ加算
		// 加算処理を売上ファイルの数だけ繰り返す
		boolean readPossibility = false;
		BufferedReader br = null;
		for (int i = 0; i < rcdFiles.size(); i++) {

			// 読み込んだファイルの中身（支店コード、売上金額）を格納するリスト（contents）を作成
			List<String> contents = new ArrayList<>();

			try {
				// ファイルをインスタンス化
				// パス＝コマンドライン引数、ファイル名＝売上ファイルListのi+1行目
				File file = new File(args[0], rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				String line;
				// 一行ずつ読み込む
				while ((line = br.readLine()) != null) {
					// 売上情報（支店コード、売上金額）を格納
					contents.add(line);
				}

				// ファイル1つ分の情報がリストに入った状態

				// ファイルの2行目（売上金額）をString型からLong型にキャスト
				long fileSale = Long.parseLong(contents.get(1));
				// ファイルの1行目（支店コード）をキーに合計売上金額を取得し、個別の売上金額と加算
				long saleAmount = branchSales.get(contents.get(0)) + fileSale;
				// 合計売上金額をMapにセット
				branchSales.put(contents.get(0), saleAmount);

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
			// ファイルの存在チェック
			if (!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// 処理内容1-2
				// items[0] には支店コード、items[1] には支店名を格納
				String[] items = line.split(",");
				//ファイルのフォーマットチェック
				if ((items.length != 2) || !(items[0].matches("[0-9]{3}"))) {
					System.out.println(FILE_INVALID_FORMAT);
				}

				// 支店情報（支店コード、支店名）を格納
				branchNames.put(items[0], items[1]);
				// 売上情報（支店コード、売上金額は一旦0）を格納
				branchSales.put(items[0], 0L);
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
		BufferedWriter bw = null;

		try {
			// コマンドライン引数で指定したパスにファイルを新規作成
			File outputFile = new File(path, fileName);
			outputFile.createNewFile();

			// ファイルへの書き込み準備
			FileWriter fw = new FileWriter(outputFile);
			bw = new BufferedWriter(fw);

			// ファイルへの書き込み処理
			for (String key : branchNames.keySet()) {
				// 出力内容：支店コード,支店名,合計金額
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				// 改行
				bw.newLine();
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
		} finally {
			// ファイルを開いている場合
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}

		return true;
	}

}
