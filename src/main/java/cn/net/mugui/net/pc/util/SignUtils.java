package cn.net.mugui.net.pc.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 类描述：　[签名方式的工具类]<br/>
 * 项目名称：[herou]<br/>
 * 包名：　　[com.herou.utils]<br/>
 * 创建人：　[杨挺(yang.pollyyeung@gmail.com)]<br/>
 * 创建时间：[2017/8/9 14:53]<br/>
 */
public class SignUtils
{
	public final static String SHA_256 = "SHA-256";
	public final static String SHA_1 = "SHA-1";
	public final static String MD5 = "MD5";

	private static final String K_SIGN = "sign";

	/** 计算sign */
	public static String markSign(String appKey, String... key_val)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		for (int i = 1; i < key_val.length; i += 2)
			if (key_val[i] != null && key_val[i - 1] != null)
				map.put(key_val[i - 1], key_val[i]);
		return markSignFlag(appKey, map);
	}

	/** 具体执行计算sign */
	public static String markSignFlag(String appKey, Map<String, Object> params)
	{
		ArrayList<String> keys = new ArrayList<String>(params.keySet());
		keys.remove(K_SIGN);
		Collections.sort(keys);
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < keys.size(); i++)
		{
			final String key = keys.get(i);
			final String value = String.valueOf(params.get(key));
			if (key != null && key.length() > 0)
			{
				builder.append(key).append("=").append(value).append('&');
			}
		}
		builder.append(appKey);
		String srcData = builder.toString();
		return encrypt(MD5, srcData);
	}

	/**
	 * 对字符串加密,加密算法使用MD5,SHA-1,SHA-256,默认使用SHA-256
	 * @param encName 加密类型
	 * @param strSrc 要加密的字符串
	 */
	public static String encrypt(String encName, Object... strSrc)
	{
		String strDes = null;
		if (strSrc != null)
		{
			try
			{
				if (encName == null || encName.length() == 0)
				{
					encName = SHA_256;
				}
				MessageDigest md = MessageDigest.getInstance(encName);
				for (Object o : strSrc)
				{
					if (o instanceof byte[])
					{
						md.update((byte[]) o);
					}
					else if (o instanceof String)
					{
						md.update(((String) o).getBytes());
					}
					else if (o != null)
					{
						md.update(String.valueOf(o).getBytes());
					}
				}
				byte[] d = md.digest();
				strDes = bytes2Hex(d); // to HexString
			}
			catch (NoSuchAlgorithmException e)
			{
				// e.printStackTrace();
			}
		}
		return strDes;
	}

	/* ------------------------------------------------------- */

	private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
//	protected static final char[] HEX_DIGITS_UPPER = "0123456789ABCDEF".toCharArray();

	private static String _bytes2Hex(byte[] bts, char[] hexDigits)
	{
		char[] o = new char[bts.length + bts.length];
		for (int i = 0, j = 0; i < bts.length; i++)
		{
			byte b = bts[i];
			o[j++] = hexDigits[(b & 0xf0) >> 4];
			o[j++] = hexDigits[b & 0x0f];
		}
		return new String(o);
	}

	public static String bytes2Hex(byte[] bts)
	{
		return _bytes2Hex(bts, HEX_DIGITS);
	}

	public static String bytes2HexUpper(byte[] bts)
	{
		return _bytes2Hex(bts, HEX_DIGITS);
	}

	/* ------------------------------------------------------- */

	public static String genSign_PkReq(String msg)
	{
		return genSign_PkReq(msg.getBytes());
	}

	public static String genSign_PkReq(byte[] data)
	{
		return encrypt(MD5, "SDK", data, "YINGXIONG-2015");
	}

	public static String genSign_PkResp(String msg)
	{
		return genSign_PkResp(msg.getBytes());
	}

	public static String genSign_PkResp(byte[] data)
	{
		return encrypt(MD5, "YINGXIONG-2015", data, "DKS");
	}

	public static String genSign_AuthSign(String msg, String key)
	{
		return encrypt(MD5, "SDK", msg, key);
	}

}
