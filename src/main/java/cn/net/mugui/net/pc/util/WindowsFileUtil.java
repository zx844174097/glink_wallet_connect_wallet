package cn.net.mugui.net.pc.util;

import com.mugui.base.client.net.classutil.DataSave;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.internal.win32.BROWSEINFO;
import org.eclipse.swt.internal.win32.OPENFILENAME;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.TCHAR;

import com.mugui.base.base.Component;

import static org.eclipse.swt.internal.win32.OS.BIF_NEWDIALOGSTYLE;
import static org.eclipse.swt.internal.win32.OS.BIF_RETURNONLYFSDIRS;

/**
 * windows 文件处理器
 * 
 * @author root
 *
 */
@Component
public class WindowsFileUtil {

	private static final int OFN_FILEMUSTEXIST = 0x00001000;
	private static final int OFN_PATHMUSTEXIST = 0x00000800;
	String[] filterNames = new String[0];
	String[] filterExtensions = new String[0];
	String[] fileNames = new String[0];
	String filterPath = "", fileName = "";
	int filterIndex = 0;
	boolean overwrite = false;
	static final String FILTER = "*.*";
	static int BUFFER_SIZE = 1024 * 32;
	static boolean USE_HOOK = true;
	static {
		/*
		 * Feature in Vista. When OFN_ENABLEHOOK is set in the save or open file dialog,
		 * Vista uses the old XP look and feel. OFN_ENABLEHOOK is used to grow the file
		 * name buffer in a multi-select file dialog. The fix is to only use
		 * OFN_ENABLEHOOK when the buffer has overrun.
		 */
		if (!OS.IsWinCE && OS.WIN32_VERSION >= OS.VERSION(6, 0)) {
			USE_HOOK = false;
		}
	}

	/**
	 * Makes the dialog visible and brings it to the front of the display.
	 *
	 * @return a string describing the absolute path of the first selected file, or
	 *         null if the dialog was cancelled or an error occurred
	 *
	 * @exception SWTException
	 *                         <ul>
	 *                         <li>ERROR_WIDGET_DISPOSED - if the dialog has been
	 *                         disposed</li>
	 *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from
	 *                         the thread that created the dialog</li>
	 *                         </ul>
	 */
	public String openFiles() {
		long /* int */ hHeap = OS.GetProcessHeap();

		/*
		 * Feature in Windows. There is no API to set the orientation of a file dialog.
		 * It is always inherited from the parent. The fix is to create a hidden parent
		 * and set the orientation in the hidden parent for the dialog to inherit.
		 */
		boolean enabled = false;

		String title;
		/* Convert the title and copy it into lpstrTitle */
		/* Use the character encoding for the default locale */
		TCHAR buffer3 = new TCHAR(0, "选择文件", true);
		int byteCount3 = buffer3.length() * TCHAR.sizeof;
		long /* int */ lpstrTitle = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount3);
		OS.MoveMemory(lpstrTitle, buffer3, byteCount3);

		/* Compute filters and copy into lpstrFilter */
		String strFilter = "";
		if (filterNames == null)
			filterNames = new String[0];
		if (filterExtensions == null)
			filterExtensions = new String[0];
		for (int i = 0; i < filterExtensions.length; i++) {
			String filterName = filterExtensions[i];
			if (i < filterNames.length)
				filterName = filterNames[i];
			strFilter = strFilter + filterName + '\0' + filterExtensions[i] + '\0';
		}
		if (filterExtensions.length == 0) {
			strFilter = strFilter + FILTER + '\0' + FILTER + '\0';
		}
//		strFilter=".xls;.xlxs";
		/* Use the character encoding for the default locale */
		TCHAR buffer4 = new TCHAR(0, strFilter, true);
		int byteCount4 = buffer4.length() * TCHAR.sizeof;
		long /* int */ lpstrFilter = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount4);
		OS.MoveMemory(lpstrFilter, buffer4, byteCount4);

		/* Convert the fileName and filterName to C strings */
		if (fileName == null)
			fileName = "";
		/* Use the character encoding for the default locale */
		TCHAR name = new TCHAR(0, fileName, true);

		/*
		 * Copy the name into lpstrFile and ensure that the last byte is NULL and the
		 * buffer does not overrun.
		 */
		int nMaxFile = OS.MAX_PATH;

		int byteCount = nMaxFile * TCHAR.sizeof;
		long /* int */ lpstrFile = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount);
		int byteCountFile = Math.min(name.length() * TCHAR.sizeof, byteCount - TCHAR.sizeof);
		OS.MoveMemory(lpstrFile, name, byteCountFile);

		/*
		 * Copy the path into lpstrInitialDir and ensure that the last byte is NULL and
		 * the buffer does not overrun.
		 */
		if (filterPath == null)
			filterPath = "";
		/* Use the character encoding for the default locale */
		TCHAR path = new TCHAR(0, filterPath.replace('/', '\\'), true);
		int byteCount5 = OS.MAX_PATH * TCHAR.sizeof;
		long /* int */ lpstrInitialDir = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount5);
		int byteCountDir = Math.min(path.length() * TCHAR.sizeof, byteCount5 - TCHAR.sizeof);
		OS.MoveMemory(lpstrInitialDir, path, byteCountDir);

		/* Create the file dialog struct */
		OPENFILENAME struct = new OPENFILENAME();
		struct.lStructSize = OPENFILENAME.sizeof;
		struct.Flags = OS.OFN_HIDEREADONLY | OS.OFN_NOCHANGEDIR;

		struct.Flags |= OS.OFN_OVERWRITEPROMPT;

		struct.hwndOwner=OS.GetForegroundWindow();
		struct.lpstrTitle = lpstrTitle;
		struct.lpstrFile = lpstrFile;
		struct.nMaxFile = nMaxFile;
		struct.lpstrInitialDir = lpstrInitialDir;
		struct.lpstrFilter = lpstrFilter;
		struct.nFilterIndex = filterIndex == 0 ? filterIndex : filterIndex + 1;

		/*
		 * Set the default extension to an empty string. If the user fails to type an
		 * extension and this extension is empty, Windows uses the current value of the
		 * filter extension at the time that the dialog is closed.
		 */
		long /* int */ lpstrDefExt = 0;

		/*
		 * Open the dialog. If the open fails due to an invalid file name, use an empty
		 * file name and open it again.
		 */
		boolean sucess = OS.GetOpenFileName(struct);

		lpstrFile = struct.lpstrFile;

		/* Set the new path, file name and filter */
		fileNames = new String[0];
		String fullPath = null;
		if (sucess) {

			/* Use the character encoding for the default locale */
			TCHAR buffer = new TCHAR(0, struct.nMaxFile);
			int byteCount1 = buffer.length() * TCHAR.sizeof;
			OS.MoveMemory(buffer, lpstrFile, byteCount1);

			/*
			 * Bug in WinCE. For some reason, nFileOffset and nFileExtension are always zero
			 * on WinCE HPC. nFileOffset is always zero on WinCE PPC when using
			 * GetSaveFileName(). nFileOffset is correctly set on WinCE PPC when using
			 * OpenFileName(). The fix is to parse lpstrFile to calculate nFileOffset.
			 * 
			 * Note: WinCE does not support multi-select file dialogs.
			 */
			int nFileOffset = struct.nFileOffset;
			if (OS.IsWinCE && nFileOffset == 0) {
				int index = 0;
				while (index < buffer.length()) {
					int ch = buffer.tcharAt(index);
					if (ch == 0)
						break;
					if (ch == '\\')
						nFileOffset = index + 1;
					index++;
				}
			}
			if (nFileOffset > 0) {

				/* Use the character encoding for the default locale */
				TCHAR prefix = new TCHAR(0, nFileOffset - 1);
				int byteCount2 = prefix.length() * TCHAR.sizeof;
				OS.MoveMemory(prefix, lpstrFile, byteCount2);
				filterPath = prefix.toString(0, prefix.length());

				/*
				 * Get each file from the buffer. Files are delimited by a NULL character with 2
				 * NULL characters at the end.
				 */
				int count = 0;
				int start = nFileOffset;
				do {
					int end = start;
					while (end < buffer.length() && buffer.tcharAt(end) != 0)
						end++;
					String string = buffer.toString(start, end - start);
					start = end;
					if (count == fileNames.length) {
						String[] newFileNames = new String[fileNames.length + 4];
						System.arraycopy(fileNames, 0, newFileNames, 0, fileNames.length);
						fileNames = newFileNames;
					}
					fileNames[count++] = string;
					start++;
				} while (start < buffer.length() && buffer.tcharAt(start) != 0);

				if (fileNames.length > 0)
					fileName = fileNames[0];
				String separator = "";
				int length = filterPath.length();
				if (length > 0 && filterPath.charAt(length - 1) != '\\') {
					separator = "\\";
				}
				fullPath = filterPath + separator + fileName;
				if (count < fileNames.length) {
					String[] newFileNames = new String[count];
					System.arraycopy(fileNames, 0, newFileNames, 0, count);
					fileNames = newFileNames;
				}
			}
			filterIndex = struct.nFilterIndex - 1;

		}
		/* Free the memory that was allocated. */
		OS.HeapFree(hHeap, 0, lpstrFile);
		OS.HeapFree(hHeap, 0, lpstrFilter);
		OS.HeapFree(hHeap, 0, lpstrInitialDir);
		OS.HeapFree(hHeap, 0, lpstrTitle);
		if (lpstrDefExt != 0)
			OS.HeapFree(hHeap, 0, lpstrDefExt);
		return fullPath;
	}

	/**
	 * 选择文件夹
	 */
	public String openDirectory(){

		long /* int */ hHeap = OS.GetProcessHeap();

		/* Create the file dialog struct */
		BROWSEINFO struct = new BROWSEINFO();

		struct.hwndOwner=OS.GetForegroundWindow();
		int BIF_UAHINT=0x00000100;
		struct.ulFlags= BIF_RETURNONLYFSDIRS | BIF_NEWDIALOGSTYLE | BIF_UAHINT/*带TIPS提示*/;


		/* Convert the title and copy it into lpstrTitle */
		/* Use the character encoding for the default locale */
		TCHAR buffer3 = new TCHAR(0, "选择文件夹", true);
		int byteCount3 = buffer3.length() * TCHAR.sizeof;
		long /* int */ lpstrTitle = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount3);
		OS.MoveMemory(lpstrTitle, buffer3, byteCount3);
		struct.lpszTitle=lpstrTitle;

		//选择根路径
		/* Convert the title and copy it into lpstrTitle */
		/* Use the character encoding for the default locale */
//		TCHAR buffer4 = new TCHAR(0, DataSave.APP_PATH.replaceAll("\\\\","\\\\\\\\"),true);
//		int byteCount4 = buffer4.length() * TCHAR.sizeof;
//		long /* int */ lpstrPath = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount4);
//		OS.MoveMemory(lpstrPath, buffer4, byteCount4);
//
//		struct.lParam=lpstrPath;

		struct.lpszTitle=lpstrTitle;

		long l = OS.SHBrowseForFolderW(struct);
		fileNames = new String[0];
		String fullPath = null;
		if (l!=0) {

			char [] lpstrFile = new char[1024];
			boolean b = OS.SHGetPathFromIDListW(l, lpstrFile);
			if(b){
				fullPath=new String(lpstrFile).trim();
			}

		}

		OS.HeapFree(hHeap, 0, lpstrTitle);
//		OS.HeapFree(hHeap, 0, lpstrPath);
		return fullPath;
	}

}
