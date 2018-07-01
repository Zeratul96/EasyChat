package com.bs.tool_package;

/**
 * Created by 13273 on 2017/11/22.
 *
 */

public class ExpressParse {

    private static String[] expressCharacter = new String[]{
            "微笑","开心","笑哭","大笑","尬笑","坏笑","卖萌","天使","羡慕","睡觉",
            "色","撇嘴","难过","伤心","激动","外星人","流泪","亲亲","骷髅","大便",
            "么么哒","倒脸","憨笑","惊讶","得意","调皮亲亲","抿嘴","调皮","吐舌","生气"};

    /**
     * 将表情解析成字
     */
    public static String parseExpress(String number){
        return "\\"+expressCharacter[Integer.parseInt(number)];
    }
}
