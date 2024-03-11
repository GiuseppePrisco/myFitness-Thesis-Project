package com.example.greetingcard.data

import com.example.greetingcard.model.RepsData
import com.example.greetingcard.network.RepsApi


interface RepsDataRepository {
    suspend fun getRepsData(): List<RepsData>
    //suspend fun postRepsData(repsList: List<RepsData>)
    suspend fun postRepsData(repsList: RepsData): String
}

class NetworkRepsDataRepository() : RepsDataRepository {
    override suspend fun getRepsData(): List<RepsData> {
        return RepsApi.myRetrofitService.getData()
    }
    /*override suspend fun postRepsData(repsList: List<RepsData>) {
        return RepsApi.myRetrofitService.postData(repsList)
    }*/
    override suspend fun postRepsData(repsList: RepsData): String {
        return RepsApi.myRetrofitService.postData(repsList)
    }
}