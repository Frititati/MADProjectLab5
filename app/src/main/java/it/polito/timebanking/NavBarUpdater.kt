package it.polito.timebanking

interface NavBarUpdater {
    fun updateIMG(url: String)
    fun updateTime(time: Long)
    fun updateFName(name: String)
    fun setNavBarTitle(title: String?)
}