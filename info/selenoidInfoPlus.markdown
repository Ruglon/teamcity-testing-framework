The command `netstat -ano | findstr :4444` returns nothing because no process is currently listening on port 4444 on your machine. This means Selenoid (which should bind to that port) is not running or failed to start properly. Here's how to fix it step by step:

1. **Confirm Selenoid is stopped**: Run `docker ps` — if "selenoid" container is not listed or not "Up", it's not running.

2. **Restart Selenoid**: Use Configuration Manager (CM) to launch it again:
   ```
   .\cm.exe selenoid start --browsers-json config\browsers.json --vnc
   ```
   - Watch the logs for "Listening on :4444" — if it succeeds, the port will be bound.

3. **Re-check the port**: Run `netstat -ano | findstr :4444` again. It should now show something like:
   ```
   TCP    0.0.0.0:4444           0.0.0.0:0              LISTENING       [PID]
   ```
   - [PID] is the process ID of Docker/Selenoid.

4. **If still nothing**:
   - Check Docker logs for Selenoid: `docker logs selenoid` (if container exists).
   - Ensure no firewall blocks 4444: In Windows Defender Firewall → Advanced Settings → Inbound Rules, add a rule for TCP port 4444 (allow all).
   - Restart Docker Desktop and try launching again.
   - If CM fails, try direct Docker launch:
     ```
     docker run -d --name selenoid -p 4444:4444 -v /var/run/docker.sock:/var/run/docker.sock -v ./config:/etc/selenoid aerokube/selenoid:latest-release -conf /etc/selenoid/browsers.json
     ```

Once port 4444 is listening, relaunch UI with `--selenoid-uri http://host.docker.internal:4444` as before, and it should connect without "connection refused". If logs show new errors, share them.