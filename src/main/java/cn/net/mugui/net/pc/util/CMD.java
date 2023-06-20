//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package cn.net.mugui.net.pc.util;

import com.mugui.base.util.Other;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CMD {
    private static Process process;
    private boolean isTrue;
    private String info = "";
    private OutputStream outputStream = null;
    private static int winmode = -1;
    private static String CHARSET = "";
    public static final int LIUNX = 1;
    public static final int WINDOWS = 2;

    static {
        String os = System.getProperties().getProperty("os.name");
        if (!os.startsWith("win") && !os.startsWith("Win")) {
            winmode = 1;
            CHARSET = "UTF-8";
        } else {
            winmode = 2;
            CHARSET = "GBK";
        }

    }

    public CMD() {
    }


    public String getInfo() {
       String str=  this.info;
       reInfo();
       return str;
    }

    public void reInfo() {
        this.info = "";
    }

    public void start() {
        this.isTrue = true;

        try {
            ProcessBuilder builder = new ProcessBuilder("cmd");
            builder.redirectErrorStream(true);
            process = builder.start();

            PipedOutputStream pos = new PipedOutputStream();
            PipedInputStream pis = new PipedInputStream(pos);
            BufferedReader reader = new BufferedReader(new InputStreamReader(pis));

             outputStream = process.getOutputStream();
            new Thread(() -> {
                try {
                    InputStream inputStream = process.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        pos.write(buffer, 0, len);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    System.out.println("结束了");
                }
            }).start();
            new Thread(() -> {
                try {
                    String line;
                    while (true) {
                        if (!((line = reader.readLine()) != null)) break;
                        this.info+=line;
                        System.out.println(line);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    System.out.println("结束了");
                }
            }).start();
        } catch (IOException var2) {
            var2.printStackTrace();
            this.isTrue = false;
            process.destroy();
        }

    }

//    public void getRtInput() {
//        (new Thread(new Runnable() {
//            InputStream is = null;
//            BufferedReader br = null;
//
//            public void run() {
//                try {
//                    this.is = CMD.rt.getInputStream();
//                    this.br = new BufferedReader(new InputStreamReader(this.is, CMD.CHARSET));
//                    String s = null;
//
//                    while((s = this.br.readLine()) != null) {
//                        System.out.println(s);
//                    }
//                } catch (Exception var10) {
//                    var10.printStackTrace();
//                } finally {
//                    try {
//                        CMD.this.isTrue = false;
//                        if (this.br != null) {
//                            this.br.close();
//                        }
//
//                        if (this.is != null) {
//                            this.is.close();
//                        }
//
//                        CMD.rt.destroy();
//                    } catch (Exception var9) {
//                    }
//
//                }
//
//            }
//        })).start();
//    }

//    public void getRtInEur() {
//        (new Thread(new Runnable() {
//            BufferedReader br = null;
//            InputStream is = null;
//
//            public void run() {
//                try {
//                    this.is = CMD.rt.getErrorStream();
//                    this.br = new BufferedReader(new InputStreamReader(this.is, CMD.CHARSET));
//                    String s = null;
//
//                    while((s = this.br.readLine()) != null) {
//                        System.out.println(s);
//                    }
//                } catch (Exception var10) {
//                    var10.printStackTrace();
//                } finally {
//                    try {
//                        CMD.this.isTrue = false;
//                        if (this.br != null) {
//                            this.br.close();
//                        }
//
//                        if (this.is != null) {
//                            this.is.close();
//                        }
//
//                        CMD.rt.destroy();
//                    } catch (Exception var9) {
//                    }
//
//                }
//
//            }
//        })).start();
//    }

    public void send(String intfo) {
        if (intfo.equals("exit")) {
            Other.sleep(200);
//            rt.destroy();
            this.isTrue = false;
        } else {
            try {
                this.outputStream.write(intfo.getBytes(StandardCharsets.UTF_8));
                this.outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
