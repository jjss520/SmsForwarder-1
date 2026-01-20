package com.idormy.sms.forwarder.server.controller

import com.idormy.sms.forwarder.utils.HttpServerUtils
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RestController
import java.net.Inet6Address
import java.net.NetworkInterface
import java.util.Collections

@RestController
@RequestMapping("/ipv6")
class Ipv6Controller {

    @GetMapping("/query")
    fun query(): String {
        val ipv6Addr = getIPv6Address()
        return if (ipv6Addr.isNotEmpty()) {
            // 返回成功结构
            HttpServerUtils.response(200, "success", mapOf("ipv6" to ipv6Addr))
        } else {
            // 返回失败结构
            HttpServerUtils.response(500, "未找到有效的IPv6地址", null)
        }
    }

    /**
     * 获取本机 IPv6 地址 (过滤掉环回地址和链路本地地址)
     */
    private fun getIPv6Address(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet6Address && !addr.isLinkLocalAddress) {
                        // 也就是不以 fe80 开头的 IPv6
                        val hostAddress = addr.hostAddress
                        if (!hostAddress.isNullOrEmpty()) {
                            // 移除可能存在的 Scope ID (例如 %wlan0)
                            val index = hostAddress.indexOf('%')
                            return if (index > 0) hostAddress.substring(0, index) else hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}
