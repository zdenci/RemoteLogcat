package com.zdeno.logcat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class RemoteLogcat {

	private static Thread mThread;
	private volatile static boolean mQuit;

	private static String mStrIp = "192.168.1.1";
	private static int mPortNum = 58362;

	public static void start() {
		runLogs();
	}

	public static void start(final String ip, final int port) {
		mStrIp = ip;
		mPortNum = port;

		start();
	}

	public static void stop() {
		mQuit = true;
	}

	public static void setIp(final String ip) {
		mStrIp = ip;
	}

	public static void setPort(final int port) {
		mPortNum = port;
	}

	private static void runLogs() {
		mThread = new Thread( new Runnable() {
			@Override
			public void run() {
				BufferedReader br = null;
				mQuit = false;

				try {
					Runtime.getRuntime().exec(new String[] {"logcat", "-c"});

					final Process logCatProc = Runtime.getRuntime().exec("/system/bin/logcat -b main -v threadtime");
					if (logCatProc == null)
						return;

					br = new BufferedReader(new InputStreamReader(logCatProc.getInputStream()));
					final Socket socket = new Socket(mStrIp , mPortNum);
					final DataOutputStream dos = new DataOutputStream( socket.getOutputStream());

					if (br == null || dos == null)
						return;

					dos.writeBytes("---Starting log---\n");

					String strLine;
					while (mQuit == false) {
						strLine = br.readLine();
						if (strLine != null) {
							dos.writeBytes(strLine + "\n");
							dos.flush();
						}
					}

					br.close();
					br = null;

					dos.close();
					socket.close();

				} catch (final IOException e) {
					e.printStackTrace();
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (final IOException e) {
							e.printStackTrace();
						}
						br = null;
					}
				}
			}
		});

		if (mThread != null) {
			mThread.start();
		}
	}

}
