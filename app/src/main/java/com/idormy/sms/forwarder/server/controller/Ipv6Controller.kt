package com.idormy.sms.forwarder.server.controller

import com.idormy.sms.forwarder.server.model.BaseRequest
import com.idormy.sms.forwarder.server.model.EmptyData
import com.yanzhenjie.andserver.annotation.*
import java.net.Inet6Address
import java.net.NetworkInterface
import java.util.Collections

@RestController
@RequestMapping(path = ["/ipv6"])
class Ipv6Controller {

    @CrossOrigin(methods = [RequestMethod.POST])
    @PostMapping("/query")
    fun query(@RequestBody bean: BaseRequest<EmptyData>): Map<String, String> {
        return mapOf("ipv6" to getIPv6Address())
    }

    /**
     * 获取本机 IPv6 地址 (强制优先获取 2xxx 开头的公网地址)
     */
    private fun getIPv6Address(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            // 定义一个变量存备胎（如果找不到公网的，就随便返回一个非本地的）
            var fallbackIp = ""

            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet6Address && !addr.isLinkLocalAddress) {
                        val hostAddress = addr.hostAddress
                        if (!hostAddress.isNullOrEmpty()) {
                            // 处理 Scope ID (如 %wlan0)
                            val cleanIp = if (hostAddress.indexOf('%') > 0) 
                                hostAddress.substring(0, hostAddress.indexOf('%')) 
                                else hostAddress

                            // 🎯 核心修改：如果是 2 开头（公网地址），直接返回！
                            if (cleanIp.startsWith("2")) {
                                return cleanIp
                            }

                            // 如果不是 2 开头（比如 fd 开头），先存起来当备胎
                            if (fallbackIp.isEmpty()) {
                                fallbackIp = cleanIp
                            }
                        }
                    }
                }
            }
            // 如果循环完了都没找到 2 开头的，那就返回备胎（fd开头的），总比没有好
            // 或者你也可以直接 return "" 宁缺毋滥
            return fallbackIp 

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}
