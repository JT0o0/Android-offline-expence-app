# 記帳本 Ledger

一款 **離線、單機** 的 Android 記帳 App，使用 **Kotlin + Jetpack Compose + Material 3**。

## 下載安裝

前往 **[Releases 頁面](https://github.com/JT0o0/Android-offline-expence-app/releases/latest)** 下載最新的 `.apk`，用手機瀏覽器開啟該檔即可安裝。

- 需求：Android 8.0（API 26）以上。
- 第一次安裝時，若系統提示，請允許「安裝未知來源的應用程式」。
- App 完全離線，不需要網路權限，資料只存在手機本機。

## 功能

- **內建計算機**：輸入金額時可直接做 `+ − × ÷`，即時顯示運算結果。
- **每日帳本**：依日分組、每日小計，新增時日期預設為今天。
- **收支分類**：內建分類，可自訂名稱 / 圖示 / 顏色。
- **多帳戶**：現金 / 銀行 / 信用卡分開記錄，即時結餘與總結餘。
- **統計圖表**：每月收支總覽、分類甜甜圈圖、每日趨勢長條圖。
- **主題與顏色自訂**：4 組典雅預設主題、淺/深色模式，並可單獨調整主色 / 輔色 / 背景 / 收入色 / 支出色（即時生效、重啟保留）。
- **備份與匯出**：匯出 CSV、完整 JSON 備份與還原（透過系統檔案選擇器，免儲存權限）。

## 技術架構

- **UI**：Jetpack Compose、Material 3、Navigation Compose、底部四分頁。
- **架構**：MVVM + Repository、單向資料流（StateFlow）。
- **資料**：Room（SQLite）離線儲存；金額以 `Long` 最小單位（分）儲存避免浮點誤差。
- **偏好/主題**：DataStore。
- **DI**：Hilt。
- **圖表**：Compose Canvas 自繪（無外部圖表相依）。

主要程式進入點：[`MainActivity`](app/src/main/java/com/toting/ledger/MainActivity.kt)、
[`AppNavHost`](app/src/main/java/com/toting/ledger/ui/navigation/AppNavHost.kt)、
主題系統 [`Theme.kt`](app/src/main/java/com/toting/ledger/ui/theme/Theme.kt)。

## 建置與執行

### 方式 A：Android Studio（建議）

1. 用 Android Studio 開啟此資料夾。
2. 等待 Gradle 同步；若提示缺少 SDK 平台 / build-tools，按提示安裝。
3. 建立一個模擬器（Device Manager → Create Device，建議 API 34+）或接上實體手機（開啟 USB 偵錯）。
4. 按 **Run ▶**。

### 方式 B：命令列

```powershell
# 於專案根目錄
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:assembleDebug          # 產生 debug APK
.\gradlew.bat test                         # 跑單元測試（計算機 / 金額格式）
```

`local.properties` 已指向本機 SDK；第一次建置會自動下載 Gradle 與相依套件。

### 產生簽章 Release APK（發佈用）

於 Android Studio：**Build → Generate Signed Bundle / APK → APK**，選擇（或建立）一組 keystore 完成簽章。輸出的 `.apk` 位於 `app/release/`。

> ⚠️ keystore（`*.jks`）與相關密碼**請自行妥善備份、切勿上傳**，`.gitignore` 已將其排除。發佈新版時沿用同一組 keystore，手機才能直接覆蓋更新、不必先解除安裝。

## 疑難排解：`Unable to establish loopback connection`

若建置時出現此錯誤，代表此環境的 **AF_UNIX（Unix domain socket）loopback 故障**（JDK 16+ 的 `Selector` 會用到它，且不會退回 TCP）。本機實測 TCP loopback 正常、AF_UNIX 失敗。可嘗試：

- 確認是否為**安全防護軟體**攔截了本機 socket；暫時停用後再試。
- 更新 / 修復 Windows（此機為 Windows 11 25H2, build 26200）。
- 若僅在某些受限環境（如沙箱）發生，於一般使用者終端機 / Android Studio 直接建置通常正常。

> 註：JDK 17、21 皆受影響（皆使用 AF_UNIX self-pipe），故換 JDK 版本無法解決；需從上述環境面處理。

## 專案結構

```
app/src/main/java/com/toting/ledger/
  data/   model · local(Room) · repository · datastore · backup
  di/     Hilt 模組
  ui/     theme · navigation · home · entry · stats · accounts · settings · components
  util/   DateUtils · MoneyFormatter · ExpressionEvaluator
```
