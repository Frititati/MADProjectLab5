package it.polito.timebanking

interface NavBarUpdater {
    fun updateIMG(url: String)
    fun updateTime(time: String)
    fun updateFName(name: String)
    fun setTitleWithSkill(title: String?)
}