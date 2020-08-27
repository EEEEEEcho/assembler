package cn.hyg.client.util;

/**
 * created by ${user} on ${date}
 */
public class HyExceptionHandler {

    public static void handleException(Exception e) {
        e.printStackTrace();
        /*System.out.println("异常被处理掉了:以注解加继承的方式");
        System.out.println("异常的信息是:" + e.getClass().getName());*/
    }

}
