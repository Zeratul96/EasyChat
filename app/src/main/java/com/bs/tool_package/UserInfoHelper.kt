package com.bs.tool_package

import com.bs.database.DataBaseUtil

/**
 * Created by 13273 on 2017/11/9.
 *
 */
object UserInfoHelper {

    /**
     * 查询用户地址
     */
    @JvmStatic fun findUserArea(areaID: String): String {

        if (areaID == "-1") return "此用户未填写"
        val areaList = DataBaseUtil.queryArea()
        if(areaList.isEmpty()) return ""

        var parentID = ""
        var city = ""
        var province = ""

        var iterator = areaList.iterator()
        while (iterator.hasNext()) {
            val per = iterator.next()
            if (per["area_id"] == areaID) {
                city = per["name"]!!
                parentID = per["parent_id"]!!
                break
            }
        }

        iterator = areaList.iterator()
        while (iterator.hasNext() && parentID != "0") {
            val per = iterator.next()
            if (per["area_id"] == parentID) {
                province = per["name"] + " "
                break
            }
        }
        return province + city
    }
}