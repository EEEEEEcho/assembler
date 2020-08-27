package cn.hyg.client.value;

public enum TaskType {

    QC(1, "质控"),
    ASM(2, "拼接"),
    PRD(3, "预测"),
    ANT(4, "注释");

    public final int key;
    public final String value;

    TaskType(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static TaskType valueOf(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new IndexOutOfBoundsException("invalid ordinal");
        }

        return values()[ordinal];
    }

    public static TaskType getByValue(String value) {

        for (TaskType m : values()) {
            if (m.value.equals(value)) {
                return m;
            }
        }

        return null;

    }
}
