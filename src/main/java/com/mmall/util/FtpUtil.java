package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FtpUtil {

    private static Logger logger = LoggerFactory.getLogger(FtpUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPassword = PropertiesUtil.getProperty("ftp.pass");

    public static final String ftpServerHttpPrefix = PropertiesUtil.getProperty("ftp.server.http.prefix");

    private static FTPClient ftpClient = new FTPClient();

    public static boolean uploadFile(List<File> fileList)  throws IOException {

        return FtpUtil.uploadFile("img", fileList);

    }

    private static boolean uploadFile(String remotePath, List<File> fileList) throws IOException {

        FileInputStream fis = null;
        boolean uploaded = true;

        Boolean isConnect = FtpUtil.connectServer(FtpUtil.ftpIp, FtpUtil.ftpUser, FtpUtil.ftpPassword);
        if(isConnect) try {
            ftpClient.changeWorkingDirectory(remotePath);
            ftpClient.setBufferSize(1024);
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            for (File file : fileList) {
                fis = new FileInputStream(file);
                ftpClient.storeFile(file.getName(), fis);
            }
        } catch (IOException e) {
            uploaded = Boolean.FALSE;
            logger.error("上传文件异常！", e);
        } finally {
            if (null != fis)
                fis.close();
            ftpClient.disconnect();
        }
        return uploaded;
    }

    private static boolean connectServer(String ip, String user, String password){
        Boolean flag = Boolean.FALSE;
        try {
            ftpClient.connect(ip);
            flag = ftpClient.login(user, password);
        } catch (IOException e) {
            logger.error("连接ftp失败！", e);
        }
        return flag;
    }

}
