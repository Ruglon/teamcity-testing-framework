package teamcity.ui.driverfactory;

import java.net.HttpURLConnection;
import java.net.URL;

public class SelenoidRemote {


    /**
     * Возвращает нормализованный URL WebDriver-хаба (…/wd/hub), если он жив (HTTP 200 на /status),
     * иначе возвращает null (значит едем локально).
     *
     * Логика:
     * 1) Если явно задано свойство (-Dselenide.remote или -Dremote) — пробуем его.
     * Если недоступен: либо падаем (если -DfailIfRemoteDown=true), либо фолбэк на локальный.
     * 
     * 2) Если явного нет и включён авто-поиск (-DautoDetectSelenoid=true) — пробуем localhost.
     */
    public static String chooseRemote() {
        String explicit = firstNonBlank(
                System.getProperty("selenide.remote"),
                System.getProperty("remote"),
                // если используете свой Config.getProperty — подставьте тут:
                null
        );

        if (isNotBlank(explicit)) {
            String url = normalizeHub(explicit);
            if (isAlive(statusUrl(url))) return url;

            if (Boolean.getBoolean("failIfRemoteDown")) {
                throw new IllegalStateException("Remote set but not reachable: " + url);
            } else {
                System.out.println("[SelenoidRemote] Remote is down, falling back to LOCAL: " + url);
                return null;
            }
        }

        // Авто-поиск локального Selenoid (опционально)
        if (Boolean.getBoolean("autoDetectSelenoid")) {
            String url = "http://localhost:4444/wd/hub";
            if (isAlive(statusUrl(url))) {
                System.out.println("[SelenoidRemote] Auto-detected local Selenoid at " + url);
                return url;
            }
        }
        return null;
    }

    // --- helpers ---
    private static String normalizeHub(String url) {
        return url.contains("/wd/hub") ? url : (url.endsWith("/") ? url + "wd/hub" : url + "/wd/hub");
    }

    private static String statusUrl(String hubUrl) {
        // проверяем именно /status у корня (без /wd/hub)
        String base = hubUrl.replace("/wd/hub", "");
        return base.endsWith("/") ? base + "status" : base + "/status";
    }

    private static boolean isAlive(String url) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(1000);
            c.setReadTimeout(1000);
            c.setRequestMethod("GET");
            int code = c.getResponseCode();
            return code >= 200 && code < 300;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isNotBlank(String s) { return s != null && !s.isBlank(); }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (isNotBlank(v)) return v;
        return null;
    }
}
