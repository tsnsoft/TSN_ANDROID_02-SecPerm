package kz.talipovsn.permissions;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    final String PSU_URL = "https://tou.edu.kz/images/links/gos_vestnik.png"; // Картинка для загрузки
    final int RCODE_LOADING = 1; // Константа для идентификации запроса разрешений
    String currentURL; // Переменная с URL-адресом загружаемой картинки
    private Button button; // Кнопка загрузки картинки

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        button = findViewById(R.id.button); // Получение кнопки загрузки картинки

        // Обработчик кнопки загрузки картинки
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentURL = PSU_URL; // Задаем URL-адрес картинки
                saveImage(currentURL); // Запускаем загрузку картинки
            }
        });
    }

    //-------------------------------------------------------

    // Проверка наличия конкретного разрешения у программы
    public static boolean hasPermission(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission); // Проверка наличия разрешения
        return res == PackageManager.PERMISSION_GRANTED; // Возвращение результата проверки
    }

    // Проверка наличия списка разрешений у программы
    public static boolean hasPermissionsList(Context context, String[] permissions) {
        boolean hasAllPermissions = true; // Переменная для хранения результата проверки
        for (String permission : permissions) { // Перебор списка разрешений
            if (!hasPermission(context, permission)) { // Если разрешение не найдено
                hasAllPermissions = false; // То устанавливаем флаг в false
                break; // И выходим из цикла
            }
        }
        return hasAllPermissions; // Возвращаем результат проверки

    }

    // Нужные разрешения для старых версий Android
    public static String[] storge_permissions = { // Массив с разрешениями
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // Запись во внешнее хранилище
            Manifest.permission.READ_EXTERNAL_STORAGE // Чтение из внешнего хранилища
    };

    // Нужные разрешения для Android 33
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU) // Указание версии Android
    public static String[] storge_permissions_33 = { // Массив с разрешениями
            Manifest.permission.READ_MEDIA_IMAGES, // Чтение изображений
            Manifest.permission.READ_MEDIA_AUDIO, // Чтение аудио
            Manifest.permission.READ_MEDIA_VIDEO // Чтение видео
    };

    // Определяем перечень нужных разрешений для текущей версии Android
    public static String[] permissions() {
        String[] p; // Переменная для хранения списка разрешений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Если версия Android 33 и выше
            p = storge_permissions_33; // То устанавливаем новый список разрешений
        } else {
            p = storge_permissions; // Иначе устанавливаем старый список разрешений
        }
        return p; // Возвращаем список разрешений
    }

    //-------------------------------------------------------

    // Вызывается сразу после установки разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Если вызывается наш запрос на разрешения
        if (requestCode == RCODE_LOADING) {
            // Если разрешения даны, то сохранить картинку
            if (hasPermissionsList(this, permissions())) {
                saveImage(currentURL); // Запускаем загрузку картинки
            }
        }
    }

    // Сохранение картинки с заданного адреса URL
    private void saveImage(String url) {
        try {
            // Проверяем, есть ли нужные разрешения, если нет то выдаем окно запроса с выходом из этого метода
            if (!hasPermissionsList(this, permissions())) {
                ActivityCompat.requestPermissions(this, permissions(), RCODE_LOADING); // Запрос разрешений
                return;
            }
            // Если адрес нормальный, то загружаем картинку
            if (URLUtil.isValidUrl(url)) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url)); // Создание запроса на загрузку
                request.allowScanningByMediaScanner(); // Разрешение сканирования загруженного файла
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // Уведомление о загрузке
                request.setDestinationInExternalPublicDir( // Установка пути для сохранения файла в папку "Pictures"
                        Environment.DIRECTORY_PICTURES, Uri.parse(url).getLastPathSegment());
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE); // Получение менеджера загрузок
                downloadManager.enqueue(request); // Запуск загрузки
                Toast.makeText(MainActivity.this, R.string.loaded, Toast.LENGTH_LONG).show(); // Вывод сообщения о успешной загрузке
            } else {
                Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_LONG).show(); // Вывод сообщения об ошибке
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_LONG).show(); // Вывод сообщения об ошибке
        }
    }



}
