package kz.talipovsn.security;

import static android.os.Build.VERSION.SDK_INT;
import android.content.Context;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {


    private static final String PROFFIX4_SIGN = "pfB27YhhsCwDFkDj83XWhstVXhE=\n"; // Сигнатура программы

    // Проверка программы на взлом
    public static boolean checkSignature(Context context) {
        String sign = getSignature(context); // Получение сигнатуры программы
        return PROFFIX4_SIGN.equals(sign); // Сравнение сигнатуры программы с оригинальной
    }

    // Проверка программы на установку из официального магазина Play Market
    public static boolean checkInstaller(Context context) {
        try {
            final String installedPackageName = getInstallerPackageName(context, context.getPackageName()); // Получение имени установщика
            if (installedPackageName == null) return false; // Если установщик не найден, то выход
            return installedPackageName.startsWith("com.android.vending"); // Проверка установщика
        } catch (Exception e) {
            return false; // Если произошла ошибка, то выход
        }
    }

    // Проверка программы на установку из официального магазина Play Market
    public static boolean checkAPKInstaller(Context context) {
        try {
            final String installedPackageName = getInstallerPackageName(context, context.getPackageName()); // Получение имени установщика
            if (installedPackageName == null) return false; // Если установщик не найден, то выход
            return installedPackageName.startsWith("com.android.packageinstaller"); // Проверка установщика
        } catch (Exception e) {
            return false; // Если произошла ошибка, то выход
        }
    }



    // Получение имени установщика программы
    private static String getInstallerPackageName(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager(); // Получение менеджера пакетов
            if (SDK_INT >= Build.VERSION_CODES.R) { // Если версия Android 11 и выше
                InstallSourceInfo info = pm.getInstallSourceInfo(packageName); // Получение информации об установщике
                if (info != null) { // Если информация найдена
                    return info.getInitiatingPackageName(); // Возврат имени установщика
                }
            }
            return pm.getInstallerPackageName(packageName); // Возврат имени установщика (для версий Android ниже 11)
        } catch (PackageManager.NameNotFoundException e) {
            return ""; // Возврат пустой строки в случае ошибки
        }
    }

    // Получение сигнатуры (подписи) пакета (программы *.apk)
    public static String getSignature(Context context) {
        String apkSignature = ""; // Сигнатура программы
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES); // Получение информации о пакете
            for (Signature signature : packageInfo.signatures) { // Перебор всех сигнатур
                MessageDigest md = MessageDigest.getInstance("SHA"); // Получение хеш-функции SHA
                md.update(signature.toByteArray()); // Обновление хеш-функции с данными
                apkSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT); // Перекодирование хеш-функции в строку
            }
            return apkSignature; // Возврат сигнатуры программы
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        EditText editText = findViewById(R.id.editText); // Получение ссылки на элемент EditText
        editText.setText(String.format("%s\n%s\n%s",
                getSignature(this), getInstallerPackageName(this, getPackageName()), getPackageName())); // Установка текста в элементе EditText

        if (!BuildConfig.DEBUG) { // Если запуск не в режиме отладки
            // Если программа была взломана ...
            if (!checkSignature(this)) {
                Toast.makeText(this, "Неверная сигнатура!", Toast.LENGTH_LONG).show();
                finish(); // Выход из программы
            }
        }

        // Если программа установлена не из Магазина ...
        if (!checkInstaller(this)) {
            Toast.makeText(this, "Программа установленна не из магазина...", Toast.LENGTH_LONG).show();
            //              finish(); // Выход из программы
        }

        // Если программа установлена из APK ...
        if (!checkAPKInstaller(this)) {
            Toast.makeText(this, "Программа установленна из APK...", Toast.LENGTH_LONG).show();
            //              finish(); // Выход из программы
        }

    }
}
