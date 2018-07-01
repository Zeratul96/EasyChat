package com.bs.tool_package;

object ListCompareTools{

    /**
     * 找出AList中不在BList中的元素
     */
    @JvmStatic fun listFilter(aList:List<Map<String,String>> ,bList:List<Map<String,String>>)
            = aList.filterNot { bList.contains(it) }
}