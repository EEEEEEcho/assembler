package cn.hyg.client.value;

/**
 * Created by BEI on 2017-10-16.
 * MenuEnum
 */
public enum MenuEnum {

    TASK(0, "任务管理");
    //TEST(1, "测试");

    public final int key;
    public final String value;

    MenuEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static MenuEnum valueOf(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new IndexOutOfBoundsException("invalid ordinal");
        }

        return values()[ordinal];
    }

    public static MenuEnum getByValue(String value) {

        for (MenuEnum m : values()) {
            if (m.value.equals(value)) {
                return m;
            }
        }

        return null;

    }
}
