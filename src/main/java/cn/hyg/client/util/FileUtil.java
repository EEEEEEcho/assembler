package cn.hyg.client.util;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 说明：文件处理
 * 作者：
 * 官网：
 */
public class FileUtil {

    /**
     * 获取文件大小 返回 KB 保留3位小数  没有文件时返回0
     *
     * @param filenameWithPath 文件完整路径，包括文件名
     * @return Long
     */
    public static Long getFileSizeNumber(String filenameWithPath) {
        File bakPath = new File(filenameWithPath);
        return bakPath.length();
    }

    /**
     * 获取文件大小 返回 KB 保留3位小数  没有文件时返回0
     *
     * @param filepath 文件完整路径，包括文件名
     * @return Double
     */
    public static String getFileSize(String filepath) {
        File f = new File(filepath);
        return getFileSize(f.length());
    }

    public static String getFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString;
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 创建目录
     *
     * @param destDirName 目标目录名
     * @return Boolean
     */
    public static Boolean createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {
            return true;
        }

        // 判断有没有父路径，就是判断文件整个路径是否存在
        if (!dir.getParentFile().exists()) {
            // 不存在就全部创建
            if (createDir(dir.getParent())) {
                return dir.mkdirs();
            } else {
                System.out.println("创建文件夹失败: " + dir.getAbsolutePath());
                //throw new IOException("create folder " + dirPath.getAbsolutePath() + " failed!");
                return false;
            }
        }
        return dir.mkdirs();
    }

    /**
     * 删除文件
     *
     * @param filePathAndName String 文件路径及名称 如c:/fqf.txt
     */
    public static void delFile(String filePathAndName) {
        try {
            File myDelFile = new File(filePathAndName);
            myDelFile.delete();
        } catch (Exception e) {
            System.out.println("删除文件操作出错: " + filePathAndName);
            HyExceptionHandler.handleException(e);
        }
    }

    /**
     * 读取到字节数组0
     *
     * @param filePath //路径
     * @throws IOException e
     */
    public static byte[] getContent(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        fi.close();
        return buffer;
    }

    /**
     * 读取到字节数组1
     *
     * @param filePath String
     * @return byte[]
     * @throws IOException e
     */
    public static byte[] toByteArray(String filePath) throws IOException {

        File f = new File(filePath);
        if (!f.exists()) {
            throw new FileNotFoundException(filePath);
        }
        BufferedInputStream in;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length())) {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            HyExceptionHandler.handleException(e);
            throw e;
        }
    }

    /**
     * 读取到字节数组2
     *
     * @param filePath String
     * @return byte[]
     * @throws IOException e
     */
    public static byte[] toByteArray2(String filePath) throws IOException {
        File f = new File(filePath);
        if (!f.exists()) {
            throw new FileNotFoundException(filePath);
        }
        FileChannel channel = null;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(f);
            channel = fs.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
            while ((channel.read(byteBuffer)) > 0) {
                // do nothing
                // System.out.println("reading");
            }
            return byteBuffer.array();
        } catch (IOException e) {
            HyExceptionHandler.handleException(e);
            throw e;
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                HyExceptionHandler.handleException(e);
            }
            try {
                fs.close();
            } catch (IOException e) {
                HyExceptionHandler.handleException(e);
            }
        }
    }

    /**
     * Mapped File way MappedByteBuffer 可以在处理大文件时，提升性能
     *
     * @param filePath String
     * @return byte[]
     * @throws IOException e
     */
    public static byte[] toByteArray3(String filePath) throws IOException {

        FileChannel fc = null;
        RandomAccessFile rf = null;
        try {
            rf = new RandomAccessFile(filePath, "r");
            fc = rf.getChannel();
            MappedByteBuffer byteBuffer = fc.map(MapMode.READ_ONLY, 0,
                    fc.size()).load();
            //System.out.println(byteBuffer.isLoaded());
            byte[] result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
                // System.out.println("remain");
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        } catch (IOException e) {
            HyExceptionHandler.handleException(e);
            throw e;
        } finally {
            try {
                rf.close();
                fc.close();
            } catch (IOException e) {
                HyExceptionHandler.handleException(e);
            }
        }
    }

    public static String readFileContent(File file, String encoding, String filePath) {
        StringBuilder fileContent = new StringBuilder();

        try {
            if (file.isFile() && file.exists()) {        // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);    // 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    fileContent.append(lineTxt);
                    fileContent.append("\n");
                }
                read.close();
            } else {
                System.out.println("找不到指定的文件,查看此路径是否正确:" + filePath);
            }
        } catch (Exception e) {
            HyExceptionHandler.handleException(e);
            System.out.println("读取文件内容出错");
        }

        return fileContent.toString();
    }

    public static String readFileContent2Str(String filenameWithPath, String encoding) {

        File file = new File(filenameWithPath);
        StringBuilder fileContent = new StringBuilder();

        try {
            if (file.isFile() && file.exists()) {        // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);    // 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String s = lineTxt.trim();
                    // #和//开头的为注释行，直接忽略
                    if (!s.startsWith("#") && !s.startsWith("\\/\\/")) {
                        fileContent.append(lineTxt);
                        fileContent.append("\t");
                    }
                }
                read.close();
            } else {
                System.out.println("找不到指定的文件,查看此路径是否正确:" + filenameWithPath);
            }
        } catch (Exception e) {
            HyExceptionHandler.handleException(e);
            System.out.println("读取文件内容出错");
        }

        return fileContent.toString();
    }

    public static List<String> readFileContent(String filenameWithPath, String encoding) {

        File file = new File(filenameWithPath);
        List<String> result = new ArrayList<>();

        try {
            if (file.isFile() && file.exists()) {        // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);    // 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    result.add(lineTxt);
                }
                read.close();
            } else {
                System.out.println("找不到指定的文件,查看此路径是否正确:" + filenameWithPath);
            }
        } catch (Exception e) {
            HyExceptionHandler.handleException(e);
            System.out.println("读取文件内容出错");
        }

        return result;
    }

    /**
     * 仅保留文件名不保留后缀
     */
//    public static String getFilenameWithoutSuffix(String pathAndName) {
//        pathAndName = pathAndName.replaceAll("\\\\", "/");
//        int start = pathAndName.lastIndexOf("/");
//        int end = pathAndName.lastIndexOf(".");
//        if (start == -1 && end == -1) {
//            return pathAndName;
//        } else if (start != -1 && end != -1) {
//            return pathAndName.substring(start + 1, end);
//        } else if (end != -1) {
//            return pathAndName.substring(start + 1, end);
//        } else {
//            return pathAndName.substring(start + 1);
//        }
//    }
    public static String getFilenameWithoutSuffix(String pathAndName) {
        // S1_00002507_R2.fq.gz 去掉所有的后缀名 可以得到 S1_00002507_R2，完全去掉后缀名。，
        // 原来的只去一个后缀
        if(pathAndName.contains(".fq")){
            return pathAndName.split(".fq")[0];
        }
        else if(pathAndName.contains(".fas")){
            return pathAndName.split(".fas")[0];
        }
        else{
            return pathAndName.split(".f")[0];
        }
    }
    /**
     * 保留文件名及后缀
     */
    public static String getFilenameWithSuffix(String pathAndName) {
        pathAndName = pathAndName.replaceAll("\\\\", "/");
        int start = pathAndName.lastIndexOf("/");
        return pathAndName.substring(start + 1);
    }

    /**
     * 保留文件名后缀
     */
    private static String getSuffix(String filename) {
        return filename.lastIndexOf(".")==-1?"":filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 获取路径名
     */
    public static String getPathName(String pathAndName) {
        pathAndName = pathAndName.replaceAll("\\\\", "/");
        int end = pathAndName.lastIndexOf("/");
        if (end==-1) {
            return null;
        } else {
            return pathAndName.substring(0, end+1);
        }
    }

    public static String getFileName(String pathAndName) {
        pathAndName = pathAndName.replaceAll("\\\\", "/");
        int end = pathAndName.lastIndexOf("/");
        if (end==-1) {
            return pathAndName;
        } else {
            return pathAndName.substring(end);
        }
    }

    public static Boolean checkIsImg(String filename) {
        String suffix = getSuffix(filename);
        if (suffix.equals("")) {
            return false;
        }
        String extension = "bmp,jpg,gif,psd,png,tiff,tga,eps";
        return extension.contains(suffix);
    }

    public static File fileValidation(String filenameWithPath) {
        if (filenameWithPath == null || filenameWithPath.equals("")) {
            return null;
        }
        File file = new File(filenameWithPath);
        if (!file.exists()) {
            System.out.println("文件不存在: " + filenameWithPath);
            return null;
        }

        return file;
    }

    // 遍历某个文件夹下的文件名
    public static List<String> listFilenamesByFolder(String folder) {
        List<String> result = new ArrayList<>();
        File targetFolder = new File(folder);
        if (!targetFolder.exists() || !targetFolder.isDirectory()) {
            return result;
        }
        String[] fa = targetFolder.list();
        if (fa == null || fa.length == 0) {
            return result;
        }
        return Arrays.asList(fa);
    }

    // 遍历某个文件夹下的文件
    public static List<File> listFilesByFolder(String folder) {
        List<File> result = new ArrayList<>();
        File targetFolder = new File(folder);
        if (!targetFolder.exists() || !targetFolder.isDirectory()) {
            return result;
        }
        File[] fa = targetFolder.listFiles();
        if (fa == null || fa.length == 0) {
            return result;
        }
        return Arrays.asList(fa);
    }

    static String ieFilenameFix(String filename) {
        if (filename.contains("\\")) {
            int slash = filename.lastIndexOf("\\");
            if (slash != -1) {
                filename = filename.substring(slash + 1);
            }
            // Windows doesn't like /'s either
            int slash2 = filename.lastIndexOf("/");
            if (slash2 != -1) {
                filename = filename.substring(slash2 + 1);
            }
            // In case the name is C:foo.txt
            int slash3 = filename.lastIndexOf(":");
            if (slash3 != -1) {
                filename = filename.substring(slash3 + 1);
            }
        }
        return filename;
    }

    /**
     * 从文件末尾开始读取文件，并逐行打印
     * @param filename file path
     * @param charset character
     * @param lines 行数
     * @param posMap 上次读取的结束位置
     */
    public static List<String> readFromEnd(String filename, String charset, int lines, Map<String, Long> posMap) {
        RandomAccessFile rf = null;
        List<String> result = new ArrayList<>();
        long startPos = 0;
        if (posMap == null) {
            posMap = new HashMap<>();
        }

        if (posMap.size() == 0 || posMap.get("prev") == null) {
            posMap.put("prev", 0L);
        } else {
            startPos = posMap.get("prev");
        }

        try {
            rf = new RandomAccessFile(filename, "r");
            long fileLength = rf.length();
            long start = rf.getFilePointer();   // 返回此文件中的当前偏移量
            long readIndex = start + fileLength -1;
            posMap.put("prev", readIndex);
            String line;
            rf.seek(readIndex);  // 设置偏移量为文件末尾
            int c;
            int n = 0;
            while (readIndex > startPos) {

                if (n >= lines) {
                    break;
                }

                c = rf.read();
                String readText;
                if (c == '\n' || c == '\r') {
                    line = rf.readLine();
                    n++;
                    if (StringUtils.isNotBlank(line)) {
                        //System.out.println("read line : " + line);
                        readText = new String(line.getBytes("ISO-8859-1"), charset);
                        result.add(readText);
                    }
                    readIndex--;
                }
                readIndex--;
                rf.seek(readIndex);
                if (readIndex == 0) { // 当文件指针退至文件开始处，输出第一行
                    readText = rf.readLine();
                    if (StringUtils.isNotBlank(readText)) {
                        //System.out.println(readText);
                        result.add(readText);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            HyExceptionHandler.handleException(e);
        } finally {
            try {
                if (rf != null)
                    rf.close();
            } catch (IOException e) {
                HyExceptionHandler.handleException(e);
            }
        }

        return result;
    }

    public static String fileToZip(String zipFilePath, List<File> fs) {

        File dirPath = new File(zipFilePath);

        if (!dirPath.exists()) {
            if (!dirPath.mkdirs()) {
                // if create directory fails, throw io exception
                System.out.println("创建文件夹: " + dirPath.getAbsolutePath() + " 失败!");
                return "";
            }
        }

        String targetFn = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(System.currentTimeMillis())) + ".zip";

        String zipFilenameWithPath = zipFilePath + File.separator + targetFn;

        System.out.println("目标压缩文件：" + zipFilenameWithPath);
        File zipFile = new File(zipFilenameWithPath);
        /*if (zipFile.exists()) {
            logger.error(zipFilePath + " 目录下已存在名为: " + targetFn + "的文件");
            return "";
        }*/

        FileOutputStream fos;
        ZipOutputStream zos = null;
        FileInputStream fis;
        BufferedInputStream bis = null;

        try {

            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(new BufferedOutputStream(fos));

            for (File sourceFile : fs) {

                if (!sourceFile.exists()) {
                    System.out.println("待压缩的源文件：" + sourceFile.getAbsoluteFile() + "不存在.");
                    continue;
                }

                fis = new FileInputStream(sourceFile);
                bis = new BufferedInputStream(fis, 1024 * 10);

                byte[] buffer = new byte[1024 * 10];

                ZipEntry zipEntry = new ZipEntry(sourceFile.getName());
                zos.putNextEntry(zipEntry);
                // 读取待压缩的文件并写进压缩包里

                int read;
                while ((read = bis.read(buffer, 0, 1024 * 10)) != -1) {
                    zos.write(buffer, 0, read);
                }
            }

            return zipFilenameWithPath;

        } catch (IOException e) {
            HyExceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } finally {
            // 关闭流
            try {
                if (null != bis) {
                    bis.close();
                }
                if (null != zos) {
                    zos.close();
                }
            } catch (IOException e) {
                HyExceptionHandler.handleException(e);
            }
        }
    }

    public static String appendTimestampToFilename(String rawName) {

        /*String filenameWoSuffix = getFilenameWithoutSuffix(rawName);
        String suffix = getSuffix(rawName);

        String result = filenameWoSuffix + "." + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
        if (StringUtils.isNotBlank(suffix)) {
            result += "." + suffix;
        }

        return result;*/

        return rawName + "." + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));

    }

    public static String removeTimestampInFilename(String rawName) {

        String filenameWoSuffix = getFilenameWithoutSuffix(rawName);
        String suffix = getSuffix(rawName);
        if (StringUtils.isBlank(suffix) || StringUtils.isBlank(filenameWoSuffix) || !filenameWoSuffix.contains(".")) {
            return rawName;
        }

        int idx = filenameWoSuffix.lastIndexOf(".");
        if (idx == -1) {
            return rawName;
        }

        return filenameWoSuffix.substring(0, idx) + "." + suffix;

    }

    /**
     * 复制单个文件
     *
     * @param oldFilenameWithPath String 原文件路径 如：c:/fqf.txt
     * @param newFilenameWithPath String 复制后路径 如：f:/fqf.txt
     */
    public boolean copy(String oldFilenameWithPath, String newFilenameWithPath) {
        try {
            int byteSum = 0;
            int byteRead;
            File oldFile = new File(oldFilenameWithPath);
            if (oldFile.exists()) { // 文件存在时
                InputStream inStream = new FileInputStream(oldFilenameWithPath); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newFilenameWithPath);
                byte[] buffer = new byte[1444];
                // int length;
                while ((byteRead = inStream.read(buffer)) != -1) {
                    byteSum += byteRead; // 字节数 文件大小
                    System.out.println(byteSum);
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
            }
            return true;
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public boolean copyFolder(String oldPath, String newPath) {

        File target = new File(newPath);
        // 判断文件夹是否存在
        if (target.exists()) {
            return false;
        }

        // 新建文件夹
        if (!target.mkdirs()) {
            return false;
        }

        try {
            File a = new File(oldPath);
            String[] file = a.list();
            if (file == null || file.length == 0) {
                return false;
            }

            File temp;
            for (String f : file) {

                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + f);
                } else {
                    temp = new File(oldPath + File.separator + f);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) { // 如果是子文件夹
                    copyFolder(oldPath + "/" + f, newPath + "/" + f);
                }
            }

            return true;
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
            return false;
        }
    }

    public static void insertNewLine(String srcFile, List<String> insertLines, String pattern) {
        Path path = Paths.get(srcFile);
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int position = 0;
        if (lines == null) {
            lines = new ArrayList<>();
        }

        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i);
            if (s.contains(pattern)) {
                position = i;
                break;
            }
        }

        for (String line : insertLines) {
            position++;
            lines.add(position, line);
        }

        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 逐行写入外部文件
    public static void writeFile(String filenameWithPath, List<String> lines) {
        /* 输出数据 */
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filenameWithPath)),
                    "UTF-8"));
            for (int i = 0; i< lines.size(); i++) {
                bw.write(lines.get(i));
                if (i < lines.size() - 1) {
                    bw.newLine();
                }
            }
            bw.close();
        } catch (Exception e) {
            System.err.println("写文件异常: " + filenameWithPath);
        }
    }

    public static void openDir(String targetDir) {
        String cmd[];
        // 调用资源浏览器打开文件夹
        if (System.getProperties().getProperty("os.name").toLowerCase().contains("linux")) {
            //cmd = "dde-file-manager " + targetDir;
            cmd = new String[]{"sh","-c","nautilus " + targetDir};
        } else {
            cmd = new String[]{"explorer ", targetDir};
        }
        ShellUtil.runCmd(cmd);
    }

}