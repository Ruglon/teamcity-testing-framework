```markdown
# Полная Инструкция по Настройке Selenoid для Тестового Фреймворка (Для Новичков)

Эта инструкция предназначена для новичков ("чайников"), которые хотят настроить Selenoid для проекта автоматизированного тестирования в браузерах (на базе Selenium). Selenoid — альтернатива Selenium Grid, запускающая браузеры в Docker-контейнерах для изоляции, параллельного запуска и стабильности тестов. Инструкция адаптирована под Windows (с учётом PowerShell, путей с пробелами и особенностей Docker Desktop), но принципы применимы к другим ОС.

Предполагается, что у вас есть тестовый фреймворк (например, на Java с Selenium, как в `teamcity-testing-framework`). Добавьте эту инструкцию в README.md репозитория, чтобы любой скачавший проект мог легко настроить. Все команды вводите в PowerShell (Win + R → `powershell`) или Command Prompt (cmd.exe), от имени администратора. Для путей с пробелами используйте кавычки, например: `cd "C:\Users\Lian Li\IdeaProjects\teamcity-testing-framework"`.

## 1. Предварительные Требования
Перед началом убедитесь:
- **ОС:** Windows 10/11.
- **Docker:** Установите Docker Desktop с [официального сайта](https://www.docker.com/products/docker-desktop).
  - Проверьте: `docker --version` (должно вывести версию, e.g., "Docker version 27.1.1").
  - Добавьте пользователя в группу `docker-users` (чтобы не запускать от админа каждый раз):
    - В Command Prompt от админа: `net localgroup "docker-users" "ВашЛогин" /add` (узнайте логин: `whoami`).
    - Выйдите/войдите в систему.
  - Запустите Docker Desktop (иконка в трее).
- **Нет локальных браузеров:** Selenoid установит их в контейнерах.
- **Проект:** Перейдите в папку: `cd "C:\Users\Lian Li\IdeaProjects\teamcity-testing-framework"`.
- **Инструменты:** IntelliJ IDEA или аналог для редактирования кода.

Если Docker не работает: Проверьте в настройках Docker Desktop → General → Use WSL 2 based engine (рекомендуется для стабильности). Если ошибки — `docker info | grep Storage` (рекомендуется OverlayFS).

## 2. Установка Configuration Manager (CM)
CM — инструмент для автоматизации настройки Selenoid.

1. Скачайте CM в PowerShell (в папке проекта):
   ```
Invoke-WebRequest -Uri "https://github.com/aerokube/cm/releases/latest/download/cm_windows_amd64.exe" -OutFile "cm.exe"
   ```
   - Альтернатива: Скачайте вручную с [GitHub релизов](https://github.com/aerokube/cm/releases/latest), переименуйте в `cm.exe` и положите в папку проекта.
2. Проверьте: `dir` — файл `cm.exe` должен быть в списке. Нет нужды в `chmod` на Windows.
3. Если ошибка при запуске: Добавьте `.\` перед `cm.exe` (e.g., `.\cm.exe --version` для теста).

## 3. Настройка Файла Конфигурации (browsers.json)
Этот файл определяет браузеры и версии.

1. Создайте папку: `mkdir config`.
2. Создайте файл `config\browsers.json` в редакторе (Notepad, IntelliJ):
   ```
{
"chrome": {
"default": "latest",
"versions": {
"latest": {
"image": "selenoid/chrome:128.0",
"port": "4444",
"path": "/"
}
}
},
"firefox": {
"default": "latest",
"versions": {
"latest": {
"image": "selenoid/firefox:110.0",
"port": "4444",
"path": "/wd/hub"
}
}
}
}
   ```
   - **Версии:** Актуальны на 18 августа 2025 года (проверьте на [aerokube.com/images/latest](https://aerokube.com/images/latest/) для обновлений).
   - **Объяснение:** "image" — Docker-образ браузера; "port" — порт Selenoid; "path" — эндпоинт для Selenium. Добавьте другие браузеры (e.g., "edge") аналогично.
   - - Для параллельного запуска: Добавьте в запуск `--limit 5` (максимум сессий).

## 4. Скачивание Docker-Образов
Скачайте образы браузеров и Selenoid.

В PowerShell:
```
docker pull selenoid/chrome:128.0
docker pull selenoid/firefox:110.0
docker pull aerokube/selenoid:latest
docker pull selenoid/video-recorder:latest-release  # Для записи видео тестов
docker pull aerokube/selenoid-ui:latest  # Для UI мониторинга
```
- Это может занять 5-20 минут (образы ~1-2 ГБ каждый).
- Проверьте: `docker images` — список должен включать скачанные образы.
- Если ошибка "repository does not exist": Обновите версию (e.g., на 127.0 для Chrome). Если "access denied": `docker login` (с аккаунтом на Docker Hub).
- Альтернатива: CM скачает автоматически при запуске, но ручной pull ускоряет процесс.

## 5. Запуск Selenoid и UI
1. Запустите Selenoid:
   ```
.\cm.exe selenoid start --browsers-json config\browsers.json --vnc
   ```
   - `--vnc`: Включает просмотр экрана в реальном времени через UI.
   - Лог: Должен закончиться "Successfully started Selenoid" и "Listening on :4444". Если предупреждение про API версию — игнорируйте (использует дефолт 1.45).
   - Проверьте: `docker ps` — контейнер "selenoid" в статусе "Up". Откройте [http://localhost:4444/status](http://localhost:4444/status) — JSON с браузерами.

2. Запустите Selenoid UI (для визуального мониторинга сессий, видео, VNC-экранов):
   ```
.\cm.exe selenoid-ui start
   ```
   - Альтернатива (прямая Docker-команда для кастомизации):
     ```
     docker run -d --name selenoid-ui --link selenoid -p 8080:8080 aerokube/selenoid-ui --selenoid-uri http://selenoid:4444
     ```
     - Замените URI на IP хоста, если нужно (узнайте: `docker inspect selenoid -f "{{.NetworkSettings.Gateway}}"` — e.g., http://172.17.0.1:4444).
   - Откройте: [http://localhost:8080](http://localhost:8080) — дашборд с браузерами. Если тесты запущены, увидите сессии (кликом — VNC/видео).

- **Автоматизация:** Создайте `start-all.bat`:
  ```
@echo off
echo Starting Selenoid...
.\cm.exe selenoid start --browsers-json config\browsers.json --vnc
echo Starting UI...
.\cm.exe selenoid-ui start
echo Open http://localhost:8080
pause
  ```
  - Запускайте двойным кликом.

Остановка: `.\cm.exe selenoid stop` и `.\cm.exe selenoid-ui stop`. Если порт занят: `netstat -ano | findstr :4444` и убейте процесс в Task Manager.

## 6. Интеграция Selenoid в Тестовый Фреймворк
Тесты должны использовать RemoteWebDriver с URL `http://localhost:4444/wd/hub` вместо локального драйвера (e.g., ChromeDriver). Это обеспечит запуск в Docker-контейнерах Selenoid.

- **Зависимости (pom.xml для Maven):**
  ```
  <dependencies>
    <dependency>
      <groupId>org.seleniumhq.selenium</groupId>
      <artifactId>selenium-java</artifactId>
      <version>4.23.0</version>  <!-- Актуальная на 2025 -->
    </dependency>
  </dependencies>
  ```
  - Для parallel: Добавьте TestNG/JUnit с parallel конфигом.

- **Пример Кода Теста (Java, JUnit/TestNG):**
  ```java
  import org.openqa.selenium.WebDriver;
  import org.openqa.selenium.remote.DesiredCapabilities;
  import org.openqa.selenium.remote.RemoteWebDriver;
  import java.net.URL;
  import org.junit.Test;  // Или import org.testng.annotations.Test;

  public class SelenoidTestExample {
      @Test
      public void testInSelenoid() throws Exception {
          // URL Selenoid
          URL selenoidUrl = new URL("http://localhost:4444/wd/hub");

          // Capabilities (параметры браузера)
          DesiredCapabilities caps = new DesiredCapabilities();
          caps.setBrowserName("chrome");  // Или "firefox"
          caps.setVersion("128.0");  // Из browsers.json
          caps.setCapability("enableVNC", true);  // Просмотр экрана в UI
          caps.setCapability("enableVideo", true);  // Запись видео
          caps.setCapability("screenResolution", "1920x1080x24");  // Разрешение (опционально)
          caps.setCapability("sessionTimeout", "5m");  // Таймаут сессии

          // Запуск через Remote (тест в Docker)
          WebDriver driver = new RemoteWebDriver(selenoidUrl, caps);
          driver.get("https://example.com");
          System.out.println("Title: " + driver.getTitle());  // Пример действия
          // Добавьте asserts и другие шаги теста
          driver.quit();  // Завершите сессию
      }
  }
  ```
    - **Объяснение:** RemoteWebDriver отправляет команды в Selenoid, который запускает браузер в контейнере. Для Firefox измените browserName и version на "110.0".
    - Запуск: `mvn test` или через IntelliJ. В UI увидите сессию.

- **Гибкий Переключатель (Локально vs Selenoid):**
  Создайте `src/test/resources/config.properties`:
  ```
  webdriver.mode=remote  # Или local
  selenoid.url=http://localhost:4444/wd/hub
  browser.name=chrome
  browser.version=128.0
  ```
  В коде (в методе setup):
  ```java
  import java.io.FileInputStream;
  import java.util.Properties;

  Properties props = new Properties();
  props.load(new FileInputStream("src/test/resources/config.properties"));

  String mode = props.getProperty("webdriver.mode");
  WebDriver driver;
  if ("remote".equals(mode)) {
      URL url = new URL(props.getProperty("selenoid.url"));
      DesiredCapabilities caps = new DesiredCapabilities();
      caps.setBrowserName(props.getProperty("browser.name"));
      caps.setVersion(props.getProperty("browser.version"));
      caps.setCapability("enableVNC", true);
      driver = new RemoteWebDriver(url, caps);
  } else {
      // Локальный: driver = new ChromeDriver();
  }
  // ... тест ...
  driver.quit();
  ```
    - В README: "Измените mode на remote для Selenoid".

## 7. Интеграция с TeamCity
Для CI/CD в TeamCity:
1. Установите Docker на build agent.
2. В build configuration:
    - Шаг 1: Command Line — Запуск Selenoid: `cm.exe selenoid start --browsers-json config\browsers.json --vnc`.
    - Шаг 2: Command Line — Запуск UI: `cm.exe selenoid-ui start`.
    - Шаг 3: Maven/Gradle — Запуск тестов: `mvn test -Dwebdriver.mode=remote`.
3. Параметры: Передайте %system.selenoid.url% = http://localhost:4444/wd/hub.
4. Артефакты: Соберите видео из /opt/selenoid/video (в browsers.json добавьте "-video-output-dir /opt/selenoid/video").

- Параллель: В TestNG xml: <suite parallel="methods" thread-count="5">.

## 8. Отладка, Проблемы и Советы
- **Проверка:**
    - Selenoid: [http://localhost:4444/status](http://localhost:4444/status) — JSON.
    - UI: [http://localhost:8080](http://localhost:8080) — сессии, клик для VNC/видео.
    - Тесты: Запустите простой тест, проверьте в UI.
- **Общие Проблемы:**
    - "Command not found": Добавьте `.\` перед cm.exe.
    - "Pull access denied": Проверьте версии на aerokube.com; `docker login`.
    - "Port occupied": `netstat -ano | findstr :4444`; убейте PID.
    - "No such image": Обновите browsers.json на актуальные версии.
    - Логи: `docker logs <container_id>` (id из `docker ps`).
    - Флейковые тесты: Добавьте caps.setCapability("enableLog", true) для логов.
- **Советы для Фреймворка:**
    - Добавьте start-all.bat в репозиторий.
    - В README: "Установите Docker, скачайте CM, отредактируйте browsers.json если нужно, запустите start-all.bat, измените config.properties на remote, запустите тесты."
    - Масштабирование: `--limit 10` для 10 сессий; используйте Docker Compose для кластера.
    - Обновление: `.\cm.exe selenoid update` для новых образов.
    - Видео: В caps "enableVideo": true; видео в ~/.aerokube/selenoid/video.

Если проблемы — проверьте официальную документацию [aerokube.com/selenoid/latest](https://aerokube.com/selenoid/latest/). Удачи с тестами! Если вопросы — уточните.
```