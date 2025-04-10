package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// 商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LST = "commodity.lst";

	// 商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "が存在しません";
	private static final String FILE_INVALID_FORMAT = "のフォーマットが不正です";
	private static final String BRANCH_CODE_IS_INVALID = "の支店コードが不正です";
	private static final String COMMODITY_CODE_IS_INVALID = "の商品コードが不正です";
	private static final String FILE_NONSEQENTIAL = "売上ファイル名が連番になっていません";
	private static final String SALEAMOUNT_MAXLENGTH = "合計金額が10桁を超えました";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {

		// コマンドライン引数の存在チェック
		// 売上集計システムファイルのパスが1つだけ設定されている想定
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();
		// 商品コードと売上金額を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		// 支店コードのフォーマット：3桁の数字
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, "支店定義ファイル", "^[0-9]{3}$",
				branchNames, branchSales)) {
			return;
		}

		// 商品定義ファイル読み込み処理
		// 商品コードのフォーマット：英数字8桁
		if (!readFile(args[0], FILE_NAME_COMMODITY_LST, "商品定義ファイル", "^([a-zA-Z0-9]{8})$",
				commodityNames, commoditySales)) {
			return;
		}

		// 処理内容2-1
		// コマンドライン引数でディレクトリを指定
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFilesList = new ArrayList<>();

		// 指定したディレクトリから、拡張子がrcd、且つファイル名が数字8桁のファイルを検索
		// getNameメソッドはファイルとディレクトリの操作が可能であるため、ファイルであることを明示
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile() && files[i].getName().matches("^[0-9]{8}[.]rcd$")) {
				// 該当したファイルを、売上ファイルとしてListに保持
				rcdFilesList.add(files[i]);
			}
		}

		// 売上ファイルの連番チェック
		// OS問わず動作させるため、一律で昇順にソート
		Collections.sort(rcdFilesList);
		// 比較する2つのファイル名の先頭から数字の8文字を切り出し、int型に変換
		for (int i = 0; i < rcdFilesList.size() - 1; i++) {
			int former = Integer.parseInt(rcdFilesList.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFilesList.get(i + 1).getName().substring(0, 8));

			if ((latter - former) != 1) {
				System.out.println(FILE_NONSEQENTIAL);
				return;
			}
		}

		// 処理内容2-2
		// 売上ファイルを読み込み、支店コード、商品コード売上金額を抽出

		// rcdFilesリストについて、1行ずつ処理
		// 抽出した売上額を該当する支店の合計金額にそれぞれ加算
		// 加算処理を売上ファイルの数だけ繰り返す

		// メソッド化を視野に入れているため、売上ファイルの読み込み可否を保持する変数を作成
		// コーチ了承済
		boolean readPossibility = false;
		BufferedReader br = null;
		for (int i = 0; i < rcdFilesList.size(); i++) {

			// 読み込んだファイルの中身（支店コード、商品コード、売上金額）を格納するリスト（contents）を作成
			// for文の中で宣言することにより、ファイルを1つ読み終わるごとに中身が初期化される
			List<String> contentsList = new ArrayList<>();

			try {
				// ファイルのi行目を取得
				File file = rcdFilesList.get(i);
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				String line;
				// ファイルの中身を一行ずつ読み込む
				while ((line = br.readLine()) != null) {
					// 売上情報（支店コード、商品コード、売上金額）を格納
					contentsList.add(line);
				}

				// ファイル1つ分の情報がリストに入った状態

				// ファイルの中身をチェック
				// ①フォーマットが合っているか
				// ②1行目が正しい支店コードか
				// ③2行目が正しい商品コードか
				// ④3行目が数字かの順

				// 売上ファイルのフォーマットチェック
				// 3行でない場合エラー
				if (contentsList.size() != 3) {
					System.out.println(file.getName() + FILE_INVALID_FORMAT);
					readPossibility = false;
					return;
				}

				// Keyの存在チェック
				// 1行目（支店コード）が支店定義ファイルに存在しない場合エラー
				if (!branchNames.containsKey(contentsList.get(0))) {
					System.out.println(file.getName() + BRANCH_CODE_IS_INVALID);
					readPossibility = false;
					return;
				}

				// 2行目（商品コード）が商品定義ファイルに存在しない場合エラー
				if (!commodityNames.containsKey(contentsList.get(1))) {
					System.out.println(file.getName() + COMMODITY_CODE_IS_INVALID);
					readPossibility = false;
					return;
				}

				// 売上金額の数字チェック
				// Long型にキャストする前に行う必要がある
				// 正規表現の+：数字が1桁以上の文字列の場合true
				if (!contentsList.get(2).matches("^[0-9]+$")) {
					System.out.println(UNKNOWN_ERROR);
					readPossibility = false;
					return;
				}

				// ファイルの3行目（売上金額）をString型からLong型にキャスト
				long fileSale = Long.parseLong(contentsList.get(2));

				// ここまでは支店/商品定義ファイルで共通のチェック・処理

				// ファイルの1行目（支店コード）をキーに支店別合計売上金額を取得し、個別の売上金額と加算
				long branchSaleAmount = branchSales.get(contentsList.get(0)) + fileSale;
				// ファイルの2行目（商品コード）をキーに商品別合計売上金額を取得し、個別の売上金額と加算
				long commoditySaleAmount = commoditySales.get(contentsList.get(1)) + fileSale;

				// 支店・商品それぞれで合計金額の最大桁チェック（10桁超でアウト）
				if (branchSaleAmount >= 10000000000L || commoditySaleAmount >= 10000000000L) {
					System.out.println(SALEAMOUNT_MAXLENGTH);
					readPossibility = false;
					return;
				}

				// 支店別合計売上金額をMapにセット
				branchSales.put(contentsList.get(0), branchSaleAmount);
				// 商品別合計売上金額をMapにセット
				commoditySales.put(contentsList.get(1), commoditySaleAmount);

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				readPossibility = false;
				return;
			} finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						readPossibility = false;
						return;
					}
				}
			}
			readPossibility = true;
		}

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

		// 商品別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales)) {
			return;
		}
	}

	/**
	 * 支店/商品定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param ファイル名（日本語）
	 * @param 正規表現によるコードのフォーマット
	 * @param 支店/商品コードと、支店/商品名を保持するMap
	 * @param 支店/商品コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, String fileNameJp, String regEx,
			Map<String, String> namesMap, Map<String, Long> salesMap) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			// ファイルの存在チェック
			if (!file.exists()) {
				System.out.println(fileNameJp + FILE_NOT_EXIST);
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// 処理内容1-2
				// items[0] には支店/商品コード、items[1] には支店/商品名を格納
				String[] items = line.split(",");

				// ファイルのフォーマットチェック
				// 2行かつ1行目がフォーマットに合致しない場合エラー
				if ((items.length != 2) || !(items[0].matches(regEx))) {
					System.out.println(fileNameJp + FILE_INVALID_FORMAT);
					return false;
				}

				// 商品情報（支店/商品コード、支店/商品名）を格納
				namesMap.put(items[0], items[1]);
				// 売上情報（支店/商品コード、売上金額は一旦0）を格納
				salesMap.put(items[0], 0L);
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
	 * 支店・商品別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店/商品コードと、支店/商品名を保持するMap
	 * @param 支店/商品コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> namesMap,
			Map<String, Long> salesMap) {
		// 処理内容3-1
		// コマンドライン引数で指定したディレクトリに支店/商品別集計ファイルを作成
		// 支店別集計ファイル：全支店の支店コード、支店名、合計金額を出力
		// 商品別集計ファイル：全商品の商品コード、商品名、合計金額を出力

		BufferedWriter bw = null;

		try {
			// コマンドライン引数で指定したパスにファイルを新規作成
			// 指定したパスにファイルがない場合、わざわざcreateNewFileしなくても新規で作成してくれる
			File outputFile = new File(path, fileName);

			// ファイルへの書き込み準備
			FileWriter fw = new FileWriter(outputFile);
			bw = new BufferedWriter(fw);

			// ファイルへの書き込み処理
			for (String key : namesMap.keySet()) {
				// 出力内容：支店/商品コード,支店/商品名,合計金額
				bw.write(key + "," + namesMap.get(key) + "," + salesMap.get(key));
				// 改行
				bw.newLine();
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
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