package ru.pusher;

import org.apache.log4j.Logger;
import ru.pusher.model.SystemType;
import ru.pusher.service.IPushService;
import ru.pusher.service.impl.AndroidPushServiceImpl;
import ru.pusher.service.impl.IPhonePushServiceImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class Pusher {
  private static final Logger logger = Logger.getLogger(Pusher.class);
  private static final String IPHONE_CERT_PATH = "iphone.cert.path";
  private static final String IPHONE_CERT_PWD = "iphone.cert.pwd";
  private static final String IPHONE_CERT_PRODUCTION = "iphone.cert.production";
  private static final String GOOGLE_SERVER_KEY = "google.server.key";
  private static final String ANDROID_SEND_PUSH_RETRY_COUNT = "android.send.push.retry.count";

  private static Properties properties;
  private static int androidSendPushRetryCount = 1;
  private static boolean iphoneCertProduction = false;

  public static void main(String[] args) {
    try {
      loadProperties();
    } catch (Exception e) {
      logger.error(e);
      return;
    }

    if (args == null || args.length < 3) {
      logger.error("Аргументы отсутсвуют, либо их неверное количество.");
      logger.error("Первым аргумент необходимо описать тип системы на которую отправляется push: ios/android");
      logger.error("Второй аргумент должен соответстовать pushId/registrationId девайса");
      logger.error("Третим аргументом должен быть текст отправляемого сообщения, указывается в кавычках");
      return;
    }

    SystemType systemType;
    try {
      systemType = SystemType.valueOf(args[0].toUpperCase());
    } catch (Exception e) {
      logger.error("Ошибка в типе системе, на которую отправляет push. Допустимы значения: ios, android", e);
      return;
    }

    String pushId = args[1];
    String message = args[2];

    IPushService pushService = SystemType.ANDROID.equals(systemType) ? new AndroidPushServiceImpl() : new IPhonePushServiceImpl();

    logger.info("Отправляем сообщение \"" + message + "\" на девайс " + pushId);
    try {
      pushService.sendPush(pushId, message);
    } catch (Exception e) {
      logger.error("Ошибка во время отправки push уведомления", e);
    }
  }

  private static void loadProperties() throws Exception {
    InputStream is = null;
    try {
      properties = new Properties();
      properties.load(is = new FileInputStream(Pusher.class.getSimpleName() + ".properties"));

      checkProperties();

      for (Map.Entry<Object, Object> property : properties.entrySet()) {
        String key = property.getKey().toString();
        String value = property.getValue().toString();

        if (IPHONE_CERT_PATH.equals(key) || IPHONE_CERT_PWD.equals(key) || GOOGLE_SERVER_KEY.equals(key)) continue;

        if (ANDROID_SEND_PUSH_RETRY_COUNT.equals(key)) {
          try {
            androidSendPushRetryCount = Integer.parseInt(value);
          } catch (NumberFormatException ignore) {}
        } if (IPHONE_CERT_PRODUCTION.equals(key)) {
          iphoneCertProduction = "yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
        } else {
          System.setProperty(key, value);
        }
      }
    } catch (Exception e) {
      throw new Exception("Не получилось загрузить pusher.properties", e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException ignore) { }
      }
    }
  }

  private static void checkProperties() throws Exception {
    if (properties.isEmpty()) throw new Exception("Файл с настройками пуст.");

    if (!properties.containsKey(IPHONE_CERT_PATH))
      throw new Exception("Отсутвует путь до ключа для отправки push'ей на iPhone. Название настройки: " + IPHONE_CERT_PATH);
    if (!properties.containsKey(IPHONE_CERT_PWD))
      throw new Exception("Отсутвует пароль для ключа для отправки pushe'ей на iPhone. Название настройки: " + IPHONE_CERT_PWD);
    if (!properties.containsKey(IPHONE_CERT_PRODUCTION)) {
      logger.warn("Для отправки push'ей на iPhone будет использована настройка по умолчанию production = " + iphoneCertProduction);
      logger.warn("Название настройки " + IPHONE_CERT_PRODUCTION);
    }
    if (!properties.containsKey(GOOGLE_SERVER_KEY))
      throw new Exception("Отсутствует серверный ключ Google. Название настройки: " + GOOGLE_SERVER_KEY);
    if (!properties.containsKey(ANDROID_SEND_PUSH_RETRY_COUNT)) {
      logger.warn("Отсутствует настройка количества попыток отсылки push уведомления для Android устройств " + ANDROID_SEND_PUSH_RETRY_COUNT +
              " Было установлено значение по умолчанию: " + androidSendPushRetryCount);
    }
  }

  public static String getIphoneCertPath() {
    return properties.getProperty(IPHONE_CERT_PATH);
  }

  public static String getIphoneCertPwd() {
    return properties.getProperty(IPHONE_CERT_PWD);
  }

  public static boolean getIphoneCertProduction() {
    return iphoneCertProduction;
  }

  public static String getGoogleServerKey() {
    return properties.getProperty(GOOGLE_SERVER_KEY);
  }

  public static int getAndroidSendPushRetryCount() {
    return androidSendPushRetryCount;
  }
}
