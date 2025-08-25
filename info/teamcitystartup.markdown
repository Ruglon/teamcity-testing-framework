No worries — that “waiting to stop…” loop happens when the Windows service won’t exit cleanly. Here’s how to **force it down**, **keep it from auto-restarting**, then **run TeamCity from the console** and **read the Super User token**.

---

## 1) Kill the stuck service cleanly (PowerShell, run **as Administrator**)

**A. Stop the agent first (prevents file locks)**

```powershell
Stop-Service "TeamCity Build Agent" -Force -ErrorAction SilentlyContinue
```

**B. Try a normal stop with a timeout**

```powershell
Stop-Service -Name TeamCity -Force -ErrorAction Continue -Verbose
# If your service name is different, check both:
Get-Service -Name TeamCity
Get-Service -DisplayName "TeamCity Server"
```

If it still hangs, get the PID and kill the underlying process:

**C. Find the service PID and kill it**

```powershell
$svc = Get-CimInstance Win32_Service -Filter "Name='TeamCity'"
$svc | Select-Object Name, State, Status, ProcessId
if ($svc.ProcessId -ne 0) { Stop-Process -Id $svc.ProcessId -Force }
```

(Alternative with classic tools:)

```powershell
sc.exe queryex TeamCity
taskkill /PID <PID_FROM_ABOVE> /F
```

**D. Make sure nothing is still listening on 8111**

```powershell
netstat -ano | findstr ":8111"
# If you see a PID, kill it:
taskkill /PID <PID> /F
```

> If the service keeps coming back by itself, it’s usually Recovery settings or Automatic startup. See the next step.

---

## 2) Stop the service from auto-restarting

Set the startup type to **Manual** (or Disabled) so it won’t compete with your console run:

```powershell
Set-Service -Name TeamCity -StartupType Manual
Set-Service -Name "TCBuildAgent" -StartupType Manual  # if present
```

(If you’re not sure of the agent’s service name, run `Get-Service *teamcity*` and set any you see to Manual.)

Optional (UI): Services → **TeamCity Server** → **Recovery** tab → set “First/Second/Subsequent failures” to **Take No Action**.

---

## 3) Start TeamCity **from the console**

Open a **new** PowerShell window (normal user is fine once the service is stopped):

```powershell
cd D:\Projects\TeamCity\bin
.\teamcity-server.bat run
```

* Keep this window open — it’s your live server log.
* When it says it’s started, open `http://localhost:8111/`.

To stop the console server later, press **Ctrl+C** in that window, or from another window:

```powershell
cd D:\Projects\TeamCity\bin
.\teamcity-server.bat stop 30 -force
```

---

## 4) Read the **Super User (maintenance) token**

You’ll see it right in the console on startup, a line like:

```
Super user authentication token: 1234567890
```

If you missed it, grab it from the log:

```powershell
Select-String -Path "C:\TeamCity\logs\teamcity-server.log" -Pattern "Super user authentication token" | Select-Object -Last 1
```

Use it to log in:

* Go to `http://localhost:8111/login.html?super=1` and paste the token, **or**
* On the normal login page, leave username empty and paste the token as the password.

> Note: A **new** Super User token is generated **each time** the server starts.

---

## 5) (Optional) Run the agent from the console

In a separate PowerShell window:

```powershell
cd D:\Projects\TeamCity\buildAgent\bin
.\agent.bat start
# stop later with:
.\agent.bat stop
```

---

### If it’s still stuck after killing the PID

* Reboot once to clear a “Stopping” state the Service Control Manager can’t shake off.
* After reboot, set startup type to **Manual** (Step 2), ensure port **8111** is free, then start via the console (Step 3).

If you paste the output of:

```powershell
Get-Service *teamcity* | Format-List *
Get-CimInstance Win32_Service -Filter "Name='TeamCity'" | Select Name,State,Status,ProcessId,StartMode
```

I’ll pinpoint the exact snag (wrong service name, permissions, recovery auto-restart, etc.).
