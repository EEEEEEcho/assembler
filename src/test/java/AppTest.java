import cn.hyg.client.model.Task;
import cn.hyg.client.repository.ConfigRepo;
import cn.hyg.client.repository.TaskRepo;
import cn.hyg.client.util.UuidUtil;
import org.junit.Test;

import java.util.Date;
import java.util.Random;

public class AppTest {

    @Test
    public void createTaskTable() {
        TaskRepo tr = new TaskRepo();
        tr.create();
    }

    @Test
    public void createConfigTable() {
        ConfigRepo cr = new ConfigRepo();
        cr.create();
    }

    @Test
    public void initRandomTaskData() {
        TaskRepo tr = new TaskRepo();
        int[] idx = {10, 11, 60, 404};
        String[] types = {"QC", "ASM", "PRD", "ANT"};

        for (int i = 0; i < 55; i++) {
            Task t = new Task();
            t.setSerial(UuidUtil.get32UUIDFull());
            String files = "SM04.L350_BDMS190009619-1a_1.fq.gz,SM04.L350_BDMS190009619-1a_2.fq.gz";
            t.setFiles(files);
            t.setPath("/home/cdc/test");

            Random random = new Random();
            int r = random.nextInt(idx.length);
            int a = idx[r];
            String type = types[r];
            t.setState(a);
            t.setType(type);
            t.setSubmitTime(new Date());
            switch (a) {
                case 11:
                    t.setStartTime(new Date());
                case 60:
                case 404:
                    t.setFinishTime(new Date());
                    tr.finishTask(t.getSerial(), "init", t.getState());
                    break;
                default:
                    break;
            }
            tr.insert(t);
        }
    }


}
