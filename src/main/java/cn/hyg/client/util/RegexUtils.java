package cn.hyg.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    public static List<String> getMatches(String txt, String pattern) {
        List<String> result = new ArrayList<>();

        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(txt.trim());
        while (matcher.find()) {
            result.add(matcher.group(0).trim());
            //System.out.println(matcher.group(0));
        }

        return result;
    }

}
