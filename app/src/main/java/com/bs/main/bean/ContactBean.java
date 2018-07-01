package com.bs.main.bean;

import com.mcxtzhang.indexlib.IndexBar.bean.BaseIndexPinyinBean;

/**
 * Created by 13273 on 2017/10/11.
 *
 */

public class ContactBean extends BaseIndexPinyinBean{

    private String name;
    private boolean isTop = false;//是否是最上面的 不需要被转化成拼音

    public ContactBean(){}

    public ContactBean(String name){this.name = name;}

    public String getContactName(){return this.name;}

    public ContactBean setContactName(String name){
        this.name = name;
        return this;
    }

    public boolean isTop(){return isTop;}

    public ContactBean setTop(boolean isTop){
        this.isTop = isTop;
        return this;
    }

    @Override
    public String getTarget() {return name;}

    @Override
    public boolean isNeedToPinyin() {return !isTop;}

    @Override
    public boolean isShowSuspension() {return !isTop;}

}
