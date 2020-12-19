package com.tato0980.familytasks.CommonUtils

class UserModel(val email : String,val group : String, val name : String, val phone : String, val admin : String)
{
    constructor() : this(  "","","","", "")
}


class ItemModel(val email : String, val name : String, val description : String, val group : String, val image : String, val id : String)
{
    constructor() : this(  "","","","","", "")
}

class ItemListModel(val email : String, val name : String, val group : String, val description : String, val qty : String, val image : String, val id : String, val status : String)
{
    constructor() : this(  "","","","","", "","","")
}
