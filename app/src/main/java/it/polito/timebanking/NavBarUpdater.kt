package it.polito.timebanking

interface NavBarUpdater {
    fun updateIMG(url: String)
    fun updateTime(time: Long)
    fun updateActiveJobs(consuming: Long, producing:Long)
    fun updateFName(name: String)
    fun setNavBarTitle(title: String?)
}