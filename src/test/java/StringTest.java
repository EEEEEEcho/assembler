import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringTest {
    public static void main(String[] args) {
//        String s1 = "1492.L222_2.fq.gz";
//        String s2 = "16520IVM_FM10247_524_L001_R1_001.fastq";
//        String s3 = "S1_00002507_R2.fq.gz";
//        String s4 = "sm04_L309_BDM-1a_1.fq.gz";
//        if(s1.contains("fq")){
//            String[] t = s1.split(".fq");
//            System.out.println(t[0]);
//        }
//        if(s2.contains("fas")){
//            String[] t = s2.split(".fas");
//            System.out.println(t[0]);
//        }
//        Date date = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        String dataString = dateFormat.format(date);
//        System.out.println(dataString);
        String s1 = "home/biouser/Sabccssae123134413/QC/";
        String tmp = s1.split("QC")[0];
        s1 = tmp + "assembly" + File.separator + "QC" + File.separator;
        System.out.println(s1);
    }
}
