package com.tacke.myapplication.data.model

enum class ScoreType(val code: String, val displayName: String) {
    SPY_P("spy P", "3DMark Time Spy"),
    SPY_X("spy X", "3DMark Time Spy Extreme"),
    SW_P("sw P", "3DMark Speed Way"),
    SW_DX("sw DX", "3DMark Steel Nomad DX12"),
    SW_B("sw B", "3DMark Steel Nomad Vulkan"),
    SW_DXLT("sw DXLT", "3DMark Steel Nomad Light DX12"),
    SW_VKLT("sw VKLT", "3DMark Steel Nomad Light Vulkan"),
    PR_P("pr P", "3DMark Port Royal"),
    SB_P("sb P", "3DMark Solar Bay"),
    SB_DXX("sb DXX", "3DMark Solar Bay Extreme DX12"),
    SB_VKX("sb VKX", "3DMark Solar Bay Extreme Vulkan"),
    FS_P("fs P", "3DMark Fire Strike"),
    FS_X("fs X", "3DMark Fire Strike Extreme"),
    FS_R("fs R", "3DMark Fire Strike Ultra"),
    WL_P("wl P", "3DMark Wild Life"),
    WL_X("wl X", "3DMark Wild Life Extreme"),
    NR_P("nr P", "3DMark Night Raid"),
    CRC_P("crc P", "3DMark CPU Profile"),
    STRG("strg", "3DMark Storage Benchmark"),
    PCM10B_D("pcm10b D", "PCMark 10 Benchmark"),
    PCM10EB_D("pcm10eb D", "PCMark 10 Express Benchmark"),
    PCM10EXB_D("pcm10exb D", "PCMark 10 Extended Benchmark"),
    VRPOR_DE("vrpor DE", "VRMark Orange Room"),
    VRPCR_DE("vrpcr DE", "VRMark Cyan Room"),
    VRPBR_DE("vrpbr DE", "VRMark Blue Room");

    companion object {
        fun getGpuBenchmarks(): List<ScoreType> = listOf(
            SPY_P, SPY_X, SW_P, SW_DX, SW_B, SW_DXLT, SW_VKLT,
            PR_P, SB_P, SB_DXX, SB_VKX, FS_P, FS_X, FS_R,
            WL_P, WL_X, NR_P, STRG
        )

        fun getCpuBenchmarks(): List<ScoreType> = listOf(
            CRC_P, PCM10B_D, PCM10EB_D, PCM10EXB_D
        )

        fun getVrBenchmarks(): List<ScoreType> = listOf(
            VRPOR_DE, VRPCR_DE, VRPBR_DE
        )
    }
}
